import { DateTime } from "luxon";
import { MatchEventType } from "./types";
import { PROVISIONAL_MATCHES } from "./rating";

/** Statistics of a member over the club's completed matches (BE-004 RN-7). */
export interface PlayerStats {
  clubMemberId: string;
  matchesPlayed: number;
  wins: number;
  draws: number;
  losses: number;
  goals: number;
  assists: number;
  yellowCards: number;
  redCards: number;
  minutesPlayed: number;
}

export const emptyStats = (clubMemberId: string): PlayerStats => ({
  clubMemberId, matchesPlayed: 0, wins: 0, draws: 0, losses: 0, goals: 0, assists: 0, yellowCards: 0, redCards: 0, minutesPlayed: 0
});

export interface CompletedMatch {
  /** Team A players and the minutes each one played. */
  teamA: Map<string, number>;
  teamB: Map<string, number>;
  events: Array<{ clubMemberId: string; type: MatchEventType }>;
  /** Manual official score (BE-008 RN-E2); when null the score is the goals of [events]. */
  officialScoreA?: number | null;
  officialScoreB?: number | null;
}

/**
 * Derives statistics from completed matches instead of storing counters, so reopening and completing a match again
 * always leaves consistent figures (BE-004 RN-7).
 */
export function aggregateStats(matches: CompletedMatch[]): Map<string, PlayerStats> {
  const stats = new Map<string, PlayerStats>();
  for (const match of matches) {
    const goalsIn = (team: Map<string, number>) =>
      match.events.filter((e) => e.type === "GOAL" && team.has(e.clubMemberId)).length;
    const goalsA = match.officialScoreA ?? goalsIn(match.teamA);
    const goalsB = match.officialScoreB ?? goalsIn(match.teamB);

    const accumulate = (team: Map<string, number>, scored: number, conceded: number) => {
      team.forEach((minutes, memberId) => {
        const own = match.events.filter((e) => e.clubMemberId === memberId);
        const count = (type: MatchEventType) => own.filter((e) => e.type === type).length;
        const current = stats.get(memberId) ?? emptyStats(memberId);
        stats.set(memberId, {
          ...current,
          matchesPlayed: current.matchesPlayed + 1,
          wins: current.wins + (scored > conceded ? 1 : 0),
          draws: current.draws + (scored === conceded ? 1 : 0),
          losses: current.losses + (scored < conceded ? 1 : 0),
          goals: current.goals + count("GOAL"),
          assists: current.assists + count("ASSIST"),
          yellowCards: current.yellowCards + count("YELLOW_CARD"),
          redCards: current.redCards + count("RED_CARD"),
          minutesPlayed: current.minutesPlayed + minutes
        });
      });
    };
    accumulate(match.teamA, goalsA, goalsB);
    accumulate(match.teamB, goalsB, goalsA);
  }
  return stats;
}

export const STATS_SORT = {
  GOALS: (s: PlayerStats) => s.goals,
  ASSISTS: (s: PlayerStats) => s.assists,
  MATCHES: (s: PlayerStats) => s.matchesPlayed,
  MINUTES: (s: PlayerStats) => s.minutesPlayed,
  WINS: (s: PlayerStats) => s.wins
} as const;
export type StatsSortBy = keyof typeof STATS_SORT;

const byId = (a: string, b: string) => (a < b ? -1 : a > b ? 1 : 0);

/** Highest value first; ties share the position (1, 2, 2, 4) and are listed by wins, goals and id (BE-004 RN-8). */
export function rankStats(stats: PlayerStats[], sortBy: StatsSortBy): Array<{ rank: number; stats: PlayerStats }> {
  const metric = STATS_SORT[sortBy];
  const sorted = [...stats].sort(
    (a, b) => metric(b) - metric(a) || b.wins - a.wins || b.goals - a.goals || byId(a.clubMemberId, b.clubMemberId)
  );
  const ranked: Array<{ rank: number; stats: PlayerStats }> = [];
  sorted.forEach((s, index) => {
    const previous = ranked[ranked.length - 1];
    ranked.push({ rank: previous && metric(previous.stats) === metric(s) ? previous.rank : index + 1, stats: s });
  });
  return ranked;
}

export interface RatingEntry {
  clubMemberId: string;
  rating: number;
  matchesRated: number;
}

/**
 * Club classification by rating (BE-003 RN-11): highest first; ties share the position comparing the rounded rating
 * users see, listed by matches played and then id.
 */
export function rankRatings(entries: RatingEntry[]) {
  const sorted = [...entries].sort(
    (a, b) => b.rating - a.rating || b.matchesRated - a.matchesRated || byId(a.clubMemberId, b.clubMemberId)
  );
  const ranked: Array<RatingEntry & { rank: number; isProvisional: boolean }> = [];
  sorted.forEach((entry, index) => {
    const previous = ranked[ranked.length - 1];
    const rank = previous && Math.round(previous.rating) === Math.round(entry.rating) ? previous.rank : index + 1;
    ranked.push({ ...entry, rank, isProvisional: entry.matchesRated < PROVISIONAL_MATCHES });
  });
  return ranked;
}

/** Optional date range of the statistics (BE-012 RN-B1): local dates of the club, both inclusive. */
export class StatsPeriod {
  constructor(readonly from: string | null, readonly to: string | null) {
    if (from && to && from > to) throw new Error("from cannot be after to");
  }

  contains(instant: Date, zone: string): boolean {
    const date = DateTime.fromJSDate(instant, { zone }).toISODate() as string;
    return (!this.from || date >= this.from) && (!this.to || date <= this.to);
  }

  static readonly ALL = new StatsPeriod(null, null);
}
