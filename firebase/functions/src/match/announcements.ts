import { randomUUID } from "node:crypto";
import { FieldValue, Timestamp, Transaction } from "firebase-admin/firestore";
import { onCall } from "firebase-functions/v2/https";
import { db } from "../admin";
import { requireMember } from "../clubs/access";
import { AfterCommit, onMembershipEnded } from "../clubs/hooks";
import * as validate from "../clubs/validation";
import { isManager } from "../domain/clubRoles";
import { allocate } from "../domain/enrollmentAllocator";
import { badRequest, conflict, forbidden, notFound } from "../errors";
import { body, requireAuth, requireString } from "../guards";
import { publishChange } from "../live";
import { messages } from "../notifications/messages";
import { sendToUsers } from "../notifications/push";
import { paths } from "../paths";
import {
  AnnouncementDoc, announcementDto, clubIdOf, currentAnnouncementDto, DEFAULT_TIME_ZONE, EntryDoc, MatchDoc, uidOfMember
} from "./model";
import { announcementRef, clubName, readAnnouncement, readSchedule } from "./store";

/** The announcement of each match: places, waitlist and guests (BE-002, BE-008 RN-A; spec 019 AC-019-09). */

const MAX_GUESTS_PER_MEMBER = 2;

function requireClubId(id: string): string {
  const clubId = clubIdOf(id);
  if (!clubId) notFound("Match announcement not found");
  return clubId as string;
}

/** Enrollment needs status OPEN and now in [opensAt, closesAt) (400, as the backend). */
function ensureOpen(a: AnnouncementDoc, now = Date.now()) {
  if (a.status !== "OPEN" || now >= a.closesAt.toMillis()) badRequest("The match announcement is closed");
  if (now < a.opensAt.toMillis()) badRequest(`The match announcement opens at ${a.opensAt.toDate().toISOString()}`);
}

/**
 * Recomputes places with the allocator and returns who moved from the waitlist to a confirmed place (the entry just
 * created is not a promotion), so they get the "you have a place" push.
 */
export function reallocate(entries: EntryDoc[], maxPlayers: number, newEntryId: string | null) {
  const allocation = allocate(
    entries.map((e) => ({ entryId: e.id, isGuest: e.participantType === "GUEST", enrolledAt: e.enrolledAt.toMillis() })),
    maxPlayers
  );
  const promoted: string[] = [];
  const updated = entries.map((e) => {
    const status = allocation.get(e.id) ?? "WAITLISTED";
    if (e.id !== newEntryId && e.status === "WAITLISTED" && status === "CONFIRMED" && e.clubMemberId) promoted.push(e.clubMemberId);
    return { ...e, status };
  });
  return { entries: updated, promoted };
}

/** Pushes to the promoted members (ignores the club mute, spec 005 RN-7). */
export function promotionPushes(clubId: string, matchId: string, promotedMemberIds: string[]): AfterCommit[] {
  if (promotedMemberIds.length === 0) return [];
  return [async () => {
    const [match, schedule, name] = await Promise.all([
      db().doc(paths.match(clubId, matchId)).get(), readSchedule(clubId), clubName(clubId)
    ]);
    if (!match.exists) return;
    const ctx = {
      type: "match.waitlist.promoted" as const, clubId, clubName: name, matchId,
      zone: schedule?.timeZone ?? DEFAULT_TIME_ZONE, matchAt: (match.data() as MatchDoc).scheduledAt.toDate()
    };
    for (const memberId of promotedMemberIds) await sendToUsers(messages.promoted(ctx, uidOfMember(memberId), matchId));
  }];
}

/** Applies a change of entries to an announcement inside [tx]; returns the after-commit work. */
function writeEntries(tx: Transaction, a: AnnouncementDoc, entries: EntryDoc[], newEntryId: string | null): { updated: AnnouncementDoc; after: AfterCommit[] } {
  const { entries: allocated, promoted } = reallocate(entries, a.maxPlayers, newEntryId);
  tx.update(db().doc(paths.announcement(a.clubId, a.matchId)), { entries: allocated, updatedAt: FieldValue.serverTimestamp() });
  return {
    updated: { ...a, entries: allocated },
    after: [() => publishChange(a.clubId, "MATCH", a.matchId), ...promotionPushes(a.clubId, a.matchId, promoted)]
  };
}

async function runAll(after: AfterCommit[]) {
  for (const action of after) await action();
}

// region Reads

/** The announcement of the club's next scheduled match, with my status (404 when there is none). */
export const getCurrentAnnouncement = onCall(async (request) => {
  const uid = requireAuth(request);
  const clubId = requireString(body(request), "clubId");
  const me = await requireMember(clubId, uid);
  const next = await db().collection(paths.matches(clubId))
    .where("status", "==", "SCHEDULED").where("scheduledAt", ">", Timestamp.now())
    .orderBy("scheduledAt").limit(1).get();
  if (next.empty) notFound("Match announcement not found");
  const match = next.docs[0];
  const announcement = await announcementRef(match.id).get();
  if (!announcement.exists) notFound("Match announcement not found");
  return currentAnnouncementDto(announcement.data() as AnnouncementDoc, match.get("scheduledAt"), me.id);
});

/** Every announcement of the club, newest first. */
export const getAnnouncementHistory = onCall(async (request) => {
  const uid = requireAuth(request);
  const clubId = requireString(body(request), "clubId");
  await requireMember(clubId, uid);
  const snapshot = await db().collection(paths.announcements(clubId)).orderBy("createdAt", "desc").get();
  return snapshot.docs.map((d) => announcementDto(d.data() as AnnouncementDoc));
});

