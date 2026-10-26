import { Timestamp } from "firebase-admin/firestore";
import { DateTime } from "luxon";
import { onCall } from "firebase-functions/v2/https";
import { db } from "../admin";
import { requireMember } from "../clubs/access";
import { badRequest, notFound } from "../errors";
import { body, requireAuth, requireString } from "../guards";
import { publishChange } from "../live";
import { paths } from "../paths";
import { withdrawForAbsence } from "./announcements";
import { AbsenceDoc, absenceDto } from "./model";
import { zoneOf } from "./store";

/** Periods in which a member will not play (BE-008 RN-C; spec 019 AC-019-09). Dates are local dates of the club. */

const MAX_ABSENCE_DAYS = 366;
const isDate = (value: unknown): value is string => typeof value === "string" && DateTime.fromISO(value).isValid && value.length === 10;

/** Absences overlapping [from, to] (every absence when both are null), visible to members (RN-C5). */
export const getAbsences = onCall(async (request) => {
  const uid = requireAuth(request);
  const data = body(request);
  const clubId = requireString(data, "clubId");
  await requireMember(clubId, uid);
  const from = isDate(data.from) ? data.from : null;
  const to = isDate(data.to) ? data.to : null;
  let query = db().collection(paths.absences(clubId)).orderBy("fromDate");
  if (to) query = query.where("fromDate", "<=", to);
  const snapshot = await query.get();
  return snapshot.docs
    .map((d) => ({ id: d.id, data: d.data() as AbsenceDoc }))
    .filter((a) => !from || a.data.toDate >= from)
    .map((a) => absenceDto(a.id, a.data));
});

/** Registers my absence and withdraws me from the open announcements it covers (RN-C2). */
export const createAbsence = onCall(async (request) => {
  const uid = requireAuth(request);
  const data = body(request);
  const clubId = requireString(data, "clubId");
  const me = await requireMember(clubId, uid);
  if (!isDate(data.fromDate) || !isDate(data.toDate)) badRequest("Invalid dates", "VALIDATION_ERROR");
  const fromDate = data.fromDate as string;
  const toDate = data.toDate as string;
  const zone = await zoneOf(clubId);
  const today = DateTime.now().setZone(zone).toISODate() as string;
  if (toDate < fromDate) badRequest("toDate cannot be before fromDate");
  if (toDate < today) badRequest("The absence must end today or later");
  if (DateTime.fromISO(toDate).diff(DateTime.fromISO(fromDate), "days").days >= MAX_ABSENCE_DAYS) {
    badRequest(`An absence can last at most ${MAX_ABSENCE_DAYS} days`);
  }
  const reason = typeof data.reason === "string" && data.reason.trim() ? data.reason.trim().slice(0, 200) : null;
  const absence: AbsenceDoc = { clubId, clubMemberId: me.id, fromDate, toDate, reason, createdAt: Timestamp.now() };
  const ref = await db().collection(paths.absences(clubId)).add(absence);

  const start = DateTime.fromISO(fromDate, { zone }).startOf("day");
  const end = DateTime.fromISO(toDate, { zone }).plus({ days: 1 }).startOf("day");
  const matches = await db().collection(paths.matches(clubId))
    .where("status", "==", "SCHEDULED")
    .where("scheduledAt", ">=", Timestamp.fromDate(start.toJSDate()))
    .where("scheduledAt", "<", Timestamp.fromDate(end.toJSDate()))
    .get();
  if (!matches.empty) await withdrawForAbsence(clubId, me.id, new Set(matches.docs.map((d) => d.id)));
  await publishChange(clubId, "ABSENCES");
  return absenceDto(ref.id, absence);
});

/** A member deletes their own absences only. */
export const deleteAbsence = onCall(async (request) => {
  const uid = requireAuth(request);
  const data = body(request);
  const clubId = requireString(data, "clubId");
  const absenceId = requireString(data, "absenceId");
  const me = await requireMember(clubId, uid);
  const ref = db().doc(`${paths.absences(clubId)}/${absenceId}`);
  const absence = (await ref.get()).data() as AbsenceDoc | undefined;
  if (!absence || absence.clubMemberId !== me.id) notFound("Member absence not found");
  await ref.delete();
  await publishChange(clubId, "ABSENCES");
  return { ok: true };
});

/** Members absent on the local date of a match: no opening or reminder push for them (RN-C3). */
export async function absentMemberIds(clubId: string, matchAt: Date): Promise<Set<string>> {
  const date = DateTime.fromJSDate(matchAt, { zone: await zoneOf(clubId) }).toISODate() as string;
  const snapshot = await db().collection(paths.absences(clubId)).where("fromDate", "<=", date).get();
  return new Set(snapshot.docs.map((d) => d.data() as AbsenceDoc).filter((a) => a.toDate >= date).map((a) => a.clubMemberId));
}
