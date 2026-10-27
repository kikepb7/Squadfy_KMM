import { FieldValue, Timestamp } from "firebase-admin/firestore";
import { DateTime } from "luxon";
import { db } from "../admin";
import { DeadlineRule } from "../domain/deadlineRule";
import { announcementWindow, instantOf, isoDay, nextMatchDate } from "../domain/matchCalendar";
import { publishChange } from "../live";
import { messages } from "../notifications/messages";
import { sendToUsers } from "../notifications/push";
import { paths } from "../paths";
import {
  AnnouncementDoc, closeRule, DEFAULT_DURATION_MINUTES, DEFAULT_TIME_ZONE, drawRule, MatchDoc, matchIdOf,
  ScheduleDoc, ScheduleExceptionDoc, uidOfMember
} from "./model";
import { activeMembers, clubName, readSchedule } from "./store";
import { HttpsError } from "firebase-functions/v2/https";
import { conflict } from "../errors";

/**
 * Creates matches with their announcement, from the weekly schedule or manually (backend MatchPlanningService).
 * Planning is idempotent: the match id is the club plus its time, so running it every hour, or twice at once, never
 * creates a duplicate.
 */

const MAX_WEEKS_AHEAD = 26;

/** Regular kickoff of a schedule on a local [date]. */
export const regularMatchTime = (schedule: ScheduleDoc, date: string) => instantOf(date, schedule.matchTime.slice(0, 5), schedule.timeZone);

async function hasUpcomingScheduledMatch(clubId: string, at: Date): Promise<boolean> {
  const snapshot = await db().collection(paths.matches(clubId))
    .where("status", "==", "SCHEDULED")
    .where("scheduledAt", ">", Timestamp.fromDate(at))
    .limit(1).get();
  return !snapshot.empty;
}

/** A week is planned when a match of that schedule date exists, or any match on that local day. */
async function isWeekPlanned(schedule: ScheduleDoc, date: string): Promise<boolean> {
  const matches = db().collection(paths.matches(schedule.clubId));
  if (!(await matches.where("scheduleDate", "==", date).limit(1).get()).empty) return true;
  const start = DateTime.fromISO(date, { zone: schedule.timeZone }).startOf("day");
  const sameDay = await matches
    .where("scheduledAt", ">=", Timestamp.fromDate(start.toJSDate()))
    .where("scheduledAt", "<", Timestamp.fromDate(start.plus({ days: 1 }).toJSDate()))
    .limit(1).get();
  return !sameDay.empty;
}

async function exceptionOf(clubId: string, date: string): Promise<ScheduleExceptionDoc | null> {
  const snapshot = await db().collection(paths.scheduleExceptions(clubId)).where("date", "==", date).limit(1).get();
  return snapshot.empty ? null : (snapshot.docs[0].data() as ScheduleExceptionDoc);
}

/**
 * Plans the next weekly match when the club has no upcoming scheduled match and nothing exists yet for the next match
 * week. Cancelled weeks are skipped and moved weeks planned at their new time (BE-008 RN-B2).
 */
export async function planNextMatch(clubId: string, at: Date = new Date()): Promise<string | null> {
  const schedule = await readSchedule(clubId);
  if (!schedule || !schedule.isActive) return null;
  if (await hasUpcomingScheduledMatch(clubId, at)) return null;
  const today = DateTime.fromJSDate(at, { zone: schedule.timeZone }).toISODate() as string;
  const firstDate = nextMatchDate(today, isoDay(schedule.matchDayOfWeek));
  for (let week = 0; week < MAX_WEEKS_AHEAD; week++) {
    const date = DateTime.fromISO(firstDate).plus({ weeks: week }).toISODate() as string;
    const exception = await exceptionOf(clubId, date);
    let scheduledAt: Date;
    if (exception?.type === "CANCELLED") continue;
    if (exception?.type === "RESCHEDULED") {
      const moved = exception.newScheduledAt?.toDate();
      if (!moved || moved <= at) continue;
      scheduledAt = moved;
    } else {
      scheduledAt = regularMatchTime(schedule, date);
    }
    if (await isWeekPlanned(schedule, date)) return null;
    return createMatchWithAnnouncement({
      clubId, scheduledAt, maxPlayers: schedule.maxPlayers, durationMinutes: schedule.matchDurationMinutes,
      zone: schedule.timeZone, scheduleDate: date, close: closeRule(schedule), draw: drawRule(schedule), now: at
    }).catch((e) => {
      // Another run planned the same match first
      if (e instanceof HttpsError && e.code === "failed-precondition") return null;
      throw e;
    });
  }
  return null;
}