export const getAnnouncementByMatch = onCall(async (request) => {
  const uid = requireAuth(request);
  const matchId = requireString(body(request), "matchId");
  await requireMember(requireClubId(matchId), uid);
  return announcementDto(await readAnnouncement(matchId));
});

// endregion

// region Enrollment

export const enroll = onCall(async (request) => {
  const uid = requireAuth(request);
  const announcementId = requireString(body(request), "announcementId");
  const clubId = requireClubId(announcementId);
  const { updated, after } = await db().runTransaction(async (tx) => {
    const a = await readAnnouncement(announcementId, tx);
    const me = await requireMember(clubId, uid, tx);
    ensureOpen(a);
    if (a.entries.some((e) => e.clubMemberId === me.id)) conflict("You are already enrolled in this match");
    const entry: EntryDoc = {
      id: randomUUID(), participantType: "MEMBER", clubMemberId: me.id, guestName: null, guestPosition: null,
      invitedByMemberId: null, status: "WAITLISTED", enrolledAt: Timestamp.now()
    };
    return writeEntries(tx, a, [...a.entries, entry], entry.id);
  });
  await runAll(after);
  return announcementDto(updated);
});

export const withdraw = onCall(async (request) => {
  const uid = requireAuth(request);
  const announcementId = requireString(body(request), "announcementId");
  const clubId = requireClubId(announcementId);
  const { updated, after } = await db().runTransaction(async (tx) => {
    const a = await readAnnouncement(announcementId, tx);
    const me = await requireMember(clubId, uid, tx);
    ensureOpen(a);
    if (!a.entries.some((e) => e.clubMemberId === me.id)) notFound("You are not enrolled in this match");
    return writeEntries(tx, a, a.entries.filter((e) => e.clubMemberId !== me.id), null);
  });
  await runAll(after);
  return announcementDto(updated);
});

/** A member adds a guest (at most 2); members keep priority over guests for the places (BE-008 RN-A). */
export const addGuest = onCall(async (request) => {
  const uid = requireAuth(request);
  const data = body(request);
  const announcementId = requireString(data, "announcementId");
  const name = requireString(data, "name", 1, 60);
  const position = validate.position(data.position);
  const clubId = requireClubId(announcementId);
  const { updated, after } = await db().runTransaction(async (tx) => {
    const a = await readAnnouncement(announcementId, tx);
    const host = await requireMember(clubId, uid, tx);
    ensureOpen(a);
    if (a.entries.filter((e) => e.invitedByMemberId === host.id).length >= MAX_GUESTS_PER_MEMBER) {
      conflict(`You can bring at most ${MAX_GUESTS_PER_MEMBER} guests`);
    }
    const guest: EntryDoc = {
      id: randomUUID(), participantType: "GUEST", clubMemberId: null, guestName: name, guestPosition: position,
      invitedByMemberId: host.id, status: "WAITLISTED", enrolledAt: Timestamp.now()
    };
    return writeEntries(tx, a, [...a.entries, guest], guest.id);
  });
  await runAll(after);
  return announcementDto(updated);
});

/** The host of the guest or a manager removes a guest. */
export const removeGuest = onCall(async (request) => {
  const uid = requireAuth(request);
  const data = body(request);
  const announcementId = requireString(data, "announcementId");
  const guestId = requireString(data, "guestId");
  const clubId = requireClubId(announcementId);
  const { updated, after } = await db().runTransaction(async (tx) => {
    const a = await readAnnouncement(announcementId, tx);
    const requester = await requireMember(clubId, uid, tx);
    ensureOpen(a);
    const guest = a.entries.find((e) => e.id === guestId && e.participantType === "GUEST");
    if (!guest) notFound("Guest not found in this announcement");
    if (guest!.invitedByMemberId !== requester.id && !isManager(requester.data.role)) forbidden("You cannot remove this guest");
    return writeEntries(tx, a, a.entries.filter((e) => e.id !== guestId), null);
  });
  await runAll(after);
  return announcementDto(updated);
});

// endregion

// region Withdrawals made by other operations

async function openAnnouncements(tx: Transaction, clubId: string) {
  const snapshot = await tx.get(
    db().collection(paths.announcements(clubId)).where("status", "==", "OPEN").where("closesAt", ">", Timestamp.now())
  );
  return snapshot.docs.map((d) => d.data() as AnnouncementDoc);
}

/** A member who left or was removed leaves every open announcement with their guests (BE-001 RN-10, BE-008 RN-A5). */
onMembershipEnded(async (tx, clubId, memberId) => {
  const announcements = await openAnnouncements(tx, clubId);
  const after: AfterCommit[] = [];
  for (const a of announcements) {
    const remaining = a.entries.filter((e) => e.clubMemberId !== memberId && e.invitedByMemberId !== memberId);
    if (remaining.length !== a.entries.length) after.push(...writeEntries(tx, a, remaining, null).after);
  }
  return after;
});

/** An absence withdraws the member from the open announcements of the matches it covers (BE-008 RN-C2). */
export async function withdrawForAbsence(clubId: string, memberId: string, matchIds: Set<string>) {
  const after = await db().runTransaction(async (tx) => {
    const announcements = (await openAnnouncements(tx, clubId)).filter((a) => matchIds.has(a.matchId));
    const actions: AfterCommit[] = [];
    for (const a of announcements) {
      if (a.entries.some((e) => e.clubMemberId === memberId)) {
        actions.push(...writeEntries(tx, a, a.entries.filter((e) => e.clubMemberId !== memberId), null).after);
      }
    }
    return actions;
  });
  await runAll(after);
}

// endregion
