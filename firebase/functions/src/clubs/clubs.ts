import { randomInt } from "node:crypto";
import { logger } from "firebase-functions/v2";
import { FieldValue, Timestamp, Transaction } from "firebase-admin/firestore";
import { onCall } from "firebase-functions/v2/https";
import { db } from "../admin";
import { canAssign, canRemove } from "../domain/clubRoles";
import { ClubMemberRole } from "../domain/types";
import { badRequest, conflict, fail, forbidden, notFound } from "../errors";
import { body, requireAuth, requireString } from "../guards";
import { memberIdOf, paths } from "../paths";
import { requireClub, requireManager, requireMember, requireTargetMember } from "./access";
import { AfterCommit, membershipEnded, runAfterCommit } from "./hooks";
import { ClubDoc, clubDto, MemberDoc, memberDto } from "./model";
import { ensureSeasons } from "../match/seasons";
import { clubMessages } from "../notifications/messages";
import { sendToUsers } from "../notifications/push";

/** Pushes are sent after the transaction commits; a failed push never undoes the change (spec 021). */
async function notify(work: () => Promise<void>) {
  try {
    await work();
  } catch (e) {
    logger.error("[push] club notice failed", e);
  }
}

async function managerIds(clubId: string): Promise<string[]> {
  const snapshot = await db().collection(paths.members(clubId)).where("status", "==", "ACTIVE").get();
  return snapshot.docs.map((d) => d.data() as MemberDoc).filter((m) => m.role === "OWNER" || m.role === "ADMIN").map((m) => m.userId);
}
import * as validate from "./validation";

/**
 * Clubs and membership (BE-001 / spec 003, spec 019 AC-019-07). Every write runs in a transaction and checks the
 * caller's role in that club; the rules only let members read.
 */

const CODE_ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
const CODE_LENGTH = 8;
const ROLES: ClubMemberRole[] = ["OWNER", "ADMIN", "CAPTAIN", "PLAYER"];

const randomCode = () => Array.from({ length: CODE_LENGTH }, () => CODE_ALPHABET[randomInt(CODE_ALPHABET.length)]).join("");

/** Reserves a code no other club uses (the backend retries until it is unique). */
async function reserveInvitationCode(tx: Transaction, clubId: string): Promise<string> {
  for (let attempt = 0; attempt < 10; attempt++) {
    const code = randomCode();
    const ref = db().doc(paths.invitationCode(code));
    if (!(await tx.get(ref)).exists) {
      tx.set(ref, { clubId });
      return code;
    }
  }
  throw new Error("Could not generate a unique invitation code");
}

/**
 * The caller's public profile. A deleted account has none, so its still-valid ID token cannot create clubs or
 * memberships (the own backend revoked its access tokens instead): 401, as with an invalid token.
 */
async function profileOf(tx: Transaction, uid: string) {
  const user = await tx.get(db().doc(paths.user(uid)));
  if (!user.exists) fail(401, "INVALID_TOKEN", "The account no longer exists");
  return {
    username: (user.get("username") as string | undefined) ?? "",
    pictureUrl: (user.get("pictureUrl") as string | null | undefined) ?? null
  };
}

export const createClub = onCall(async (request) => {
  const uid = requireAuth(request);
  const data = body(request);
  const name = validate.clubName(data.name);
  const description = validate.clubDescription(data.description);
  const maxMembers = validate.maxMembers(data.maxMembers);

  const clubRef = db().collection("clubs").doc();
  const club = await db().runTransaction(async (tx) => {
    const profile = await profileOf(tx, uid);
    const invitationCode = await reserveInvitationCode(tx, clubRef.id);
    const now = Timestamp.now();
    const club: ClubDoc = {
      name, description, logoUrl: null, ownerId: uid, invitationCode, maxMembers, membersCount: 1, createdAt: now
    };
    const owner: MemberDoc = {
      clubId: clubRef.id, userId: uid, username: profile.username, profilePictureUrl: profile.pictureUrl,
      clubPictureUrl: null, shirtNumber: null, position: null, role: "OWNER", status: "ACTIVE",
      joinedAt: now, leftAt: null, bannedAt: null
    };
    tx.set(clubRef, club);
    tx.set(db().doc(paths.member(clubRef.id, memberIdOf(clubRef.id, uid))), owner);
    return club;
  });
  // Spec 020 APP-RN-S2: the club starts with its season
  await ensureSeasons(clubRef.id);
  return clubDto(clubRef.id, club);
});