/** Creates the match and its announcement (BE-002/BE-008). Rejects a second match of the club at the same time. */
export async function createMatchWithAnnouncement(params: {
  clubId: string;
  scheduledAt: Date;
  maxPlayers: number;
  durationMinutes?: number;
  zone?: string;
  scheduleDate?: string | null;
  close?: DeadlineRule;
  draw?: DeadlineRule;
  now?: Date;
}): Promise<string> {
  const { clubId, scheduledAt, maxPlayers } = params;
  const schedule = await readSchedule(clubId);
  const zone = params.zone ?? schedule?.timeZone ?? DEFAULT_TIME_ZONE;
  const now = params.now ?? new Date();
  const previous = await db().collection(paths.matches(clubId))
    .where("scheduledAt", "<", Timestamp.fromDate(scheduledAt))
    .orderBy("scheduledAt", "desc").limit(10).get();
  const previousMatchAt = previous.docs.map((d) => d.data() as MatchDoc).find((m) => m.status !== "CANCELLED")?.scheduledAt.toDate() ?? null;
  const window = announcementWindow({
    matchAt: scheduledAt, zone, previousMatchAt, now,
    close: params.close ?? (schedule ? closeRule(schedule) : undefined),
    draw: params.draw ?? (schedule ? drawRule(schedule) : undefined)
  });

  const matchId = matchIdOf(clubId, scheduledAt);
  const created = Timestamp.fromDate(now);
  const match: MatchDoc = {
    clubId, scheduledAt: Timestamp.fromDate(scheduledAt), status: "SCHEDULED", scheduleDate: params.scheduleDate ?? null,
    durationMinutes: params.durationMinutes ?? schedule?.matchDurationMinutes ?? DEFAULT_DURATION_MINUTES,
    manualTeamAScore: null, manualTeamBScore: null, teamA: [], teamB: [], events: [], ratingChanges: {},
    createdAt: created, updatedAt: created
  };
  const announcement: AnnouncementDoc = {
    matchId, clubId, maxPlayers,
    opensAt: Timestamp.fromDate(window.opensAt), closesAt: Timestamp.fromDate(window.closesAt), drawAt: Timestamp.fromDate(window.drawAt),
    status: "OPEN", entries: [], openedNotifiedAt: null, closingReminderSentAt: null, teamsPublishedAt: null,
    createdAt: created, updatedAt: created
  };
  const batch = db().batch();
  batch.create(db().doc(paths.match(clubId, matchId)), match);
  batch.create(db().doc(paths.announcement(clubId, matchId)), announcement);
  try {
    await batch.commit();
  } catch (e) {
    if ((e as { code?: unknown }).code === 6) conflict("The club already has a match at that time");
    throw e;
  }
  await publishChange(clubId, "MATCH", matchId);
  return matchId;
}

/**
 * Moves a scheduled match keeping its enrollments: close and draw are recalculated for the new date, a closed
 * announcement opens again if the new close is in the future, and every member is notified (BE-008 RN-B3).
 */
