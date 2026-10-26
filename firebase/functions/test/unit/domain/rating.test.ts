import { describe, expect, it } from "vitest";
import { calculateDeltas, INITIAL_RATING, RatedPlayer, teamBalance } from "../../../src/domain/rating";
import { aggregateStats, CompletedMatch, emptyStats, rankRatings, rankStats, StatsPeriod } from "../../../src/domain/stats";

let id = 0;
const team = (size: number, rating = INITIAL_RATING, matches = 20): RatedPlayer[] =>
  Array.from({ length: size }, () => ({ memberId: `r${id++}`, rating, matchesRated: matches }));
const total = (m: Map<string, number>) => [...m.values()].reduce((s, v) => s + v, 0);

describe("PlayerRatingCalculator (ported from the backend, AC-019-11)", () => {
  it("winners gain and losers lose the same amount when nobody stands out", () => {
    const a = team(5); const b = team(5);
    const d = calculateDeltas({ teamA: a, teamB: b, goalsA: 3, goalsB: 1 });
    a.forEach((p) => expect(d.get(p.memberId)!).toBeGreaterThan(0));
    b.forEach((p) => expect(d.get(p.memberId)!).toBeLessThan(0));
    expect(total(d)).toBeCloseTo(0, 9);
  });

  it("a draw between equal teams does not change ratings", () => {
    const d = calculateDeltas({ teamA: team(7), teamB: team(7), goalsA: 2, goalsB: 2 });
    d.forEach((v) => expect(v).toBeCloseTo(0, 9));
  });

  it("an upset moves ratings more than an expected win", () => {
    const weak = team(5, 900); const strong = team(5, 1100);
    const upset = calculateDeltas({ teamA: weak, teamB: strong, goalsA: 2, goalsB: 1 });
    const expected = calculateDeltas({ teamA: strong, teamB: weak, goalsA: 2, goalsB: 1 });
    expect(upset.get(weak[0].memberId)!).toBeGreaterThan(expected.get(strong[0].memberId)!);
  });

  it("a bigger goal margin moves ratings more", () => {
    const a = team(5); const b = team(5);
    const narrow = calculateDeltas({ teamA: a, teamB: b, goalsA: 1, goalsB: 0 });
    const thrashing = calculateDeltas({ teamA: a, teamB: b, goalsA: 6, goalsB: 0 });
    expect(thrashing.get(a[0].memberId)!).toBeGreaterThan(narrow.get(a[0].memberId)!);
  });

  it("rewards individual performance without inflating the total", () => {
    const scorer: RatedPlayer = { memberId: "scorer", rating: INITIAL_RATING, matchesRated: 20, goals: 3, assists: 1 };
    const mates = team(4);
    const d = calculateDeltas({ teamA: [scorer, ...mates], teamB: team(5), goalsA: 1, goalsB: 1 });
    expect(d.get("scorer")!).toBeGreaterThan(d.get(mates[0].memberId)!);
    expect(Math.abs(total(d))).toBeLessThan(1e-9);
  });

  it("moves new players faster than established ones", () => {
    const rookie: RatedPlayer = { memberId: "rookie", rating: INITIAL_RATING, matchesRated: 0 };
    const veteran: RatedPlayer = { memberId: "veteran", rating: INITIAL_RATING, matchesRated: 30 };
    const d = calculateDeltas({ teamA: [rookie, veteran], teamB: team(2), goalsA: 2, goalsB: 0 });
    expect(d.get("rookie")!).toBeGreaterThan(d.get("veteran")!);
  });

  it("counts guests in the team average with the initial rating but gives them no delta", () => {
    const strong: RatedPlayer = { memberId: "strong", rating: 1300, matchesRated: 20 };
    const rival: RatedPlayer = { memberId: "rival", rating: 1300, matchesRated: 20 };
    const without = calculateDeltas({ teamA: [strong], teamB: [rival], goalsA: 1, goalsB: 0 });
    const withGuest = calculateDeltas({ teamA: [strong], teamB: [rival], goalsA: 1, goalsB: 0, guestsB: 1 });
    expect([...withGuest.keys()].sort()).toEqual(["rival", "strong"]);
    expect(withGuest.get("strong")!).toBeLessThan(without.get("strong")!);
    const onlyGuests = calculateDeltas({ teamA: [], teamB: [rival], goalsA: 2, goalsB: 0, guestsA: 2 });
    expect([...onlyGuests.keys()]).toEqual(["rival"]);
    expect(onlyGuests.get("rival")!).toBeLessThan(0);
  });

  it("team balance compares averages and gives the Elo expectation", () => {
    const balance = teamBalance(
      [{ clubMemberId: "a1", rating: 1100, isGuest: false }, { clubMemberId: "a2", rating: 900, isGuest: false }],
      [{ clubMemberId: "b1", rating: 1000, isGuest: true }]
    );
    expect(balance.teamA.averageRating).toBe(1000);
    expect(balance.teamAExpectedScore).toBeCloseTo(0.5, 9);
    expect(balance.teamA.playerRatings[0].clubMemberId).toBe("a1");
  });
});

