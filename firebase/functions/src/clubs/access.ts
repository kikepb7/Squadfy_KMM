import { DocumentSnapshot, Transaction } from "firebase-admin/firestore";
import { db } from "../admin";
import { isManager } from "../domain/clubRoles";
import { forbidden, notFound } from "../errors";
import { memberIdOf, paths } from "../paths";
import { ClubDoc, MemberDoc } from "./model";

export interface Membership {
  id: string;
  data: MemberDoc;
}

/** The club, or 404 like the own backend. */
export async function requireClub(clubId: string, tx?: Transaction): Promise<ClubDoc> {
  const ref = db().doc(paths.club(clubId));
  const snapshot: DocumentSnapshot = tx ? await tx.get(ref) : await ref.get();
  if (!snapshot.exists) notFound("Club not found");
  return snapshot.data() as ClubDoc;
}

/** The caller's active membership, or 403 NOT_CLUB_MEMBER (ClubAccessGuard.requireMember). */
export async function requireMember(clubId: string, uid: string, tx?: Transaction): Promise<Membership> {
  const id = memberIdOf(clubId, uid);
  const ref = db().doc(paths.member(clubId, id));
  const snapshot = tx ? await tx.get(ref) : await ref.get();
  const data = snapshot.data() as MemberDoc | undefined;
  if (!data || data.status !== "ACTIVE") forbidden("Not a member of this club", "NOT_CLUB_MEMBER");
  return { id, data: data as MemberDoc };
}

/** OWNER or ADMIN of the club, or 403 (ClubAccessGuard.requireManager). */
export async function requireManager(clubId: string, uid: string, tx?: Transaction): Promise<Membership> {
  const membership = await requireMember(clubId, uid, tx);
  if (!isManager(membership.data.role)) forbidden("Only the club managers can do this");
  return membership;
}

/** An active member of the club by id, or 404. */
export async function requireTargetMember(clubId: string, memberId: string, tx?: Transaction): Promise<Membership> {
  const ref = db().doc(paths.member(clubId, memberId));
  const snapshot = tx ? await tx.get(ref) : await ref.get();
  const data = snapshot.data() as MemberDoc | undefined;
  if (!data || data.status !== "ACTIVE") notFound("Member not found");
  return { id: memberId, data: data as MemberDoc };
}
