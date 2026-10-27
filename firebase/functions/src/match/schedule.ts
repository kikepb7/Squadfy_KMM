import { Timestamp } from "firebase-admin/firestore";
import { DateTime } from "luxon";
import { onCall } from "firebase-functions/v2/https";
import { db } from "../admin";
import { requireManager, requireMember } from "../clubs/access";
import { DEFAULT_DEADLINE, validateDeadlines } from "../domain/deadlineRule";
import { DAYS_OF_WEEK, DayOfWeek } from "../domain/matchCalendar";
import { MATCH_FORMATS, MatchFormat, maxPlayersOf } from "../domain/types";
import { badRequest, conflict, notFound } from "../errors";
import { body, requireAuth, requireString } from "../guards";
import { publishChange } from "../live";
import { paths } from "../paths";
import { DEFAULT_DURATION_MINUTES, DEFAULT_TIME_ZONE, exceptionDto, MatchDoc, ScheduleDoc, ScheduleExceptionDoc, scheduleDto } from "./model";
import { cancelMatchInternal, moveMatch, planNextMatch, reactivateMatch, regularMatchTime, createMatchWithAnnouncement } from "./planning";
import { readSchedule } from "./store";

/** Weekly schedule and its exceptions (BE-002, BE-008 RN-B, RN-D; spec 019 AC-019-08). */

const TIME = /^([01]\d|2[0-3]):[0-5]\d(:[0-5]\d)?$/;
const normalizeTime = (value: string) => (value.length === 5 ? `${value}:00` : value);

function time(value: unknown, field: string): string {
  if (typeof value !== "string" || !TIME.test(value)) badRequest(`${field} must be HH:mm`, "VALIDATION_ERROR");
  return normalizeTime(value as string);
}

function dayOfWeek(value: unknown): DayOfWeek {
  if (typeof value !== "string" || !DAYS_OF_WEEK.includes(value as DayOfWeek)) badRequest("Invalid matchDayOfWeek", "VALIDATION_ERROR");
  return value as DayOfWeek;
}

function format(value: unknown): MatchFormat {
  if (typeof value !== "string" || !(value in MATCH_FORMATS)) badRequest("Invalid format", "VALIDATION_ERROR");
  return value as MatchFormat;
}

function zone(value: unknown): string {
  if (typeof value !== "string" || !DateTime.local().setZone(value.trim()).isValid) badRequest(`Unknown time zone: ${value}`);
  return (value as string).trim();
}

function positiveInt(value: unknown, field: string, min = 0): number {
  if (!Number.isInteger(value) || (value as number) < min) badRequest(`Invalid ${field}`, "VALIDATION_ERROR");
  return value as number;
}

function checkDeadlines(s: ScheduleDoc) {
  const error = validateDeadlines(
    s.matchTime, { daysBefore: s.closeDaysBefore, time: s.closeTime }, { daysBefore: s.drawDaysBefore, time: s.drawTime }
  );
  if (error) badRequest(error);
}

export const getSchedule = onCall(async (request) => {
  const uid = requireAuth(request);
  const clubId = requireString(body(request), "clubId");
  await requireMember(clubId, uid);
  const schedule = await readSchedule(clubId);
  if (!schedule) notFound("No match schedule found for this club");
  return scheduleDto(schedule as ScheduleDoc);
});

export const createSchedule = onCall(async (request) => {
  const uid = requireAuth(request);
  const data = body(request);
  const clubId = requireString(data, "clubId");
  await requireManager(clubId, uid);
  const now = Timestamp.now();
  const matchFormat = format(data.format);
  const close = {
    daysBefore: data.closeDaysBefore === undefined || data.closeDaysBefore === null ? DEFAULT_DEADLINE.daysBefore : positiveInt(data.closeDaysBefore, "closeDaysBefore"),
    time: data.closeTime ? time(data.closeTime, "closeTime") : `${DEFAULT_DEADLINE.time}:00`
  };
  // The draw follows the close unless it is configured (backend: draw ?: close)
  const hasDraw = data.drawDaysBefore !== undefined && data.drawDaysBefore !== null;
  const schedule: ScheduleDoc = {
    clubId,
    matchDayOfWeek: dayOfWeek(data.matchDayOfWeek),
    matchTime: time(data.matchTime, "matchTime"),
    timeZone: data.timeZone ? zone(data.timeZone) : DEFAULT_TIME_ZONE,
    format: matchFormat,
    maxPlayers: maxPlayersOf(matchFormat),
    matchDurationMinutes: data.matchDurationMinutes ? positiveInt(data.matchDurationMinutes, "matchDurationMinutes", 1) : DEFAULT_DURATION_MINUTES,
    closeDaysBefore: close.daysBefore,
    closeTime: close.time,
    drawDaysBefore: hasDraw ? positiveInt(data.drawDaysBefore, "drawDaysBefore") : close.daysBefore,
    drawTime: hasDraw && data.drawTime ? time(data.drawTime, "drawTime") : close.time,
    isActive: true,
    createdAt: now,
    updatedAt: now
  };
  checkDeadlines(schedule);
  try {
    await db().doc(paths.schedule(clubId)).create(schedule);
  } catch (e) {
    if ((e as { code?: unknown }).code === 6) conflict("A match schedule already exists for this club");
    throw e;
  }
  await planNextMatch(clubId);
  await publishChange(clubId, "SCHEDULE");
  return scheduleDto(schedule);
});

