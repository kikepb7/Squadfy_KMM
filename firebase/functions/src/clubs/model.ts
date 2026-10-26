import { Timestamp } from "firebase-admin/firestore";
import { ClubMemberRole, PlayerPosition } from "../domain/types";

export type MemberStatus = "ACTIVE" | "LEFT" | "BANNED";

/** `clubs/{clubId}` */
export interface ClubDoc {
  name: string;
  description: string | null;
  logoUrl: string | null;
  ownerId: string;
  invitationCode: string;
  maxMembers: number | null;
  membersCount: number;
  createdAt: Timestamp;
}

/** `clubs/{clubId}/members/{memberId}`: the membership plus a copy of the public profile (name and picture). */
export interface MemberDoc {
  clubId: string;
  userId: string;
  username: string;
  profilePictureUrl: string | null;
  clubPictureUrl: string | null;
  shirtNumber: number | null;
  position: PlayerPosition | null;
  role: ClubMemberRole;
  status: MemberStatus;
  joinedAt: Timestamp;
  leftAt: Timestamp | null;
  bannedAt: Timestamp | null;
}

/** What the callables answer: the backend's ClubDto, so the app maps it like before. */
export const clubDto = (id: string, club: ClubDoc) => ({
  id,
  name: club.name,
  description: club.description,
  clubLogoUrl: club.logoUrl,
  ownerId: club.ownerId,
  invitationCode: club.invitationCode,
  maxMembers: club.maxMembers,
  membersCount: club.membersCount
});

export const memberDto = (id: string, member: MemberDoc) => ({
  id,
  clubId: member.clubId,
  userId: member.userId,
  username: member.username,
  profilePictureUrl: member.profilePictureUrl,
  clubPictureUrl: member.clubPictureUrl,
  shirtNumber: member.shirtNumber,
  position: member.position,
  role: member.role
});
