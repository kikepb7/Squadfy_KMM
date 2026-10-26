import { randomUUID } from "node:crypto";
import { FieldValue, Timestamp, Transaction } from "firebase-admin/firestore";
import { onCall } from "firebase-functions/v2/https";
import { db } from "../admin";
import { requireManager, requireMember } from "../clubs/access";
import { MemberDoc } from "../clubs/model";
import { calculateDeltas, INITIAL_RATING, RatedPlayer, teamBalance } from "../domain/rating";
import { balanceTeams, PlayerProfile } from "../domain/teamBalancer";
import { MATCH_FORMATS, MatchEventType, MatchFormat, maxPlayersOf } from "../domain/types";
import { badRequest, conflict, notFound } from "../errors";
import { body, requireAuth, requireString } from "../guards";
import { publishChange } from "../live";
import { messages } from "../notifications/messages";
import { sendToUsers } from "../notifications/push";
import { paths } from "../paths";
import {
  AnnouncementDoc, clubIdOf, DEFAULT_DURATION_MINUTES, DEFAULT_TIME_ZONE, EntryDoc, inPriorityOrder, MatchDoc, matchDto,
  RatingDoc, scoreOf, TeamPlayer, uidOfMember
} from "./model";
import { cancelMatchInternal, createMatchWithAnnouncement } from "./planning";
import { clubName, findAnnouncement, matchRef, readMatch, readSchedule } from "./store";

/** Matches: teams, result and rating (BE-003, BE-004, BE-007, BE-008; spec 019 AC-019-11). */

const MAX_SCORE = 99;
const EVENT_TYPES: MatchEventType[] = ["GOAL", "ASSIST", "YELLOW_CARD", "RED_CARD"];

function clubOf(matchId: string): string {
  const clubId = clubIdOf(matchId);
  if (!clubId) notFound("Match not found");
  return clubId as string;
}

async function load(matchId: string) {
  const [match, announcement] = await Promise.all([readMatch(matchId), findAnnouncement(matchId)]);
  return matchDto(matchId, match, announcement);
}

async function loadAfter(clubId: string, matchId: string) {
  await publishChange(clubId, "MATCH", matchId);
  return load(matchId);
}

function ensureScheduled(match: MatchDoc, what: string) {
  if (match.status !== "SCHEDULED") conflict(`${what} can only be changed on scheduled matches (reopen a completed match first)`);
}

// region Reads

export const getMatch = onCall(async (request) => {
  const uid = requireAuth(request);
  const matchId = requireString(body(request), "matchId");
  await requireMember(clubOf(matchId), uid);
  return load(matchId);
});

/** Matches of a club, newest first, optionally filtered by status. */
export const getClubMatches = onCall(async (request) => {
  const uid = requireAuth(request);
  const data = body(request);
  const clubId = requireString(data, "clubId");
  await requireMember(clubId, uid);
  let query = db().collection(paths.matches(clubId)).orderBy("scheduledAt", "desc");
  if (typeof data.status === "string") query = db().collection(paths.matches(clubId)).where("status", "==", data.status).orderBy("scheduledAt", "desc");
  const [matches, announcements] = await Promise.all([query.get(), db().collection(paths.announcements(clubId)).get()]);
  const byMatch = new Map(announcements.docs.map((d) => [d.id, d.data() as AnnouncementDoc]));
  return matches.docs.map((d) => matchDto(d.id, d.data() as MatchDoc, byMatch.get(d.id) ?? null));
});

// endregion

// region Lifecycle

/** Extra match (BE-007 AC-007-08): managers, with the schedule's format and duration unless given. */
export const createExtraMatch = onCall(async (request) => {
  const uid = requireAuth(request);
  const data = body(request);
  const clubId = requireString(data, "clubId");
  await requireManager(clubId, uid);
  const scheduledAt = new Date(requireString(data, "scheduledAt"));
  if (Number.isNaN(scheduledAt.getTime())) badRequest("Invalid scheduledAt", "VALIDATION_ERROR");
  if (data.format != null && !(String(data.format) in MATCH_FORMATS)) badRequest("Invalid format", "VALIDATION_ERROR");
  const schedule = await readSchedule(clubId);
  const format = (data.format as MatchFormat | undefined) ?? schedule?.format ?? "ELEVEN_A_SIDE";
  const durationMinutes = Number.isInteger(data.durationMinutes) && (data.durationMinutes as number) > 0
    ? (data.durationMinutes as number)
    : schedule?.matchDurationMinutes ?? DEFAULT_DURATION_MINUTES;
  const matchId = await createMatchWithAnnouncement({ clubId, scheduledAt, maxPlayers: maxPlayersOf(format), durationMinutes });
  return load(matchId);
});