export const updateSchedule = onCall(async (request) => {
  const uid = requireAuth(request);
  const data = body(request);
  const clubId = requireString(data, "clubId");
  await requireManager(clubId, uid);
  const current = await readSchedule(clubId);
  if (!current) notFound("No match schedule found for this club");
  const schedule: ScheduleDoc = { ...(current as ScheduleDoc) };
  if (data.matchDayOfWeek != null) schedule.matchDayOfWeek = dayOfWeek(data.matchDayOfWeek);
  if (data.matchTime != null) schedule.matchTime = time(data.matchTime, "matchTime");
  if (data.timeZone != null) schedule.timeZone = zone(data.timeZone);
  if (data.format != null) {
    schedule.format = format(data.format);
    schedule.maxPlayers = maxPlayersOf(schedule.format);
  }
  if (data.isActive != null) schedule.isActive = data.isActive === true;
  if (data.matchDurationMinutes != null) schedule.matchDurationMinutes = positiveInt(data.matchDurationMinutes, "matchDurationMinutes", 1);
  if (data.closeDaysBefore != null) schedule.closeDaysBefore = positiveInt(data.closeDaysBefore, "closeDaysBefore");
  if (data.closeTime != null) schedule.closeTime = time(data.closeTime, "closeTime");
  if (data.drawDaysBefore != null) schedule.drawDaysBefore = positiveInt(data.drawDaysBefore, "drawDaysBefore");
  if (data.drawTime != null) schedule.drawTime = time(data.drawTime, "drawTime");
  checkDeadlines(schedule);
  schedule.updatedAt = Timestamp.now();
  await db().doc(paths.schedule(clubId)).set(schedule);
  if (schedule.isActive) await planNextMatch(clubId);
  await publishChange(clubId, "SCHEDULE");
  return scheduleDto(schedule);
});

export const getScheduleExceptions = onCall(async (request) => {
  const uid = requireAuth(request);
  const clubId = requireString(body(request), "clubId");
  await requireMember(clubId, uid);
  const snapshot = await db().collection(paths.scheduleExceptions(clubId)).orderBy("date").get();
  return snapshot.docs.map((d) => exceptionDto(d.id, d.data() as ScheduleExceptionDoc));
});

async function weekMatch(clubId: string, date: string): Promise<{ id: string; data: MatchDoc } | null> {
  const snapshot = await db().collection(paths.matches(clubId)).where("scheduleDate", "==", date).get();
  const latest = snapshot.docs.sort((a, b) => b.get("createdAt").toMillis() - a.get("createdAt").toMillis())[0];
  return latest ? { id: latest.id, data: latest.data() as MatchDoc } : null;
}

/**
 * Cancels or moves a future week of the schedule (BE-008 RN-B1…B3) with a mandatory reason (spec 020 APP-RN-H2). A moved
 * match stays within its week, Monday to Sunday (D-20); a new exception for a week replaces the previous one.
 */
