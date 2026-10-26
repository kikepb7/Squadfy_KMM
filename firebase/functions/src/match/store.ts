import { DocumentReference, Timestamp, Transaction } from "firebase-admin/firestore";
import { db } from "../admin";
import { MemberDoc } from "../clubs/model";
import { notFound } from "../errors";
import { paths } from "../paths";
import { AnnouncementDoc, clubIdOf, DEFAULT_TIME_ZONE, MatchDoc, ScheduleDoc } from "./model";

/** Small typed helpers over the match collections. */

export const matchRef = (matchId: string): DocumentReference => {
  const clubId = clubIdOf(matchId);
  if (!clubId) notFound("Match not found");
  return db().doc(paths.match(clubId as string, matchId));
};

export const announcementRef = (announcementId: string): DocumentReference => {
  const clubId = clubIdOf(announcementId);
  if (!clubId) notFound("Match announcement not found");
  return db().doc(paths.announcement(clubId as string, announcementId));
};

export async function readMatch(matchId: string, tx?: Transaction): Promise<MatchDoc> {
  const snapshot = tx ? await tx.get(matchRef(matchId)) : await matchRef(matchId).get();
  if (!snapshot.exists) notFound("Match not found");
  return snapshot.data() as MatchDoc;
}

export async function readAnnouncement(announcementId: string, tx?: Transaction): Promise<AnnouncementDoc> {
  const snapshot = tx ? await tx.get(announcementRef(announcementId)) : await announcementRef(announcementId).get();
  if (!snapshot.exists) notFound("Match announcement not found");
  return snapshot.data() as AnnouncementDoc;
}

export async function findAnnouncement(matchId: string, tx?: Transaction): Promise<AnnouncementDoc | null> {
  const ref = announcementRef(matchId);
  const snapshot = tx ? await tx.get(ref) : await ref.get();
  return snapshot.exists ? (snapshot.data() as AnnouncementDoc) : null;
}

export async function readSchedule(clubId: string, tx?: Transaction): Promise<ScheduleDoc | null> {
  const ref = db().doc(paths.schedule(clubId));
  const snapshot = tx ? await tx.get(ref) : await ref.get();
  return snapshot.exists ? (snapshot.data() as ScheduleDoc) : null;
}

export async function zoneOf(clubId: string): Promise<string> {
  return (await readSchedule(clubId))?.timeZone ?? DEFAULT_TIME_ZONE;
}

export async function clubName(clubId: string): Promise<string> {
  return ((await db().doc(paths.club(clubId)).get()).get("name") as string | undefined) ?? "";
}

/** Active members with their membership ids. */
export async function activeMembers(clubId: string): Promise<Array<{ id: string; data: MemberDoc }>> {
  const snapshot = await db().collection(paths.members(clubId)).where("status", "==", "ACTIVE").get();
  return snapshot.docs.map((d) => ({ id: d.id, data: d.data() as MemberDoc }));
}

export const now = () => Timestamp.now();