export const cancelMatch = onCall(async (request) => {
  const uid = requireAuth(request);
  const matchId = requireString(body(request), "matchId");
  const clubId = clubOf(matchId);
  await requireManager(clubId, uid);
  const match = await readMatch(matchId);
  if (match.status === "COMPLETED") conflict("A completed match cannot be cancelled");
  await cancelMatchInternal(clubId, matchId);
  return load(matchId);
});

async function ratingsOf(tx: Transaction, clubId: string, memberIds: string[]): Promise<Map<string, RatingDoc>> {
  if (memberIds.length === 0) return new Map();
  const docs = await tx.getAll(...memberIds.map((id) => db().doc(paths.rating(clubId, id))));
  return new Map(memberIds.map((id, i) => [id, (docs[i].data() as RatingDoc | undefined) ?? { clubMemberId: id, rating: INITIAL_RATING, matchesRated: 0 }]));
}

/** Closes the match with its score and updates the players' ratings (BE-003, BE-004). */
export const completeMatch = onCall(async (request) => {
  const uid = requireAuth(request);
  const matchId = requireString(body(request), "matchId");
  const clubId = clubOf(matchId);
  await requireManager(clubId, uid);
  await db().runTransaction(async (tx) => {
    const match = await readMatch(matchId, tx);
    if (match.status !== "SCHEDULED") conflict("Only scheduled matches can be completed");
    if (match.scheduledAt.toMillis() > Date.now()) conflict("The match has not started yet");
    if (match.teamA.length === 0 || match.teamB.length === 0) conflict("Teams must be generated before completing the match");
    const membersA = match.teamA.filter((p) => !p.isGuest).map((p) => p.participantId);
    const membersB = match.teamB.filter((p) => !p.isGuest).map((p) => p.participantId);
    const ratings = await ratingsOf(tx, clubId, [...membersA, ...membersB]);
    const rated = (memberId: string): RatedPlayer => {
      const own = match.events.filter((e) => e.clubMemberId === memberId);
      const count = (type: MatchEventType) => own.filter((e) => e.type === type).length;
      const r = ratings.get(memberId) as RatingDoc;
      return { memberId, rating: r.rating, matchesRated: r.matchesRated, goals: count("GOAL"), assists: count("ASSIST"), yellowCards: count("YELLOW_CARD"), redCards: count("RED_CARD") };
    };
    const score = scoreOf(match);
    const deltas = calculateDeltas({
      teamA: membersA.map(rated), teamB: membersB.map(rated), goalsA: score.teamAScore, goalsB: score.teamBScore,
      guestsA: match.teamA.length - membersA.length, guestsB: match.teamB.length - membersB.length
    });
    deltas.forEach((delta, memberId) => {
      const r = ratings.get(memberId) as RatingDoc;
      tx.set(db().doc(paths.rating(clubId, memberId)), { clubMemberId: memberId, rating: r.rating + delta, matchesRated: r.matchesRated + 1 });
    });
    tx.update(matchRef(matchId), { status: "COMPLETED", ratingChanges: Object.fromEntries(deltas), updatedAt: FieldValue.serverTimestamp() });
  });
  return loadAfter(clubId, matchId);
});

