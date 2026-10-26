import { RandomSource, shuffled } from "./random";
import { PlayerPosition } from "./types";

export interface PlayerProfile {
  /** Member id, or the guest entry id for guests. */
  memberId: string;
  position: PlayerPosition | null;
  rating: number;
}

export interface TeamAssignment {
  teamA: PlayerProfile[];
  teamB: PlayerProfile[];
}

const MAX_SWAP_ITERATIONS = 100;
const EPSILON = 1e-9;
const POSITION_DRAFT_ORDER: Array<PlayerPosition | null> = ["GOALKEEPER", "DEFENDER", "MIDFIELDER", "FORWARD", null];

const total = (team: PlayerProfile[]) => team.reduce((sum, p) => sum + p.rating, 0);

/**
 * Splits players into two teams that are as even as possible (backend TeamBalancer, ADR-0004/0005):
 * 1. Team sizes differ by at most one.
 * 2. Each position (goalkeepers first) is spread evenly between both teams.
 * 3. Total rating is balanced with a greedy draft followed by same-position swaps.
 * Players with equal rating are shuffled with [random], so repeated draws give different line-ups.
 */
export function balanceTeams(players: PlayerProfile[], random: RandomSource = Math.random): TeamAssignment {
  if (new Set(players.map((p) => p.memberId)).size !== players.length) throw new Error("Players must be unique");
  const teamA: PlayerProfile[] = [];
  const teamB: PlayerProfile[] = [];

  const groups = new Map<PlayerPosition | null, PlayerProfile[]>();
  for (const player of shuffled(players, random)) {
    const group = groups.get(player.position) ?? [];
    group.push(player);
    groups.set(player.position, group);
  }
  [...groups.entries()]
    .sort(([a], [b]) => POSITION_DRAFT_ORDER.indexOf(a) - POSITION_DRAFT_ORDER.indexOf(b))
    .forEach(([position, group]) => {
      // Stable sort keeps the shuffle among equal ratings
      [...group].sort((a, b) => b.rating - a.rating).forEach((player) => pickTeam(teamA, teamB, position).push(player));
    });

  improveBySwaps(teamA, teamB);
  return { teamA, teamB };
}

function pickTeam(teamA: PlayerProfile[], teamB: PlayerProfile[], position: PlayerPosition | null): PlayerProfile[] {
  const countA = teamA.filter((p) => p.position === position).length;
  const countB = teamB.filter((p) => p.position === position).length;
  if (countA !== countB) return countA < countB ? teamA : teamB;
  if (teamA.length !== teamB.length) return teamA.length < teamB.length ? teamA : teamB;
  return total(teamA) <= total(teamB) ? teamA : teamB;
}

/** Swaps same-position players while it reduces the rating gap; sizes and position spread are kept. */
function improveBySwaps(teamA: PlayerProfile[], teamB: PlayerProfile[]) {
  for (let iteration = 0; iteration < MAX_SWAP_ITERATIONS; iteration++) {
    const currentDiff = total(teamA) - total(teamB);
    let best: [number, number] | null = null;
    let bestDiff = Math.abs(currentDiff);
    for (let i = 0; i < teamA.length; i++) {
      for (let j = 0; j < teamB.length; j++) {
        if (teamA[i].position !== teamB[j].position) continue;
        const newDiff = Math.abs(currentDiff - 2 * (teamA[i].rating - teamB[j].rating));
        if (newDiff < bestDiff - EPSILON) {
          bestDiff = newDiff;
          best = [i, j];
        }
      }
    }
    if (!best) return;
    const [i, j] = best;
    [teamA[i], teamB[j]] = [teamB[j], teamA[i]];
  }
}
