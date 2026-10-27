import { Timestamp, Transaction } from "firebase-admin/firestore";
import { logger } from "firebase-functions/v2";
import { onCall } from "firebase-functions/v2/https";
import { onSchedule } from "firebase-functions/v2/scheduler";
import { DateTime } from "luxon";
import { db } from "../admin";
import { requireManager, requireMember } from "../clubs/access";
import { INITIAL_RATING } from "../domain/rating";
import {
  drawRating, initialSeason, nextSeason, seasonBefore, SeasonDates, seasonIdOf, seasonNameOf, seasonOn, SeasonStatus,
  statusOn, validateSeasonDates
} from "../domain/season";
import { badRequest, conflict, notFound } from "../errors";
import { body, requireAuth, requireString } from "../guards";
import { publishChange } from "../live";
import { paths } from "../paths";
import { MemberDoc } from "../clubs/model";
import { clubMessages } from "../notifications/messages";
import { sendToUsers } from "../notifications/push";
import { MatchDoc, RatingDoc } from "./model";
import { zoneOf } from "./store";

/** Club seasons, their ratings and the yearly roll-over (spec 020 APP-RN-S1…S6). */

/** `clubs/{clubId}/seasons/{seasonId}`, id `2026-27`. Its ratings live in `ratings/{memberId}` under it. */
export interface SeasonDoc extends SeasonDates {
  clubId: string;
  name: string;
  status: SeasonStatus;
  createdAt: Timestamp;
  updatedAt: Timestamp;
}

export interface Season {
  id: string;
  data: SeasonDoc;
}

export const seasonDto = (s: Season) => ({
  id: s.id, name: s.data.name, startsOn: s.data.startsOn, endsOn: s.data.endsOn, status: s.data.status
});

export const localDate = (at: Date, zone: string) => DateTime.fromJSDate(at, { zone }).toISODate() as string;

const MAX_ROLLS = 5;

async function listSeasons(clubId: string, tx?: Transaction): Promise<Season[]> {
  const query = db().collection(paths.seasons(clubId));
  const snapshot = tx ? await tx.get(query) : await query.get();
  return snapshot.docs
    .map((d) => ({ id: d.id, data: d.data() as SeasonDoc }))
    .sort((a, b) => b.data.startYear - a.data.startYear);
}

/** Creates the season if it does not exist yet; concurrent calls create it once. */
async function createSeason(clubId: string, dates: SeasonDates, today: string): Promise<Season> {
  const id = seasonIdOf(dates.startYear);
  const now = Timestamp.now();
  const season: SeasonDoc = { clubId, ...dates, name: seasonNameOf(dates.startYear), status: statusOn(dates, today), createdAt: now, updatedAt: now };
  try {
    await db().doc(paths.season(clubId, id)).create(season);
    return { id, data: season };
  } catch (e) {
    if ((e as { code?: unknown }).code !== 6) throw e;
    return { id, data: (await db().doc(paths.season(clubId, id)).get()).data() as SeasonDoc };
  }
}

/** Ratings stored before seasons existed belong to the first one (spec 020, migration). */
async function migrateLegacyRatings(clubId: string, seasonId: string) {
  const legacy = await db().collection(paths.legacyRatings(clubId)).get();
  if (legacy.empty) return;
  const batch = db().batch();
  for (const doc of legacy.docs) {
    batch.set(db().doc(paths.rating(clubId, seasonId, doc.id)), doc.data());
    batch.delete(doc.ref);
  }
  await batch.commit();
  logger.info("[seasons] legacy ratings moved", clubId, seasonId, legacy.size);
}

/**
 * Brings the club's seasons up to date on [at] and returns them, newest first (APP-RN-S2, S5). Idempotent:
 * - a club without seasons gets its first one, with the ratings it had;
 * - each season's status follows its dates;
 * - when the latest one is closed, the next one is created with the same days.
 */
