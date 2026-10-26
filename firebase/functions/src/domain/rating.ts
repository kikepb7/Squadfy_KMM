/**
 * Elo-style skill rating learned from every completed match (backend PlayerRatingCalculator, BE-003):
 * - Team component: each player moves by K · M · (S − E), where E is the expected result from the average rating of
 *   both teams, S the actual result (1 / 0.5 / 0) and M grows with the goal margin.
 * - Individual component: zero-sum within the match, rewards goals and assists and penalises cards relative to the
 *   match average, so ratings do not inflate over time.
 * - New players move faster (provisional K) until their rating stabilises.
 * - Guests (BE-008 RN-A6) count in the team average with [INITIAL_RATING] but get no delta.
 */
export const INITIAL_RATING = 1000;
export const PROVISIONAL_MATCHES = 10;
const ELO_SCALE = 400;
const BASE_K = 20;
const PROVISIONAL_EXTRA_K = 20;
const INDIVIDUAL_WEIGHT = 6;
const MAX_INDIVIDUAL_DELTA = 15;

export interface RatedPlayer {
  memberId: string;
  rating: number;
  matchesRated: number;
  goals?: number;
  assists?: number;
  yellowCards?: number;
  redCards?: number;
}

export const expectedScore = (ratingA: number, ratingB: number): number =>
  1 / (1 + Math.pow(10, (ratingB - ratingA) / ELO_SCALE));

const teamAverage = (members: RatedPlayer[], guests: number) =>
  (members.reduce((sum, p) => sum + p.rating, 0) + guests * INITIAL_RATING) / (members.length + guests);

const kFactor = (player: RatedPlayer) =>
  BASE_K + PROVISIONAL_EXTRA_K * Math.max(0, 1 - player.matchesRated / PROVISIONAL_MATCHES);

const performance = (p: RatedPlayer) =>
  (p.goals ?? 0) + 0.75 * (p.assists ?? 0) - 0.5 * (p.yellowCards ?? 0) - 2 * (p.redCards ?? 0);

export function calculateDeltas(params: {
  teamA: RatedPlayer[];
  teamB: RatedPlayer[];
  goalsA: number;
  goalsB: number;
  guestsA?: number;
  guestsB?: number;
}): Map<string, number> {
  const { teamA, teamB, goalsA, goalsB } = params;
  const guestsA = params.guestsA ?? 0;
  const guestsB = params.guestsB ?? 0;
  if (teamA.length + guestsA === 0 || teamB.length + guestsB === 0) throw new Error("Both teams need players");
  if (teamA.length === 0 && teamB.length === 0) return new Map();

  const expectedA = expectedScore(teamAverage(teamA, guestsA), teamAverage(teamB, guestsB));
  const actualA = goalsA > goalsB ? 1 : goalsA < goalsB ? 0 : 0.5;
  const margin = 1 + Math.log(1 + Math.abs(goalsA - goalsB)) / 2;
  const swingA = margin * (actualA - expectedA);
  const individual = individualDeltas([...teamA, ...teamB]);

  const deltas = new Map<string, number>();
  teamA.forEach((p) => deltas.set(p.memberId, kFactor(p) * swingA + (individual.get(p.memberId) ?? 0)));
  teamB.forEach((p) => deltas.set(p.memberId, -kFactor(p) * swingA + (individual.get(p.memberId) ?? 0)));
  return deltas;
}

/** Capped and then re-centred, so the individual component always sums to zero. */
function individualDeltas(players: RatedPlayer[]): Map<string, number> {
  const average = players.reduce((sum, p) => sum + performance(p), 0) / players.length;
  const capped = players.map((p): [string, number] => [
    p.memberId,
    Math.min(MAX_INDIVIDUAL_DELTA, Math.max(-MAX_INDIVIDUAL_DELTA, INDIVIDUAL_WEIGHT * (performance(p) - average)))
  ]);
  const drift = capped.reduce((sum, [, d]) => sum + d, 0) / capped.length;
  return new Map(capped.map(([id, d]) => [id, d - drift]));
}

/** How even two teams are (BE-003 RN-10): averages, because sizes may differ by one player. */
export interface PlayerRating {
  clubMemberId: string;
  rating: number;
  isGuest: boolean;
}

export function teamBalance(teamA: PlayerRating[], teamB: PlayerRating[]) {
  if (teamA.length === 0 || teamB.length === 0) throw new Error("Both teams need players");
  const strength = (team: PlayerRating[]) => {
    const sorted = [...team].sort((a, b) => b.rating - a.rating);
    const totalRating = sorted.reduce((sum, p) => sum + p.rating, 0);
    return { players: sorted.length, averageRating: totalRating / sorted.length, totalRating, playerRatings: sorted };
  };
  const a = strength(teamA);
  const b = strength(teamB);
  return {
    teamA: a,
    teamB: b,
    averageRatingDifference: a.averageRating - b.averageRating,
    teamAExpectedScore: expectedScore(a.averageRating, b.averageRating)
  };
}