/** Reopens the club's latest completed match to fix it; its rating changes are reverted. */
export const reopenMatch = onCall(async (request) => {
  const uid = requireAuth(request);
  const matchId = requireString(body(request), "matchId");
  const clubId = clubOf(matchId);
  await requireManager(clubId, uid);
  await db().runTransaction(async (tx) => {
    const latest = await tx.get(
      db().collection(paths.matches(clubId)).where("status", "==", "COMPLETED").orderBy("scheduledAt", "desc").limit(1)
    );
    if (latest.empty || latest.docs[0].id !== matchId) conflict("Only the latest completed match can be reopened");
    const match = latest.docs[0].data() as MatchDoc;
    const changes = Object.entries(match.ratingChanges ?? {});
    const ratings = await ratingsOf(tx, clubId, changes.map(([id]) => id));
    for (const [memberId, delta] of changes) {
      const r = ratings.get(memberId) as RatingDoc;
      tx.set(db().doc(paths.rating(clubId, memberId)), { clubMemberId: memberId, rating: r.rating - delta, matchesRated: Math.max(0, r.matchesRated - 1) });
    }
    tx.update(matchRef(matchId), { status: "SCHEDULED", ratingChanges: {}, updatedAt: FieldValue.serverTimestamp() });
  });
  return loadAfter(clubId, matchId);
});

// endregion

// region Teams

/** Confirmed members and guests of the match: the only players that can be in its teams (BE-008 RN-A6). */
function participantsOf(announcement: AnnouncementDoc | null) {
  const confirmed = announcement ? inPriorityOrder(announcement.entries, "CONFIRMED") : [];
  const members = confirmed.filter((e) => e.participantType === "MEMBER").map((e) => e.clubMemberId as string);
  const guests = confirmed.filter((e) => e.participantType === "GUEST");
  return { members, guests, ids: [...members, ...guests.map((g) => g.id)], guestIds: new Set(guests.map((g) => g.id)) };
}

type Participants = ReturnType<typeof participantsOf>;

async function autoTeams(tx: Transaction, clubId: string, participants: Participants): Promise<[string[], string[]]> {
  const memberDocs = participants.members.length
    ? await tx.getAll(...participants.members.map((id) => db().doc(paths.member(clubId, id))))
    : [];
  const ratings = await ratingsOf(tx, clubId, participants.members);
  const players: PlayerProfile[] = [
    ...participants.members.map((id, i) => ({
      memberId: id,
      position: ((memberDocs[i].data() as MemberDoc | undefined)?.position) ?? null,
      rating: (ratings.get(id) as RatingDoc).rating
    })),
    ...participants.guests.map((g: EntryDoc) => ({ memberId: g.id, position: g.guestPosition, rating: INITIAL_RATING }))
  ];
  if (players.length < 2) badRequest("At least 2 enrolled players are required for AUTO mode");
  const { teamA, teamB } = balanceTeams(players);
  return [teamA.map((p) => p.memberId), teamB.map((p) => p.memberId)];
}

function manualTeams(teamA: unknown, teamB: unknown, participants: Participants): [string[], string[]] {
  const a = [...new Set(Array.isArray(teamA) ? (teamA as string[]) : [])];
  const b = [...new Set(Array.isArray(teamB) ? (teamB as string[]) : [])];
  if (a.length === 0 || b.length === 0) badRequest("Manual mode requires non-empty teamA and teamB");
  if (a.some((id) => b.includes(id))) badRequest("A player cannot be in both teams");
  if (Math.abs(a.length - b.length) > 1) badRequest("Team sizes must be balanced (difference ≤ 1)");
  if (![...a, ...b].every((id) => participants.ids.includes(id))) badRequest("Only confirmed players and guests can be assigned to a team");
  return [a, b];
}

/** Keeps the minutes of the players that stay in their team. */
function toTeam(ids: string[], guestIds: Set<string>, previous: TeamPlayer[]): TeamPlayer[] {
  return ids.map((id) => ({
    participantId: id,
    isGuest: guestIds.has(id),
    minutesPlayed: previous.find((p) => p.participantId === id)?.minutesPlayed ?? null
  }));
}

async function teamsPublishedPush(clubId: string, matchId: string, match: MatchDoc, teamA: string[], teamB: string[], guestIds: Set<string>) {
  const schedule = await readSchedule(clubId);
  const ctx = {
    type: "match.teams.published" as const, clubId, clubName: await clubName(clubId), matchId,
    zone: schedule?.timeZone ?? DEFAULT_TIME_ZONE, matchAt: match.scheduledAt.toDate()
  };
  const users = (ids: string[]) => ids.filter((id) => !guestIds.has(id)).map(uidOfMember);
  for (const push of messages.teams(ctx, users(teamA), users(teamB))) await sendToUsers(push);
}

