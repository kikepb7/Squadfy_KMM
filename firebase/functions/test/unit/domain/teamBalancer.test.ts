import { describe, expect, it } from "vitest";
import { balanceTeams, PlayerProfile } from "../../../src/domain/teamBalancer";
import { seededRandom } from "../../../src/domain/random";
import { INITIAL_RATING } from "../../../src/domain/rating";
import { PlayerPosition } from "../../../src/domain/types";

let id = 0;
const player = (position: PlayerPosition | null, rating = INITIAL_RATING): PlayerProfile => ({ memberId: `p${id++}`, position, rating });
const sum = (team: PlayerProfile[]) => team.reduce((s, p) => s + p.rating, 0);

describe("TeamBalancer (ported from the backend, AC-019-11)", () => {
  it.each([2, 3, 7, 10, 11, 14, 22])("team sizes differ by at most one and every player is assigned once (%i)", (size) => {
    const positions: Array<PlayerPosition | null> = ["GOALKEEPER", "DEFENDER", "MIDFIELDER", "FORWARD", null];
    const players = Array.from({ length: size }, (_, i) => player(positions[i % positions.length], i % 7));
    const { teamA, teamB } = balanceTeams(players, seededRandom(size));
    expect(Math.abs(teamA.length - teamB.length)).toBeLessThanOrEqual(1);
    expect(new Set([...teamA, ...teamB].map((p) => p.memberId))).toEqual(new Set(players.map((p) => p.memberId)));
    expect(teamA.length + teamB.length).toBe(size);
  });

  it("splits the goalkeepers between both teams", () => {
    const players = [player("GOALKEEPER", 9), player("GOALKEEPER", 3), ...Array.from({ length: 8 }, () => player("MIDFIELDER"))];
    const { teamA, teamB } = balanceTeams(players, seededRandom(1));
    expect(teamA.filter((p) => p.position === "GOALKEEPER")).toHaveLength(1);
    expect(teamB.filter((p) => p.position === "GOALKEEPER")).toHaveLength(1);
  });

  it("spreads every position evenly", () => {
    const players = [
      ...Array.from({ length: 4 }, () => player("DEFENDER")),
      ...Array.from({ length: 4 }, () => player("MIDFIELDER")),
      ...Array.from({ length: 2 }, () => player("FORWARD"))
    ];
    const { teamA, teamB } = balanceTeams(players, seededRandom(7));
    (["DEFENDER", "MIDFIELDER", "FORWARD"] as const).forEach((pos) =>
      expect(teamA.filter((p) => p.position === pos).length).toBe(teamB.filter((p) => p.position === pos).length));
  });

  it("ends with balanced ratings", () => {
    const players = [9.5, 9.0, 8.0, 7.5, 6.0, 5.5, 5.0, 4.0, 3.0, 2.0].map((r) => player("MIDFIELDER", r));
    const { teamA, teamB } = balanceTeams(players, seededRandom(3));
    expect(Math.abs(sum(teamA) - sum(teamB))).toBeLessThanOrEqual(0.5);
  });

  it("does not put all the strongest players in the same team", () => {
    const players = [...Array.from({ length: 4 }, () => player("FORWARD", 10)), ...Array.from({ length: 4 }, () => player("FORWARD", 2))];
    const { teamA, teamB } = balanceTeams(players, seededRandom(11));
    expect(teamA.filter((p) => p.rating === 10)).toHaveLength(2);
    expect(teamB.filter((p) => p.rating === 10)).toHaveLength(2);
  });

  it("gives the same draw with the same seed and can change it with another", () => {
    const players = Array.from({ length: 12 }, (_, i) => player((["DEFENDER", "MIDFIELDER", "FORWARD"] as const)[i % 3]));
    const first = balanceTeams(players, seededRandom(42));
    const second = balanceTeams(players, seededRandom(42));
    expect(first).toEqual(second);
    const draws = new Set(Array.from({ length: 20 }, (_, s) => balanceTeams(players, seededRandom(s + 1)).teamA.map((p) => p.memberId).sort().join()));
    expect(draws.size).toBeGreaterThan(1);
  });

  it("rejects repeated players", () => {
    const p = player("FORWARD");
    expect(() => balanceTeams([p, p])).toThrow();
  });
});
