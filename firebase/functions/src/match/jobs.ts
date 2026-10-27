import { Timestamp } from "firebase-admin/firestore";
import { logger } from "firebase-functions/v2";
import { HttpsError, onCall } from "firebase-functions/v2/https";
import { onSchedule } from "firebase-functions/v2/scheduler";
import { db } from "../admin";
import { publishChange } from "../live";
import { messages } from "../notifications/messages";
import { isEmulator, sendToUsers } from "../notifications/push";
import { paths } from "../paths";
import { absentMemberIds } from "./absences";
import { publishTeamsAtDrawTime } from "./matches";
import { AnnouncementDoc, DEFAULT_TIME_ZONE, MatchDoc, ScheduleDoc, uidOfMember } from "./model";
import { planNextMatch } from "./planning";
import { runSeasonRollover } from "./seasons";
import { activeMembers, clubName, readSchedule } from "./store";

/** Time-based work of the match cycle (backend MatchSchedulerService; spec 019 AC-019-08, AC-019-10). */

const CLOSING_REMINDER_HOURS = 24;
/** Spec 021 APP-RN-P3: the match-day reminder goes out this long before kickoff. */
const MATCH_REMINDER_HOURS = 2;

/** Plans the next match of every active schedule; idempotent, so a missed run is caught up. */
export async function runPlanning(at: Date = new Date()) {
  const schedules = await db().collectionGroup("schedule").where("isActive", "==", true).get();
  let planned = 0;
  for (const doc of schedules.docs) {
    try {
      if (await planNextMatch((doc.data() as ScheduleDoc).clubId, at)) planned++;
    } catch (e) {
      logger.error("[jobs] planning failed", (doc.data() as ScheduleDoc).clubId, e);
    }
  }
  return planned;
}

/** Closes the announcements whose close time passed, then publishes the teams whose draw time arrived (RN-D3). */
export async function runCloseAndDraw(at: Date = new Date()) {
  const now = Timestamp.fromDate(at);
  const expired = await db().collectionGroup("announcements").where("status", "==", "OPEN").where("closesAt", "<=", now).get();
  for (const doc of expired.docs) {
    const a = doc.data() as AnnouncementDoc;
    await doc.ref.update({ status: "CLOSED", updatedAt: now });
    await publishChange(a.clubId, "MATCH", a.matchId);
  }
  const due = await db().collectionGroup("announcements")
    .where("status", "==", "CLOSED").where("teamsPublishedAt", "==", null).where("drawAt", "<=", now).get();
  let published = 0;
  for (const doc of due.docs) {
    const a = doc.data() as AnnouncementDoc;
    try {
      if (await publishTeamsAtDrawTime(a.clubId, a.matchId)) published++;
    } catch (e) {
      logger.error("[jobs] draw failed", a.matchId, e);
    }
  }
  return { closed: expired.size, published };
}

/** Marks a pending notice in a transaction, so concurrent runs notify only once. */
async function claim(ref: FirebaseFirestore.DocumentReference, field: "openedNotifiedAt" | "closingReminderSentAt", now: Timestamp, extra?: (a: AnnouncementDoc) => boolean) {
  return db().runTransaction(async (tx) => {
    const a = (await tx.get(ref)).data() as AnnouncementDoc | undefined;
    if (!a || a[field] !== null || a.status !== "OPEN") return null;
    if (extra && !extra(a)) return null;
    tx.update(ref, { [field]: now });
    return a;
  });
}

async function context(a: AnnouncementDoc, type: "match.announcement.opened" | "match.announcement.closing_soon") {
  const [match, schedule, name] = await Promise.all([db().doc(paths.match(a.clubId, a.matchId)).get(), readSchedule(a.clubId), clubName(a.clubId)]);
  const matchAt = (match.data() as MatchDoc).scheduledAt.toDate();
  return { type, clubId: a.clubId, clubName: name, matchId: a.matchId, zone: schedule?.timeZone ?? DEFAULT_TIME_ZONE, matchAt };
}