/** Draws (AUTO) or sets (MANUAL) the teams; managers, scheduled matches only. */
export const generateTeams = onCall(async (request) => {
  const uid = requireAuth(request);
  const data = body(request);
  const matchId = requireString(data, "matchId");
  const clubId = clubOf(matchId);
  await requireManager(clubId, uid);
  const mode = data.mode === "MANUAL" ? "MANUAL" : data.mode === "AUTO" ? "AUTO" : badRequest("Invalid mode", "VALIDATION_ERROR");
  const result = await db().runTransaction(async (tx) => {
    const match = await readMatch(matchId, tx);
    if (match.status !== "SCHEDULED") badRequest("Teams can only be generated for scheduled matches");
    const announcement = await findAnnouncement(matchId, tx);
    const participants = participantsOf(announcement);
    const [teamA, teamB] = mode === "AUTO" ? await autoTeams(tx, clubId, participants) : manualTeams(data.manualTeamA, data.manualTeamB, participants);
    tx.update(matchRef(matchId), {
      teamA: toTeam(teamA, participants.guestIds, match.teamA),
      teamB: toTeam(teamB, participants.guestIds, match.teamB),
      updatedAt: FieldValue.serverTimestamp()
    });
    return { match, teamA, teamB, guestIds: participants.guestIds };
  });
  await teamsPublishedPush(clubId, matchId, result.match, result.teamA, result.teamB, result.guestIds);
  return loadAfter(clubId, matchId);
});

/**
 * Publishes the teams at the draw time (BE-003 RN-9, BE-008 RN-D3): teams a manager already drew with exactly the
 * confirmed participants are kept, otherwise a balanced draw is made. Marked as published either way.
 */
export async function publishTeamsAtDrawTime(clubId: string, matchId: string): Promise<boolean> {
  const result = await db().runTransaction(async (tx) => {
    const match = await readMatch(matchId, tx);
    const announcement = await findAnnouncement(matchId, tx);
    if (!announcement || announcement.teamsPublishedAt) return null;
    const markPublished = () => tx.update(db().doc(paths.announcement(clubId, matchId)), { teamsPublishedAt: Timestamp.now() });
    if (match.status !== "SCHEDULED") { markPublished(); return null; }
    const participants = participantsOf(announcement);
    if (participants.ids.length < 2) { markPublished(); return null; }
    const current = new Set([...match.teamA, ...match.teamB].map((p) => p.participantId));
    if (current.size === participants.ids.length && participants.ids.every((id) => current.has(id))) { markPublished(); return null; }
    const [teamA, teamB] = await autoTeams(tx, clubId, participants);
    markPublished();
    tx.update(matchRef(matchId), {
      teamA: toTeam(teamA, participants.guestIds, match.teamA), teamB: toTeam(teamB, participants.guestIds, match.teamB),
      updatedAt: FieldValue.serverTimestamp()
    });
    return { match, teamA, teamB, guestIds: participants.guestIds };
  });
  if (!result) return false;
  await publishChange(clubId, "MATCH", matchId);
  await teamsPublishedPush(clubId, matchId, result.match, result.teamA, result.teamB, result.guestIds);
  return true;
}

/** Strength of each team from the current ratings, managers only (BE-003 RN-10). */
export const getTeamBalance = onCall(async (request) => {
  const uid = requireAuth(request);
  const matchId = requireString(body(request), "matchId");
  const clubId = clubOf(matchId);
  await requireManager(clubId, uid);
  const match = await readMatch(matchId);
  if (match.teamA.length === 0 || match.teamB.length === 0) conflict("Teams have not been generated for this match yet");
  const memberIds = [...match.teamA, ...match.teamB].filter((p) => !p.isGuest).map((p) => p.participantId);
  const docs = memberIds.length ? await db().getAll(...memberIds.map((id) => db().doc(paths.rating(clubId, id)))) : [];
  const ratingOf = new Map(memberIds.map((id, i) => [id, (docs[i].data() as RatingDoc | undefined)?.rating ?? INITIAL_RATING]));
  const strength = (team: TeamPlayer[]) => team.map((p) => ({
    clubMemberId: p.participantId, rating: p.isGuest ? INITIAL_RATING : (ratingOf.get(p.participantId) as number), isGuest: p.isGuest
  }));
  const balance = teamBalance(strength(match.teamA), strength(match.teamB));
  const dto = (s: typeof balance.teamA) => ({
    players: s.players, averageRating: Math.round(s.averageRating), totalRating: Math.round(s.totalRating),
    playerRatings: s.playerRatings.map((p) => ({ clubMemberId: p.clubMemberId, rating: Math.round(p.rating), isGuest: p.isGuest }))
  });
  return {
    matchId,
    teamA: dto(balance.teamA),
    teamB: dto(balance.teamB),
    averageRatingDifference: Math.round(balance.averageRatingDifference),
    teamAExpectedScore: Math.round(balance.teamAExpectedScore * 100) / 100
  };
});