/** Joins with an invitation code; a former member gets the same membership back as PLAYER (BE-001 RN-12). */
export const joinClub = onCall(async (request) => {
  const uid = requireAuth(request);
  const data = body(request);
  const code = validate.invitationCode(data.invitationCode);
  const shirtNumber = validate.shirtNumber(data.shirtNumber);
  const position = validate.position(data.position);

  const joined = await db().runTransaction(async (tx) => {
    const codeDoc = await tx.get(db().doc(paths.invitationCode(code)));
    if (!codeDoc.exists) notFound("Invalid invitation code", "INVALID_INVITATION_CODE");
    const clubId = codeDoc.get("clubId") as string;
    const clubRef = db().doc(paths.club(clubId));
    const club = (await tx.get(clubRef)).data() as ClubDoc | undefined;
    if (!club || club.invitationCode !== code) notFound("Invalid invitation code", "INVALID_INVITATION_CODE");

    const memberId = memberIdOf(clubId, uid);
    const memberRef = db().doc(paths.member(clubId, memberId));
    const previous = (await tx.get(memberRef)).data() as MemberDoc | undefined;
    if (previous?.status === "BANNED") forbidden("You are banned from this club", "BANNED_FROM_CLUB");
    if (previous?.status === "ACTIVE") conflict("You are already a member of this club");
    if (club!.maxMembers !== null && club!.membersCount >= club!.maxMembers) conflict("The club is full");

    const profile = await profileOf(tx, uid);
    const now = Timestamp.now();
    const member: MemberDoc = {
      clubId, userId: uid, username: profile.username, profilePictureUrl: profile.pictureUrl,
      clubPictureUrl: previous?.clubPictureUrl ?? null,
      shirtNumber: shirtNumber ?? previous?.shirtNumber ?? null,
      position: position ?? previous?.position ?? null,
      role: "PLAYER", status: "ACTIVE",
      joinedAt: previous?.joinedAt ?? now, leftAt: null, bannedAt: null
    };
    tx.set(memberRef, member);
    tx.update(clubRef, { membersCount: FieldValue.increment(1) });
    return { clubId, name: club!.name, username: profile.username, dto: clubDto(clubId, { ...club!, membersCount: club!.membersCount + 1 }) };
  });
  // APP-RN-P6: the managers learn who joined
  await notify(async () => sendToUsers(clubMessages.memberJoined(joined.clubId, joined.name, await managerIds(joined.clubId), joined.username)));
  return joined.dto;
});

/** Edits the name, description and member limit (managers, BE-001 RN-13). */
export const updateClub = onCall(async (request) => {
  const uid = requireAuth(request);
  const data = body(request);
  const clubId = requireString(data, "clubId");
  return db().runTransaction(async (tx) => {
    const club = await requireClub(clubId, tx);
    await requireManager(clubId, uid, tx);
    const changes: Partial<ClubDoc> = {};
    if (data.name !== undefined) changes.name = validate.clubName(data.name);
    if (data.description !== undefined) changes.description = validate.clubDescription(data.description);
    if (data.maxMembers !== undefined) {
      changes.maxMembers = validate.maxMembers(data.maxMembers);
      if (changes.maxMembers !== null && changes.maxMembers < club.membersCount) {
        badRequest("Max members cannot be lower than the current members", "BAD_REQUEST");
      }
    }
    tx.update(db().doc(paths.club(clubId)), changes);
    return clubDto(clubId, { ...club, ...changes });
  });
});

/** The logo was uploaded by the app to `clubs/{clubId}/logo.jpg` (Storage rules: managers only). */
export const setClubLogo = onCall(async (request) => {
  const uid = requireAuth(request);
  const data = body(request);
  const clubId = requireString(data, "clubId");
  const logoUrl = validate.imageUrl(data.logoUrl);
  return db().runTransaction(async (tx) => {
    const club = await requireClub(clubId, tx);
    await requireManager(clubId, uid, tx);
    tx.update(db().doc(paths.club(clubId)), { logoUrl });
    return clubDto(clubId, { ...club, logoUrl });
  });
});

export const regenerateInvitationCode = onCall(async (request) => {
  const uid = requireAuth(request);
  const clubId = requireString(body(request), "clubId");
  return db().runTransaction(async (tx) => {
    const club = await requireClub(clubId, tx);
    await requireManager(clubId, uid, tx);
    const invitationCode = await reserveInvitationCode(tx, clubId);
    tx.delete(db().doc(paths.invitationCode(club.invitationCode)));
    tx.update(db().doc(paths.club(clubId)), { invitationCode });
    return { invitationCode };
  });
});

/** Only the owner; the new owner must be an active member and the former owner becomes ADMIN (BE-001). */
export const transferOwnership = onCall(async (request) => {
  const uid = requireAuth(request);
  const data = body(request);
  const clubId = requireString(data, "clubId");
  const memberId = requireString(data, "memberId");
  const result = await db().runTransaction(async (tx) => {
    const club = await requireClub(clubId, tx);
    const me = await requireMember(clubId, uid, tx);
    if (me.data.role !== "OWNER") forbidden("Only the owner can transfer the club");
    if (memberId === me.id) badRequest("You already own this club");
    const target = await requireTargetMember(clubId, memberId, tx);
    tx.update(db().doc(paths.member(clubId, me.id)), { role: "ADMIN" });
    tx.update(db().doc(paths.member(clubId, memberId)), { role: "OWNER" });
    tx.update(db().doc(paths.club(clubId)), { ownerId: target.data.userId });
    return { userId: target.data.userId, name: club.name, dto: clubDto(clubId, { ...club, ownerId: target.data.userId }) };
  });
  await notify(() => sendToUsers(clubMessages.ownershipTransferred(clubId, result.name, result.userId)));
  return result.dto;
});