/** Opening and closing-soon pushes (BE-005 RN-1/2/5, BE-008 RN-C3), each sent once per announcement. */
export async function runAnnouncementNotifications(at: Date = new Date()) {
  const now = Timestamp.fromDate(at);
  let sent = 0;
  const opened = await db().collectionGroup("announcements")
    .where("status", "==", "OPEN").where("openedNotifiedAt", "==", null).where("opensAt", "<=", now).get();
  for (const doc of opened.docs) {
    const a = await claim(doc.ref, "openedNotifiedAt", now);
    if (!a) continue;
    const ctx = await context(a, "match.announcement.opened");
    const absent = await absentMemberIds(a.clubId, ctx.matchAt);
    const recipients = (await activeMembers(a.clubId)).filter((m) => !absent.has(m.id)).map((m) => m.data.userId);
    await sendToUsers(messages.opened(ctx, recipients, a.matchId));
    sent++;
  }

  const lead = CLOSING_REMINDER_HOURS * 3600_000;
  const closing = await db().collectionGroup("announcements")
    .where("status", "==", "OPEN").where("closingReminderSentAt", "==", null).where("closesAt", ">", now).get();
  for (const doc of closing.docs) {
    const candidate = doc.data() as AnnouncementDoc;
    const reminderTime = candidate.closesAt.toMillis() - lead;
    if (at.getTime() < reminderTime) continue;
    // Opened within the reminder window: members just got the opening push (RN-2)
    if (candidate.opensAt.toMillis() > reminderTime) {
      await claim(doc.ref, "closingReminderSentAt", now);
      continue;
    }
    // Full: keep it pending, a place may still free up before closing
    const freePlacesOf = (a: AnnouncementDoc) => a.maxPlayers - a.entries.filter((e) => e.status === "CONFIRMED").length;
    const a = await claim(doc.ref, "closingReminderSentAt", now, (x) => freePlacesOf(x) > 0);
    if (!a) continue;
    const ctx = await context(a, "match.announcement.closing_soon");
    const absent = await absentMemberIds(a.clubId, ctx.matchAt);
    const enrolled = new Set(a.entries.map((e) => e.clubMemberId).filter(Boolean));
    const recipients = (await activeMembers(a.clubId)).filter((m) => !absent.has(m.id) && !enrolled.has(m.id)).map((m) => m.data.userId);
    await sendToUsers(messages.closingSoon(ctx, recipients, a.matchId, a.closesAt.toDate(), freePlacesOf(a)));
    sent++;
  }
  return sent + await runMatchReminders(at);
}

/** Two hours before kickoff, once per match, to the called-up members (spec 021 APP-RN-P3). */
export async function runMatchReminders(at: Date = new Date()) {
  const now = Timestamp.fromDate(at);
  const soon = await db().collectionGroup("matches")
    .where("status", "==", "SCHEDULED")
    .where("scheduledAt", ">", now)
    .where("scheduledAt", "<=", Timestamp.fromMillis(at.getTime() + MATCH_REMINDER_HOURS * 3600_000))
    .get();
  let sent = 0;
  for (const doc of soon.docs) {
    const claimed = await db().runTransaction(async (tx) => {
      const match = (await tx.get(doc.ref)).data() as MatchDoc | undefined;
      if (!match || match.reminderSentAt) return null;
      tx.update(doc.ref, { reminderSentAt: now });
      return match;
    });
    if (!claimed) continue;
    const announcement = (await db().doc(paths.announcement(claimed.clubId, doc.id)).get()).data() as AnnouncementDoc | undefined;
    const confirmed = (announcement?.entries ?? [])
      .filter((e) => e.status === "CONFIRMED" && e.participantType === "MEMBER" && e.clubMemberId)
      .map((e) => uidOfMember(e.clubMemberId as string));
    if (confirmed.length === 0) continue;
    const [schedule, name] = await Promise.all([readSchedule(claimed.clubId), clubName(claimed.clubId)]);
    await sendToUsers(messages.reminder({
      type: "match.reminder", clubId: claimed.clubId, clubName: name, matchId: doc.id,
      zone: schedule?.timeZone ?? DEFAULT_TIME_ZONE, matchAt: claimed.scheduledAt.toDate()
    }, confirmed));
    sent++;
  }
  return sent;
}

export const planUpcomingMatches = onSchedule({ schedule: "5 * * * *", timeZone: "UTC" }, async () => {
  logger.info("[jobs] planned", await runPlanning());
});

export const closeAnnouncementsAndDraw = onSchedule({ schedule: "*/5 * * * *", timeZone: "UTC" }, async () => {
  logger.info("[jobs] close and draw", await runCloseAndDraw());
});

export const sendAnnouncementNotifications = onSchedule({ schedule: "*/5 * * * *", timeZone: "UTC" }, async () => {
  logger.info("[jobs] announcement notices", await runAnnouncementNotifications());
});

/**
 * Emulator only: runs the jobs now (the emulator does not fire scheduled functions), optionally "at" another time to
 * try closes, draws and reminders without waiting. Refused outside the emulator.
 */
export const runJobsNow = onCall(async (request) => {
  if (!isEmulator()) throw new HttpsError("not-found", "Not available");
  const at = typeof request.data?.at === "string" ? new Date(request.data.at) : new Date();
  return {
    seasons: await runSeasonRollover(at),
    planned: await runPlanning(at),
    ...(await runCloseAndDraw(at)),
    notified: await runAnnouncementNotifications(at)
  };
});
