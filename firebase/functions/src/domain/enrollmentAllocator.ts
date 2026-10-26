import { EntryStatus } from "./types";

/**
 * Assigns confirmed places of an announcement (BE-008 RN-A2, RN-A3): members first in enrollment order, then guests in
 * the order they were added. The first `maxPlayers` are confirmed, the rest waitlisted, so a member joining a full
 * announcement pushes the last confirmed guest to the waitlist.
 */
export interface Participant {
  entryId: string;
  isGuest: boolean;
  enrolledAt: number;
}

export function allocate(participants: Participant[], maxPlayers: number): Map<string, EntryStatus> {
  const sorted = [...participants].sort(
    (a, b) =>
      Number(a.isGuest) - Number(b.isGuest) ||
      a.enrolledAt - b.enrolledAt ||
      (a.entryId < b.entryId ? -1 : a.entryId > b.entryId ? 1 : 0)
  );
  return new Map(sorted.map((p, index) => [p.entryId, index < maxPlayers ? "CONFIRMED" : "WAITLISTED"]));
}