export async function ensureSeasons(clubId: string, at: Date = new Date()): Promise<Season[]> {
  const today = localDate(at, await zoneOf(clubId));
  let seasons = await listSeasons(clubId);
  if (seasons.length === 0) {
    const first = await createSeason(clubId, initialSeason(today), today);
    await migrateLegacyRatings(clubId, first.id);
    seasons = [first];
  }
  let changed = false;
  for (const season of seasons) {
    if (season.data.status === "CLOSED") continue;
    const status = statusOn(season.data, today);
    if (status !== season.data.status) {
      // In a transaction, so only the caller that makes the change announces it (spec 021 APP-RN-P5)
      const ref = db().doc(paths.season(clubId, season.id));
      const won = await db().runTransaction(async (tx) => {
        if ((await tx.get(ref)).get("status") !== season.data.status) return false;
        tx.update(ref, { status, updatedAt: Timestamp.now() });
        return true;
      });
      const previous = season.data.status;
      season.data = { ...season.data, status };
      changed = true;
      if (won) await announceSeason(clubId, season, previous);
    }
  }
  for (let i = 0; i < MAX_ROLLS && seasons[0].data.status === "CLOSED"; i++) {
    seasons = [await createSeason(clubId, nextSeason(seasons[0].data), today), ...seasons];
    changed = true;
  }
  if (changed) await publishChange(clubId, "SEASON");
  return seasons;
}

/** Spec 021 APP-RN-P5: a season that starts or closes is announced to the members; a closed one names its leader. */
async function announceSeason(clubId: string, season: Season, previous: SeasonStatus) {
  try {
    const started = previous === "UPCOMING" && season.data.status === "ACTIVE";
    const closed = season.data.status === "CLOSED";
    if (!started && !closed) return;
    const members = (await db().collection(paths.members(clubId)).where("status", "==", "ACTIVE").get()).docs
      .map((d) => ({ id: d.id, data: d.data() as MemberDoc }));
    const userIds = members.map((m) => m.data.userId);
    const name = ((await db().doc(paths.club(clubId)).get()).get("name") as string | undefined) ?? "";
    if (started) {
      await sendToUsers(clubMessages.seasonStarted(clubId, name, userIds, season.data.name));
      return;
    }
    const top = await db().collection(paths.ratings(clubId, season.id)).orderBy("rating", "desc").limit(5).get();
    const leaderId = top.docs.find((d) => ((d.data() as RatingDoc).matchesRated ?? 0) > 0)?.id;
    const leader = members.find((m) => m.id === leaderId)?.data.username ?? null;
    await sendToUsers(clubMessages.seasonClosed(clubId, name, userIds, season.data.name, leader));
  } catch (e) {
    logger.error("[push] season notice failed", clubId, season.id, e);
  }
}

/** The season the standings show by default: the one in progress, else the next one (pre-season), else the latest. */
export const currentSeason = (seasons: Season[]): Season =>
  seasons.find((s) => s.data.status === "ACTIVE") ?? seasons.find((s) => s.data.status === "UPCOMING") ?? seasons[0];

/** The requested season, or the current one when [seasonId] is missing; 404 for an unknown id. */
export async function resolveSeason(clubId: string, seasonId: unknown): Promise<Season> {
  const seasons = await ensureSeasons(clubId);
  if (seasonId === undefined || seasonId === null || seasonId === "") return currentSeason(seasons);
  const season = seasons.find((s) => s.id === seasonId);
  if (!season) notFound("Season not found");
  return season as Season;
}

/** The season a match played at [matchAt] counts in (null for a friendly, D-19) and the one before it. */
export async function seasonsAround(clubId: string, matchAt: Date, tx?: Transaction) {
  const zone = await zoneOf(clubId);
  const seasons = await listSeasons(clubId, tx);
  const date = localDate(matchAt, zone);
  const withDates = seasons.map((s) => ({ ...s, startsOn: s.data.startsOn, endsOn: s.data.endsOn }));
  return { current: seasonOn(withDates, date), previous: seasonBefore(withDates, date) };
}

const emptyRating = (memberId: string): RatingDoc => ({ clubMemberId: memberId, rating: INITIAL_RATING, matchesRated: 0 });

