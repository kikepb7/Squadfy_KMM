import { onCall } from "firebase-functions/v2/https";
import { db } from "../admin";
import { requireMember } from "../clubs/access";
import { INITIAL_RATING } from "../domain/rating";
import { aggregateStats, CompletedMatch, emptyStats, PlayerStats, rankRatings, rankStats, STATS_SORT, StatsPeriod, StatsSortBy } from "../domain/stats";
import { badRequest } from "../errors";
import { body, requireAuth, requireString } from "../guards";
import { paths } from "../paths";
import { MatchDoc, RatingDoc, scoreOf } from "./model";
import { activeMembers, zoneOf } from "./store";

/** Rating classification and statistics (BE-003 RN-11, BE-004 RN-7/8, BE-012 RN-B; spec 019 AC-019-12). */

async function ratingEntries(clubId: string) {
  const [members, ratings] = await Promise.all([activeMembers(clubId), db().collection(paths.ratings(clubId)).get()]);
  const stored = new Map(ratings.docs.map((d) => [d.id, d.data() as RatingDoc]));
  return rankRatings(members.map((m) => ({
    clubMemberId: m.id,
    rating: stored.get(m.id)?.rating ?? INITIAL_RATING,
    matchesRated: stored.get(m.id)?.matchesRated ?? 0
  })));
}

const ratingDto = (e: Awaited<ReturnType<typeof ratingEntries>>[number]) => ({
  rank: e.rank, clubMemberId: e.clubMemberId, rating: Math.round(e.rating), matchesRated: e.matchesRated, isProvisional: e.isProvisional
});

export const getRatings = onCall(async (request) => {
  const uid = requireAuth(request);
  const clubId = requireString(body(request), "clubId");
  await requireMember(clubId, uid);
  return (await ratingEntries(clubId)).map(ratingDto);
});

export const getMyRating = onCall(async (request) => {
  const uid = requireAuth(request);
  const clubId = requireString(body(request), "clubId");
  const me = await requireMember(clubId, uid);
  const entries = await ratingEntries(clubId);
  const mine = entries.find((e) => e.clubMemberId === me.id)!;
  return { clubId, ...ratingDto(mine), totalPlayers: entries.length };
});

/** Statistics derived from the completed matches within [period], never stored as counters. */
async function statsByMember(clubId: string, period: StatsPeriod): Promise<Map<string, PlayerStats>> {
  const zone = await zoneOf(clubId);
  const snapshot = await db().collection(paths.matches(clubId)).where("status", "==", "COMPLETED").get();
  const matches = snapshot.docs.map((d) => d.data() as MatchDoc).filter((m) => period.contains(m.scheduledAt.toDate(), zone));
  return aggregateStats(matches.map((m): CompletedMatch => {
    const team = (players: MatchDoc["teamA"]) =>
      new Map(players.filter((p) => !p.isGuest).map((p) => [p.participantId, p.minutesPlayed ?? m.durationMinutes]));
    const score = scoreOf(m);
    return {
      teamA: team(m.teamA),
      teamB: team(m.teamB),
      events: m.events.map((e) => ({ clubMemberId: e.clubMemberId, type: e.type })),
      officialScoreA: score.isManualScore ? score.teamAScore : null,
      officialScoreB: score.isManualScore ? score.teamBScore : null
    };
  }));
}

function periodOf(data: Record<string, unknown>): StatsPeriod {
  const date = (value: unknown) => (typeof value === "string" && /^\d{4}-\d{2}-\d{2}$/.test(value) ? value : null);
  try {
    return new StatsPeriod(date(data.from), date(data.to));
  } catch {
    return badRequest("from cannot be after to");
  }
}

/** Every active member ranked by [sortBy]; members who have not played appear with zeros. */
export const getStats = onCall(async (request) => {
  const uid = requireAuth(request);
  const data = body(request);
  const clubId = requireString(data, "clubId");
  await requireMember(clubId, uid);
  const sortBy = (typeof data.sortBy === "string" && data.sortBy in STATS_SORT ? data.sortBy : "GOALS") as StatsSortBy;
  const [stats, members] = await Promise.all([statsByMember(clubId, periodOf(data)), activeMembers(clubId)]);
  return rankStats(members.map((m) => stats.get(m.id) ?? emptyStats(m.id)), sortBy).map((r) => ({ rank: r.rank, ...r.stats }));
});

export const getMyStats = onCall(async (request) => {
  const uid = requireAuth(request);
  const data = body(request);
  const clubId = requireString(data, "clubId");
  const me = await requireMember(clubId, uid);
  return (await statsByMember(clubId, periodOf(data))).get(me.id) ?? emptyStats(me.id);
});