export const addScheduleException = onCall(async (request) => {
  const uid = requireAuth(request);
  const data = body(request);
  const clubId = requireString(data, "clubId");
  await requireManager(clubId, uid);
  const schedule = await readSchedule(clubId);
  if (!schedule) notFound("No match schedule found for this club");
  const s = schedule as ScheduleDoc;
  const date = requireString(data, "date", 10, 10);
  const day = DateTime.fromISO(date);
  if (!day.isValid) badRequest("Invalid date", "VALIDATION_ERROR");
  if (data.type !== "CANCELLED" && data.type !== "RESCHEDULED") badRequest("Invalid type", "VALIDATION_ERROR");
  const type = data.type as "CANCELLED" | "RESCHEDULED";
  const newScheduledAt = typeof data.newScheduledAt === "string" ? new Date(data.newScheduledAt) : null;
  if (newScheduledAt && Number.isNaN(newScheduledAt.getTime())) badRequest("Invalid newScheduledAt", "VALIDATION_ERROR");
  const reason = requireString(data, "reason", 1, 500);
  const now = new Date();

  if (DAYS_OF_WEEK[day.weekday - 1] !== s.matchDayOfWeek) badRequest(`date must be a ${s.matchDayOfWeek}, the club's match day`);
  let match = await weekMatch(clubId, date);
  const currentMatchAt = match?.data.scheduledAt.toDate() ?? regularMatchTime(s, date);
  if (currentMatchAt <= now) badRequest("Only future weeks can have an exception");
  if (type === "CANCELLED" && newScheduledAt) badRequest("newScheduledAt is only allowed for RESCHEDULED");
  if (type === "RESCHEDULED" && (!newScheduledAt || newScheduledAt <= now)) badRequest("RESCHEDULED requires a future newScheduledAt");
  if (type === "RESCHEDULED" && !sameWeek(day, DateTime.fromJSDate(newScheduledAt as Date, { zone: s.timeZone }))) {
    badRequest("A week can only move within that week, Monday to Sunday", "VALIDATION_ERROR");
  }
  if (match?.data.status === "COMPLETED") conflict("The match of that week is already completed");
  const existing = await db().collection(paths.scheduleExceptions(clubId)).where("date", "==", date).limit(1).get();
  if (!existing.empty) {
    await undoException(clubId, s, existing.docs[0].ref, existing.docs[0].data() as ScheduleExceptionDoc);
    match = await weekMatch(clubId, date);
  }
  if (type === "RESCHEDULED" && match?.data.status === "CANCELLED") conflict("The match of that week is cancelled");

  const planned = match && match.data.status === "SCHEDULED" ? match : null;
  const exception: ScheduleExceptionDoc = {
    clubId, date, type, newScheduledAt: newScheduledAt ? Timestamp.fromDate(newScheduledAt) : null, reason,
    affectedMatchId: planned?.id ?? null, createdAt: Timestamp.now()
  };
  const ref = await db().collection(paths.scheduleExceptions(clubId)).add(exception);
  if (planned) {
    if (type === "CANCELLED") await cancelMatchInternal(clubId, planned.id, reason);
    else await moveMatch(clubId, planned.id, newScheduledAt as Date, reason);
  }
  // A cancelled week lets the following one be planned right away
  await planNextMatch(clubId);
  await publishChange(clubId, "SCHEDULE");
  return exceptionDto(ref.id, exception);
});

/** Undoes an exception while its week is in the future and its match is not completed (RN-B4). */
export const deleteScheduleException = onCall(async (request) => {
  const uid = requireAuth(request);
  const data = body(request);
  const clubId = requireString(data, "clubId");
  const exceptionId = requireString(data, "exceptionId");
  await requireManager(clubId, uid);
  const ref = db().doc(`${paths.scheduleExceptions(clubId)}/${exceptionId}`);
  const snapshot = await ref.get();
  if (!snapshot.exists) notFound("Schedule exception not found");
  const exception = snapshot.data() as ScheduleExceptionDoc;
  const schedule = await readSchedule(clubId);
  if (!schedule) notFound("No match schedule found for this club");
  const now = new Date();
  const regularAt = regularMatchTime(schedule as ScheduleDoc, exception.date);
  const match = await weekMatch(clubId, exception.date);
  if (regularAt <= now || (match && match.data.scheduledAt.toDate() <= now) || match?.data.status === "COMPLETED") {
    conflict("The exception can no longer be undone: its week has already been played");
  }
  await undoException(clubId, schedule as ScheduleDoc, ref, exception);
  await publishChange(clubId, "SCHEDULE");
  return { ok: true };
});

const sameWeek = (a: DateTime, b: DateTime) => a.weekYear === b.weekYear && a.weekNumber === b.weekNumber;

/** Removes an exception and brings its week back to the regular schedule (RN-B4). */
async function undoException(clubId: string, schedule: ScheduleDoc, ref: FirebaseFirestore.DocumentReference, exception: ScheduleExceptionDoc) {
  const regularAt = regularMatchTime(schedule, exception.date);
  const match = await weekMatch(clubId, exception.date);
  await ref.delete();
  if (exception.type === "CANCELLED") {
    if (!match) await restoreWeek(clubId, schedule, exception.date, regularAt);
    else if (match.data.status === "CANCELLED" && match.id === exception.affectedMatchId) await reactivateMatch(clubId, match.id);
  } else if (match?.data.status === "SCHEDULED") {
    await moveMatch(clubId, match.id, regularAt);
  }
}

/** Plans a week whose cancellation was undone before its match existed, if it comes before every upcoming match. */
async function restoreWeek(clubId: string, schedule: ScheduleDoc, date: string, regularAt: Date) {
  if (!schedule.isActive) return;
  const next = await db().collection(paths.matches(clubId))
    .where("status", "==", "SCHEDULED").where("scheduledAt", ">", Timestamp.now())
    .orderBy("scheduledAt").limit(1).get();
  if (next.empty) {
    await planNextMatch(clubId);
  } else if (regularAt < (next.docs[0].get("scheduledAt") as Timestamp).toDate()) {
    await createMatchWithAnnouncement({
      clubId, scheduledAt: regularAt, maxPlayers: schedule.maxPlayers, durationMinutes: schedule.matchDurationMinutes,
      zone: schedule.timeZone, scheduleDate: date
    });
  }
}