/** The members' ratings in [seasonId] (1000 and no matches for those without one). */
export async function seasonRatings(clubId: string, seasonId: string, memberIds: string[], tx?: Transaction): Promise<Map<string, RatingDoc>> {
  if (memberIds.length === 0) return new Map();
  const refs = memberIds.map((id) => db().doc(paths.rating(clubId, seasonId, id)));
  const docs = tx ? await tx.getAll(...refs) : await db().getAll(...refs);
  return new Map(memberIds.map((id, i) => [id, (docs[i].data() as RatingDoc | undefined) ?? emptyRating(id)]));
}

/** The ratings the draw and the team balance use for a match at [matchAt] (D-18). */
export async function drawRatings(clubId: string, matchAt: Date, memberIds: string[], tx?: Transaction): Promise<Map<string, number>> {
  const { current, previous } = await seasonsAround(clubId, matchAt, tx);
  const [now, before] = await Promise.all([
    current ? seasonRatings(clubId, current.id, memberIds, tx) : Promise.resolve(new Map<string, RatingDoc>()),
    previous ? seasonRatings(clubId, previous.id, memberIds, tx) : Promise.resolve(new Map<string, RatingDoc>())
  ]);
  return new Map(memberIds.map((id) => [id, drawRating(current ? now.get(id) ?? null : null, previous ? before.get(id) ?? null : null)]));
}

// region Callables

export const getSeasons = onCall(async (request) => {
  const uid = requireAuth(request);
  const clubId = requireString(body(request), "clubId");
  await requireMember(clubId, uid);
  return (await ensureSeasons(clubId)).map(seasonDto);
});

/**
 * Changes the days of the season in progress or the next one (APP-RN-S3): it starts in September and ends in June of
 * the next year. Closed seasons cannot change, nor can dates that would move a completed match to another season.
 */
export const updateSeason = onCall(async (request) => {
  const uid = requireAuth(request);
  const data = body(request);
  const clubId = requireString(data, "clubId");
  const seasonId = requireString(data, "seasonId");
  const startsOn = requireString(data, "startsOn", 10, 10);
  const endsOn = requireString(data, "endsOn", 10, 10);
  await requireManager(clubId, uid);
  const seasons = await ensureSeasons(clubId);
  const season = seasons.find((s) => s.id === seasonId);
  if (!season) notFound("Season not found");
  const current = (season as Season).data;
  if (current.status === "CLOSED") conflict("A closed season cannot change");
  const error = validateSeasonDates(current.startYear, startsOn, endsOn);
  if (error) badRequest(error, "VALIDATION_ERROR");

  const zone = await zoneOf(clubId);
  const completed = await db().collection(paths.matches(clubId)).where("status", "==", "COMPLETED").get();
  const inRange = (date: string, from: string, to: string) => date >= from && date <= to;
  const moved = completed.docs.some((d) => {
    const date = localDate((d.data() as MatchDoc).scheduledAt.toDate(), zone);
    return inRange(date, current.startsOn, current.endsOn) !== inRange(date, startsOn, endsOn);
  });
  if (moved) conflict("Completed matches would change season with these dates");

  const today = localDate(new Date(), zone);
  await db().doc(paths.season(clubId, seasonId)).update({ startsOn, endsOn, status: statusOn({ startsOn, endsOn }, today), updatedAt: Timestamp.now() });
  await publishChange(clubId, "SEASON");
  const updated = (await ensureSeasons(clubId)).find((s) => s.id === seasonId) as Season;
  return seasonDto(updated);
});

// endregion

// region Job

/** Daily: closes the finished seasons and opens the next ones of every club (APP-RN-S5). Safe to run twice. */
export async function runSeasonRollover(at: Date = new Date()) {
  const clubs = await db().collection("clubs").select().get();
  let updated = 0;
  for (const club of clubs.docs) {
    try {
      await ensureSeasons(club.id, at);
      updated++;
    } catch (e) {
      logger.error("[seasons] roll-over failed", club.id, e);
    }
  }
  return updated;
}

export const rollSeasons = onSchedule({ schedule: "15 0 * * *", timeZone: "UTC" }, async () => {
  logger.info("[seasons] clubs checked", await runSeasonRollover());
});

// endregion