export async function moveMatch(clubId: string, matchId: string, newScheduledAt: Date, reason: string | null = null) {
  const schedule = await readSchedule(clubId);
  const zone = schedule?.timeZone ?? DEFAULT_TIME_ZONE;
  const now = new Date();
  const window = announcementWindow({
    matchAt: newScheduledAt, zone, previousMatchAt: null, now,
    close: schedule ? closeRule(schedule) : undefined, draw: schedule ? drawRule(schedule) : undefined
  });
  const matchDocRef = db().doc(paths.match(clubId, matchId));
  const previousAt = await db().runTransaction(async (tx) => {
    const match = (await tx.get(matchDocRef)).data() as MatchDoc;
    const announcementDocRef = db().doc(paths.announcement(clubId, matchId));
    const announcement = (await tx.get(announcementDocRef)).data() as AnnouncementDoc | undefined;
    const previous = match.scheduledAt.toDate();
    if (previous.getTime() === newScheduledAt.getTime()) return null;
    tx.update(matchDocRef, { scheduledAt: Timestamp.fromDate(newScheduledAt), updatedAt: FieldValue.serverTimestamp() });
    if (announcement) {
      const changes: Partial<Record<keyof AnnouncementDoc, unknown>> = {
        closesAt: Timestamp.fromDate(window.closesAt), drawAt: Timestamp.fromDate(window.drawAt), updatedAt: FieldValue.serverTimestamp()
      };
      if (announcement.closesAt.toMillis() !== window.closesAt.getTime()) changes.closingReminderSentAt = null;
      if (announcement.opensAt.toDate() > window.closesAt) changes.opensAt = Timestamp.fromDate(now < window.closesAt ? now : window.closesAt);
      if (announcement.status === "CLOSED" && window.closesAt > now) changes.status = "OPEN";
      if (window.drawAt > now) changes.teamsPublishedAt = null;
      tx.update(announcementDocRef, changes);
    }
    return previous;
  });
  if (!previousAt) return;
  await publishChange(clubId, "MATCH", matchId);
  const members = await activeMembers(clubId);
  await sendToUsers(messages.rescheduled(
    { type: "match.rescheduled", clubId, clubName: await clubName(clubId), matchId, zone, matchAt: newScheduledAt },
    members.map((m) => m.data.userId), previousAt, reason
  ));
}

/**
 * Cancels the match and its announcement (no permission check) and notifies the enrolled members. A week cancelled by
 * a schedule exception ([reason]) notifies every member if its announcement was already open (spec 020 APP-RN-H3).
 */
export async function cancelMatchInternal(clubId: string, matchId: string, reason: string | null = null) {
  const announcementDocRef = db().doc(paths.announcement(clubId, matchId));
  const { match, enrolled, wasOpen } = await db().runTransaction(async (tx) => {
    const match = (await tx.get(db().doc(paths.match(clubId, matchId)))).data() as MatchDoc;
    const announcement = (await tx.get(announcementDocRef)).data() as AnnouncementDoc | undefined;
    tx.update(db().doc(paths.match(clubId, matchId)), { status: "CANCELLED", updatedAt: FieldValue.serverTimestamp() });
    if (announcement) tx.update(announcementDocRef, { status: "CANCELLED", updatedAt: FieldValue.serverTimestamp() });
    const enrolled = (announcement?.entries ?? []).filter((e) => e.clubMemberId).map((e) => uidOfMember(e.clubMemberId as string));
    const wasOpen = announcement?.status === "OPEN" && announcement.opensAt.toDate() <= new Date();
    return { match, enrolled, wasOpen };
  });
  await publishChange(clubId, "MATCH", matchId);
  const recipients = reason && wasOpen ? (await activeMembers(clubId)).map((m) => m.data.userId) : enrolled;
  await sendToUsers(messages.cancelled(
    { type: "match.cancelled", clubId, clubName: await clubName(clubId), matchId, zone: (await readSchedule(clubId))?.timeZone ?? DEFAULT_TIME_ZONE, matchAt: match.scheduledAt.toDate() },
    recipients, reason
  ));
}

/** Brings back a match cancelled by a schedule exception, with its enrollments (BE-008 RN-B4). */
export async function reactivateMatch(clubId: string, matchId: string) {
  await db().runTransaction(async (tx) => {
    const announcementDocRef = db().doc(paths.announcement(clubId, matchId));
    const announcement = (await tx.get(announcementDocRef)).data() as AnnouncementDoc | undefined;
    tx.update(db().doc(paths.match(clubId, matchId)), { status: "SCHEDULED", updatedAt: FieldValue.serverTimestamp() });
    if (announcement) {
      tx.update(announcementDocRef, { status: announcement.closesAt.toDate() > new Date() ? "OPEN" : "CLOSED", updatedAt: FieldValue.serverTimestamp() });
    }
  });
  await publishChange(clubId, "MATCH", matchId);
}