describe("Rating and stats classifications (ported from the backend, AC-019-12)", () => {
  it("ranks the highest rating first and ties share the position", () => {
    const ranked = rankRatings([
      { clubMemberId: "worst", rating: 950, matchesRated: 10 },
      { clubMemberId: "tiedA", rating: 1000, matchesRated: 10 },
      { clubMemberId: "best", rating: 1100, matchesRated: 10 },
      { clubMemberId: "tiedB", rating: 1000, matchesRated: 10 }
    ]);
    expect(ranked.map((r) => r.rank)).toEqual([1, 2, 2, 4]);
    expect(ranked[0].clubMemberId).toBe("best");
    expect(ranked[3].clubMemberId).toBe("worst");
  });

  it("ties ratings that round to the same value", () => {
    const ranked = rankRatings([{ clubMemberId: "a", rating: 1000.2, matchesRated: 10 }, { clubMemberId: "b", rating: 999.8, matchesRated: 10 }, { clubMemberId: "c", rating: 998, matchesRated: 10 }]);
    expect(ranked.map((r) => r.rank)).toEqual([1, 1, 3]);
  });

  it("marks players without enough matches as provisional, after tied veterans", () => {
    const ranked = rankRatings([{ clubMemberId: "rookie", rating: 1000, matchesRated: 0 }, { clubMemberId: "veteran", rating: 1000, matchesRated: 25 }]);
    expect(ranked[0].clubMemberId).toBe("veteran");
    expect(ranked[1].isProvisional).toBe(true);
    expect(ranked.map((r) => r.rank)).toEqual([1, 1]);
    expect(rankRatings([])).toEqual([]);
  });

  it("accumulates results, events and minutes per player", () => {
    const won: CompletedMatch = {
      teamA: new Map([["striker", 60], ["keeper", 60]]), teamB: new Map([["rival", 45]]),
      events: [{ clubMemberId: "striker", type: "GOAL" }, { clubMemberId: "striker", type: "GOAL" }, { clubMemberId: "keeper", type: "ASSIST" }, { clubMemberId: "rival", type: "YELLOW_CARD" }]
    };
    const lost: CompletedMatch = {
      teamA: new Map([["striker", 30]]), teamB: new Map([["rival", 60], ["keeper", 60]]),
      events: [{ clubMemberId: "rival", type: "GOAL" }, { clubMemberId: "striker", type: "RED_CARD" }]
    };
    const drawn: CompletedMatch = { teamA: new Map([["rival", 60]]), teamB: new Map([["keeper", 60]]), events: [] };
    const stats = aggregateStats([won, lost, drawn]);
    expect(stats.get("striker")).toEqual({ ...emptyStats("striker"), matchesPlayed: 2, wins: 1, losses: 1, goals: 2, redCards: 1, minutesPlayed: 90 });
    expect(stats.get("keeper")).toEqual({ ...emptyStats("keeper"), matchesPlayed: 3, wins: 2, draws: 1, assists: 1, minutesPlayed: 180 });
    expect(stats.get("rival")).toEqual({ ...emptyStats("rival"), matchesPlayed: 3, wins: 1, draws: 1, losses: 1, goals: 1, yellowCards: 1, minutesPlayed: 165 });
    expect(aggregateStats([]).size).toBe(0);
  });

  it("orders by the chosen metric and ties share the position", () => {
    const top = { ...emptyStats("top"), goals: 5, wins: 1 };
    const tiedMoreWins = { ...emptyStats("tiedMore"), goals: 3, wins: 4 };
    const tiedFewerWins = { ...emptyStats("tiedFewer"), goals: 3, wins: 2 };
    const newcomer = emptyStats("newcomer");
    const byGoals = rankStats([newcomer, tiedFewerWins, top, tiedMoreWins], "GOALS");
    expect(byGoals.map((r) => r.stats)).toEqual([top, tiedMoreWins, tiedFewerWins, newcomer]);
    expect(byGoals.map((r) => r.rank)).toEqual([1, 2, 2, 4]);
    expect(rankStats([newcomer, tiedFewerWins, top, tiedMoreWins], "WINS")[0].stats).toEqual(tiedMoreWins);
  });

  it("lets the manual score decide the result while goals still count for the scorer", () => {
    const stats = aggregateStats([{ teamA: new Map([["striker", 60]]), teamB: new Map([["rival", 60]]), events: [{ clubMemberId: "rival", type: "GOAL" }], officialScoreA: 3, officialScoreB: 2 }]);
    expect(stats.get("striker")!.wins).toBe(1);
    expect(stats.get("rival")!.losses).toBe(1);
    expect(stats.get("rival")!.goals).toBe(1);
  });

  it("uses inclusive local dates of the club for the period", () => {
    const september = new StatsPeriod("2026-09-01", "2026-09-30");
    expect(september.contains(new Date("2026-09-30T21:30:00Z"), "Europe/Madrid")).toBe(true);
    expect(september.contains(new Date("2026-09-30T22:30:00Z"), "Europe/Madrid")).toBe(false);
    expect(september.contains(new Date("2026-08-31T22:00:00Z"), "Europe/Madrid")).toBe(true);
    expect(StatsPeriod.ALL.contains(new Date("2000-01-01T00:00:00Z"), "Europe/Madrid")).toBe(true);
    expect(new StatsPeriod("2026-01-01", null).contains(new Date("2030-01-01T00:00:00Z"), "Europe/Madrid")).toBe(true);
    expect(() => new StatsPeriod("2026-02-01", "2026-01-01")).toThrow();
  });
});
