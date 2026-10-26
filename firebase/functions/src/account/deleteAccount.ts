import { FieldValue, Timestamp } from "firebase-admin/firestore";
import { logger } from "firebase-functions/v2";
import { onCall } from "firebase-functions/v2/https";
import { auth, db, storage } from "../admin";
import { membershipEnded, runAfterCommit } from "../clubs/hooks";
import { MemberDoc } from "../clubs/model";
import { successor } from "../domain/clubRoles";
import { requireAuth } from "../guards";
import { paths } from "../paths";

/**
 * Account deletion (backend spec 010 RN-A3…A7; spec 019 AC-019-06). The app re-authenticates first. In order:
 * - owned clubs pass to the oldest admin, otherwise the oldest member, or are deleted when nobody else is left;
 * - the user leaves every club and its open announcements; memberships stay, anonymized, so the history and stats of
 *   the other members still add up;
 * - absences, chat messages, chats left empty, devices, club settings, pictures, the username and the account go.
 */

export const deletedUsername = (uid: string) => `Usuario eliminado ${uid.slice(0, 8)}`;

async function deleteFile(path: string) {
  try {
    await storage().bucket().file(path).delete({ ignoreNotFound: true });
  } catch (e) {
    logger.warn("[account] could not delete", path, e);
  }
}

async function handOverOrDeleteClub(clubId: string, ownerMemberId: string): Promise<boolean> {
  const active = await db().collection(paths.members(clubId)).where("status", "==", "ACTIVE").get();
  const heir = successor(ownerMemberId, active.docs.map((d) => {
    const m = d.data() as MemberDoc;
    return { memberId: d.id, role: m.role, joinedAt: m.joinedAt.toMillis() };
  }));
  if (!heir) {
    const club = await db().doc(paths.club(clubId)).get();
    const code = club.get("invitationCode") as string | undefined;
    if (code) await db().doc(paths.invitationCode(code)).delete();
    await db().recursiveDelete(db().doc(paths.club(clubId)));
    await deleteFile(`clubs/${clubId}/logo.jpg`);
    logger.info("[account] club deleted: its owner was the last member", clubId);
    return false;
  }
  const heirUid = (active.docs.find((d) => d.id === heir)!.data() as MemberDoc).userId;
  const batch = db().batch();
  batch.update(db().doc(paths.member(clubId, heir)), { role: "OWNER" });
  batch.update(db().doc(paths.club(clubId)), { ownerId: heirUid });
  await batch.commit();
  return true;
}

async function leaveClub(clubId: string, memberId: string) {
  const after = await db().runTransaction(async (tx) => {
    const actions = await membershipEnded(tx, clubId, memberId);
    tx.update(db().doc(paths.member(clubId, memberId)), { status: "LEFT", leftAt: Timestamp.now(), role: "PLAYER" });
    tx.update(db().doc(paths.club(clubId)), { membersCount: FieldValue.increment(-1) });
    return actions;
  });
  await runAfterCommit(after);
}

export async function eraseUser(uid: string) {
  // Clubs: hand over or delete owned ones, leave the rest, anonymize every membership
  const memberships = await db().collectionGroup("members").where("userId", "==", uid).get();
  for (const doc of memberships.docs) {
    const member = doc.data() as MemberDoc;
    if (member.clubPictureUrl) await deleteFile(`clubs/${member.clubId}/members/${uid}.jpg`);
    if (member.status === "ACTIVE") {
      const kept = member.role === "OWNER" ? await handOverOrDeleteClub(member.clubId, doc.id) : true;
      if (!kept) continue;
      await leaveClub(member.clubId, doc.id);
    }
    if ((await doc.ref.get()).exists) {
      await doc.ref.update({ username: deletedUsername(uid), profilePictureUrl: null, clubPictureUrl: null });
    }
    // Absences are personal
    const absences = await db().collection(paths.absences(member.clubId)).where("clubMemberId", "==", doc.id).get();
    await Promise.all(absences.docs.map((a) => a.ref.delete()));
  }

  // Chat: my messages go, I leave every chat and the ones left empty disappear
  const myMessages = await db().collectionGroup("messages").where("senderId", "==", uid).get();
  const writer = db().bulkWriter();
  myMessages.docs.forEach((d) => writer.delete(d.ref));
  await writer.close();
  const chats = await db().collection(paths.chats()).where("participantIds", "array-contains", uid).get();
  for (const chat of chats.docs) {
    const remaining = (chat.get("participantIds") as string[]).filter((id) => id !== uid);
    if (remaining.length === 0) {
      await db().recursiveDelete(chat.ref);
    } else {
      await chat.ref.update({ participantIds: remaining, [`participants.${uid}`]: FieldValue.delete() });
    }
  }

  // Profile, devices, club settings, username and picture
  const user = await db().doc(paths.user(uid)).get();
  const lower = user.get("usernameLower") as string | undefined;
  if (lower) await db().doc(paths.username(lower)).delete();
  await db().recursiveDelete(db().doc(paths.user(uid)));
  await deleteFile(`users/${uid}/avatar.jpg`);
}

export const deleteAccount = onCall(async (request) => {
  const uid = requireAuth(request);
  await eraseUser(uid);
  await auth().deleteUser(uid);
  logger.info("[account] account deleted");
  return { ok: true };
});