export const updateMyMembership = onCall(async (request) => {
  const uid = requireAuth(request);
  const data = body(request);
  const clubId = requireString(data, "clubId");
  return db().runTransaction(async (tx) => {
    const me = await requireMember(clubId, uid, tx);
    const changes: Partial<MemberDoc> = {};
    if (data.shirtNumber !== undefined) changes.shirtNumber = validate.shirtNumber(data.shirtNumber);
    if (data.position !== undefined) changes.position = validate.position(data.position);
    tx.update(db().doc(paths.member(clubId, me.id)), changes);
    return memberDto(me.id, { ...me.data, ...changes });
  });
});

/** The member's own picture for this club (BE-012 RN-C1), uploaded by the app first; null goes back to the profile one. */
export const setMyClubPicture = onCall(async (request) => {
  const uid = requireAuth(request);
  const data = body(request);
  const clubId = requireString(data, "clubId");
  const clubPictureUrl = validate.imageUrl(data.pictureUrl ?? null);
  return db().runTransaction(async (tx) => {
    const me = await requireMember(clubId, uid, tx);
    tx.update(db().doc(paths.member(clubId, me.id)), { clubPictureUrl });
    return memberDto(me.id, { ...me.data, clubPictureUrl });
  });
});

/** The owner cannot leave: they transfer the club first (BE-001). */
export const leaveClub = onCall(async (request) => {
  const uid = requireAuth(request);
  const clubId = requireString(body(request), "clubId");
  const after = await db().runTransaction(async (tx) => {
    const me = await requireMember(clubId, uid, tx);
    if (me.data.role === "OWNER") conflict("The owner cannot leave the club; transfer it first");
    const actions = await membershipEnded(tx, clubId, me.id);
    tx.update(db().doc(paths.member(clubId, me.id)), { status: "LEFT", leftAt: Timestamp.now(), role: "PLAYER" });
    tx.update(db().doc(paths.club(clubId)), { membersCount: FieldValue.increment(-1) });
    return actions;
  });
  await runAfterCommit(after);
  return { ok: true };
});

async function endMembership(request: { auth?: { uid: string } | null; data: unknown }, ban: boolean) {
  const uid = requireAuth(request as never);
  const data = body(request as never);
  const clubId = requireString(data, "clubId");
  const memberId = requireString(data, "memberId");
  const after: AfterCommit[] = await db().runTransaction(async (tx) => {
    const me = await requireManager(clubId, uid, tx);
    if (memberId === me.id) badRequest("You cannot remove yourself; leave the club instead");
    const target = await requireTargetMember(clubId, memberId, tx);
    if (!canRemove(me.data.role, target.data.role)) forbidden("You cannot remove this member");
    const actions = await membershipEnded(tx, clubId, memberId);
    const now = Timestamp.now();
    tx.update(
      db().doc(paths.member(clubId, memberId)),
      ban ? { status: "BANNED", bannedAt: now, leftAt: now, role: "PLAYER" } : { status: "LEFT", leftAt: now, role: "PLAYER" }
    );
    tx.update(db().doc(paths.club(clubId)), { membersCount: FieldValue.increment(-1) });
    return actions;
  });
  await runAfterCommit(after);
  return { ok: true };
}

export const removeMember = onCall((request) => endMembership(request, false));

/** A banned member cannot join again with the code until a manager lifts the ban (BE-001 RN-10). */
export const banMember = onCall((request) => endMembership(request, true));

export const unbanMember = onCall(async (request) => {
  const uid = requireAuth(request);
  const data = body(request);
  const clubId = requireString(data, "clubId");
  const memberId = requireString(data, "memberId");
  await db().runTransaction(async (tx) => {
    await requireManager(clubId, uid, tx);
    const ref = db().doc(paths.member(clubId, memberId));
    const target = (await tx.get(ref)).data() as MemberDoc | undefined;
    if (!target || target.status !== "BANNED") notFound("This member is not banned");
    tx.update(ref, { status: "LEFT", bannedAt: null });
  });
  return { ok: true };
});

export const changeMemberRole = onCall(async (request) => {
  const uid = requireAuth(request);
  const data = body(request);
  const clubId = requireString(data, "clubId");
  const memberId = requireString(data, "memberId");
  const role = data.role as ClubMemberRole;
  if (!ROLES.includes(role)) badRequest("Invalid role", "VALIDATION_ERROR");
  const changed = await db().runTransaction(async (tx) => {
    const me = await requireManager(clubId, uid, tx);
    if (memberId === me.id) badRequest("You cannot change your own role");
    const target = await requireTargetMember(clubId, memberId, tx);
    if (!canAssign(me.data.role, target.data.role, role)) forbidden("You cannot assign this role");
    tx.update(db().doc(paths.member(clubId, memberId)), { role });
    return { previous: target.data.role, userId: target.data.userId, dto: memberDto(memberId, { ...target.data, role }) };
  });
  if (changed.previous !== role) {
    await notify(async () => sendToUsers(clubMessages.roleChanged(clubId, (await requireClub(clubId)).name, changed.userId, role)));
  }
  return changed.dto;
});