// endregion

// region Result: events, minutes and manual score

async function editMatch(request: { auth?: { uid: string } | null; data: unknown }, change: (match: MatchDoc, data: Record<string, unknown>) => Partial<MatchDoc>) {
  const uid = requireAuth(request as never);
  const data = body(request as never);
  const matchId = requireString(data, "matchId");
  const clubId = clubOf(matchId);
  await requireManager(clubId, uid);
  await db().runTransaction(async (tx) => {
    const match = await readMatch(matchId, tx);
    tx.update(matchRef(matchId), { ...change(match, data), updatedAt: FieldValue.serverTimestamp() });
  });
  return loadAfter(clubId, matchId);
}

export const addMatchEvent = onCall((request) => editMatch(request, (match, data) => {
  ensureScheduled(match, "Events");
  const clubMemberId = requireString(data, "clubMemberId");
  const type = data.type as MatchEventType;
  if (!EVENT_TYPES.includes(type)) badRequest("Invalid event type", "VALIDATION_ERROR");
  const minute = data.minute == null ? null : Number(data.minute);
  if (minute !== null && (!Number.isInteger(minute) || minute < 0 || minute > 200)) badRequest("Invalid minute", "VALIDATION_ERROR");
  if (![...match.teamA, ...match.teamB].some((p) => !p.isGuest && p.participantId === clubMemberId)) {
    badRequest("The player is not assigned to any team in this match");
  }
  return { events: [...match.events, { id: randomUUID(), clubMemberId, type, minute, createdAt: Timestamp.now() }] };
}));

export const deleteMatchEvent = onCall((request) => editMatch(request, (match, data) => {
  ensureScheduled(match, "Events");
  const eventId = requireString(data, "eventId");
  if (!match.events.some((e) => e.id === eventId)) notFound("Match event not found");
  return { events: match.events.filter((e) => e.id !== eventId) };
}));

/** Minutes a player actually played (BE-004 RN-6); only before the match is completed. */
export const setPlayerMinutes = onCall((request) => editMatch(request, (match, data) => {
  ensureScheduled(match, "Minutes");
  const clubMemberId = requireString(data, "clubMemberId");
  const minutes = Number(data.minutes);
  if (!Number.isInteger(minutes) || minutes < 0 || minutes > match.durationMinutes) badRequest(`Minutes must be between 0 and ${match.durationMinutes}`);
  const update = (team: TeamPlayer[]) => team.map((p) => (!p.isGuest && p.participantId === clubMemberId ? { ...p, minutesPlayed: minutes } : p));
  if (![...match.teamA, ...match.teamB].some((p) => !p.isGuest && p.participantId === clubMemberId)) {
    badRequest("The player is not assigned to any team in this match");
  }
  return { teamA: update(match.teamA), teamB: update(match.teamB) };
}));

/** Official final score set by a manager (BE-008 RN-E1/E2); only while the match is scheduled. */
export const setMatchScore = onCall((request) => editMatch(request, (match, data) => {
  ensureScheduled(match, "The score");
  const a = Number(data.teamAScore);
  const b = Number(data.teamBScore);
  if (![a, b].every((s) => Number.isInteger(s) && s >= 0 && s <= MAX_SCORE)) badRequest(`Scores must be between 0 and ${MAX_SCORE}`);
  return { manualTeamAScore: a, manualTeamBScore: b };
}));

/** Back to the score given by the goal events (BE-008 RN-E4). */
export const clearMatchScore = onCall((request) => editMatch(request, (match) => {
  ensureScheduled(match, "The score");
  return { manualTeamAScore: null, manualTeamBScore: null };
}));

// endregion
