import { ClubMemberRole } from "./types";

/**
 * Who can act on whom inside a club (BE-001 RN-9):
 * - OWNER acts on anyone else and can assign ADMIN, CAPTAIN or PLAYER.
 * - ADMIN only acts on CAPTAIN/PLAYER and only toggles between those two roles.
 * - OWNER is never assigned through a role change (ownership is transferred).
 */
const MANAGED_BY_ADMIN: ClubMemberRole[] = ["CAPTAIN", "PLAYER"];

export function canRemove(actor: ClubMemberRole, target: ClubMemberRole): boolean {
  if (actor === "OWNER") return target !== "OWNER";
  if (actor === "ADMIN") return MANAGED_BY_ADMIN.includes(target);
  return false;
}

export function canAssign(actor: ClubMemberRole, target: ClubMemberRole, newRole: ClubMemberRole): boolean {
  if (newRole === "OWNER") return false;
  if (actor === "OWNER") return target !== "OWNER";
  if (actor === "ADMIN") return MANAGED_BY_ADMIN.includes(target) && MANAGED_BY_ADMIN.includes(newRole);
  return false;
}

export const isManager = (role: ClubMemberRole | undefined): boolean => role === "OWNER" || role === "ADMIN";

export interface SuccessionCandidate {
  memberId: string;
  role: ClubMemberRole;
  joinedAt: number;
}

/**
 * Who inherits a club when its owner deletes their account (BE-010 RN-A6): the oldest active ADMIN, otherwise the
 * oldest active member; null when nobody else is left (the club is deleted).
 */
export function successor(owner: string, activeMembers: SuccessionCandidate[]): string | null {
  const others = activeMembers
    .filter((m) => m.memberId !== owner)
    .sort((a, b) => a.joinedAt - b.joinedAt || (a.memberId < b.memberId ? -1 : 1));
  return (others.find((m) => m.role === "ADMIN") ?? others[0])?.memberId ?? null;
}
