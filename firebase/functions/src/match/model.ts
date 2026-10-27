import { Timestamp } from "firebase-admin/firestore";
import { DateTime } from "luxon";
import { DeadlineRule } from "../domain/deadlineRule";
import { DayOfWeek } from "../domain/matchCalendar";
import { AnnouncementStatus, EntryStatus, MatchEventType, MatchFormat, MatchStatus, PlayerPosition } from "../domain/types";

/**
 * Firestore documents of the match cycle (spec 019, `specs/contracts/firebase.md`) and the DTOs the callables answer,
 * field by field as `Squadfy_Backend/docs/BACKEND.md` §9, so the app maps them with the same code as before.
 */

export const DEFAULT_TIME_ZONE = "Europe/Madrid";
export const DEFAULT_DURATION_MINUTES = 60;

/**
 * Ids are unique across clubs and carry the club, so `getMatch(matchId)` and `enroll(announcementId)` find it:
 * - match: `{clubId}_{yyyyMMddTHHmm}Z` of its scheduled time (UTC); one match per club and time, as the backend's
 *   unique (club_id, scheduled_at) index, which makes planning idempotent.
 * - announcement: the id of its match (one per match).
 */
export const matchIdOf = (clubId: string, scheduledAt: Date) =>
  `${clubId}_${DateTime.fromJSDate(scheduledAt, { zone: "UTC" }).toFormat("yyyyMMdd'T'HHmm")}Z`;

export const clubIdOf = (id: string): string | null => {
  const index = id.indexOf("_");
  return index > 0 ? id.slice(0, index) : null;
};

/** The uid inside a membership id `{clubId}_{uid}`. */
export const uidOfMember = (memberId: string) => memberId.slice(memberId.indexOf("_") + 1);

// region Documents

/** `clubs/{clubId}/schedule/current` */
export interface ScheduleDoc {
  clubId: string;
  matchDayOfWeek: DayOfWeek;
  matchTime: string; // HH:mm:ss, club time
  timeZone: string;
  format: MatchFormat;
  maxPlayers: number;
  matchDurationMinutes: number;
  closeDaysBefore: number;
  closeTime: string;
  drawDaysBefore: number;
  drawTime: string;
  isActive: boolean;
  createdAt: Timestamp;
  updatedAt: Timestamp;
}

export const closeRule = (s: ScheduleDoc): DeadlineRule => ({ daysBefore: s.closeDaysBefore, time: s.closeTime });
export const drawRule = (s: ScheduleDoc): DeadlineRule => ({ daysBefore: s.drawDaysBefore, time: s.drawTime });

/** `clubs/{clubId}/scheduleExceptions/{id}` */
export interface ScheduleExceptionDoc {
  clubId: string;
  date: string; // yyyy-MM-dd, the regular match date
  type: "CANCELLED" | "RESCHEDULED";
  newScheduledAt: Timestamp | null;
  reason: string | null;
  affectedMatchId: string | null;
  createdAt: Timestamp;
}

export interface TeamPlayer {
  /** Member id, or the guest entry id when [isGuest]. */
  participantId: string;
  isGuest: boolean;
  /** Null = the whole match. */
  minutesPlayed: number | null;
}

export interface MatchEventDoc {
  id: string;
  clubMemberId: string;
  type: MatchEventType;
  minute: number | null;
  createdAt: Timestamp;
}

/** `clubs/{clubId}/matches/{matchId}` */
export interface MatchDoc {
  clubId: string;
  scheduledAt: Timestamp;
  status: MatchStatus;
  scheduleDate: string | null;
  durationMinutes: number;
  manualTeamAScore: number | null;
  manualTeamBScore: number | null;
  teamA: TeamPlayer[];
  teamB: TeamPlayer[];
  events: MatchEventDoc[];
  /** Rating delta of each member, stored when the match is completed (BE-008 RN-F1). */
  ratingChanges: Record<string, number>;
  /** The season the match counted in once completed; null for a friendly, missing before spec 020 or while open. */
  seasonId?: string | null;
  createdAt: Timestamp;
  updatedAt: Timestamp;
}

export interface EntryDoc {
  id: string;
  participantType: "MEMBER" | "GUEST";
  clubMemberId: string | null;
  guestName: string | null;
  guestPosition: PlayerPosition | null;
  invitedByMemberId: string | null;
  status: EntryStatus;
  enrolledAt: Timestamp;
}

/** `clubs/{clubId}/announcements/{matchId}`: the entries live inside, so every enrollment is one transaction. */
export interface AnnouncementDoc {
  matchId: string;
  clubId: string;
  maxPlayers: number;
  opensAt: Timestamp;
  closesAt: Timestamp;
  drawAt: Timestamp;
  status: AnnouncementStatus;
  entries: EntryDoc[];
  /** Once-only markers of the jobs; null while pending (queried with `== null`). */
  openedNotifiedAt: Timestamp | null;
  closingReminderSentAt: Timestamp | null;
  teamsPublishedAt: Timestamp | null;
  createdAt: Timestamp;
  updatedAt: Timestamp;
}

/** `clubs/{clubId}/ratings/{memberId}` */
export interface RatingDoc {
  clubMemberId: string;
  rating: number;
  matchesRated: number;
}

/** `clubs/{clubId}/absences/{id}` */
export interface AbsenceDoc {
  clubId: string;
  clubMemberId: string;
  fromDate: string;
  toDate: string;
  reason: string | null;
  createdAt: Timestamp;
}

// endregion

// region DTOs (BACKEND.md §9)

const iso = (t: Timestamp | null | undefined) => (t ? t.toDate().toISOString() : null);

export const scheduleDto = (s: ScheduleDoc) => ({
  id: s.clubId,
  clubId: s.clubId,
  matchDayOfWeek: s.matchDayOfWeek,
  matchTime: s.matchTime,
  timeZone: s.timeZone,
  format: s.format,
  maxPlayers: s.maxPlayers,
  matchDurationMinutes: s.matchDurationMinutes,
  closeDaysBefore: s.closeDaysBefore,
  closeTime: s.closeTime,
  drawDaysBefore: s.drawDaysBefore,
  drawTime: s.drawTime,
  isActive: s.isActive,
  createdAt: iso(s.createdAt),
  updatedAt: iso(s.updatedAt)
});

export const exceptionDto = (id: string, e: ScheduleExceptionDoc) => ({
  id,
  clubId: e.clubId,
  date: e.date,
  type: e.type,
  newScheduledAt: iso(e.newScheduledAt),
  reason: e.reason,
  createdAt: iso(e.createdAt)
});

export const absenceDto = (id: string, a: AbsenceDoc) => ({
  id,
  clubId: a.clubId,
  clubMemberId: a.clubMemberId,
  fromDate: a.fromDate,
  toDate: a.toDate,
  reason: a.reason,
  createdAt: iso(a.createdAt)
});

const entryDto = (announcementId: string, e: EntryDoc) => ({
  id: e.id,
  matchAnnouncementId: announcementId,
  participantType: e.participantType,
  clubMemberId: e.clubMemberId,
  guestName: e.guestName,
  guestPosition: e.guestPosition,
  invitedByMemberId: e.invitedByMemberId,
  status: e.status,
  enrolledAt: iso(e.enrolledAt)
});

/** Members first, then guests, each by enrollment time (BE-008 RN-A2/A3). */
export const inPriorityOrder = (entries: EntryDoc[], status: EntryStatus) =>
  entries
    .filter((e) => e.status === status)
    .sort((a, b) => Number(a.participantType === "GUEST") - Number(b.participantType === "GUEST") || a.enrolledAt.toMillis() - b.enrolledAt.toMillis());

export const announcementDto = (a: AnnouncementDoc) => {
  const confirmed = inPriorityOrder(a.entries, "CONFIRMED");
  const waitlist = inPriorityOrder(a.entries, "WAITLISTED");
  return {
    id: a.matchId,
    matchId: a.matchId,
    clubId: a.clubId,
    maxPlayers: a.maxPlayers,
    confirmedCount: confirmed.length,
    waitlistCount: waitlist.length,
    opensAt: iso(a.opensAt),
    closesAt: iso(a.closesAt),
    drawAt: iso(a.drawAt),
    status: a.status,
    entries: confirmed.map((e) => entryDto(a.matchId, e)),
    waitlist: waitlist.map((e) => entryDto(a.matchId, e)),
    createdAt: iso(a.createdAt),
    updatedAt: iso(a.updatedAt)
  };
};

/** The announcement of the next match seen by one member (BE-007 R-7). */
export const currentAnnouncementDto = (a: AnnouncementDoc, matchAt: Timestamp, memberId: string) => {
  const mine = a.entries.find((e) => e.clubMemberId === memberId);
  const position = inPriorityOrder(a.entries, "WAITLISTED").findIndex((e) => e.clubMemberId === memberId);
  return {
    announcement: announcementDto(a),
    matchScheduledAt: iso(matchAt),
    myStatus: mine ? mine.status : "NOT_ENROLLED",
    myWaitlistPosition: position >= 0 ? position + 1 : null
  };
};

const guestDto = (e: EntryDoc) => ({
  guestId: e.id,
  name: e.guestName ?? "",
  position: e.guestPosition,
  invitedByMemberId: e.invitedByMemberId
});

/** The score is the manual one when set, otherwise the goals of each team's members (BE-008 RN-E2). */
export function scoreOf(m: MatchDoc) {
  const isManualScore = m.manualTeamAScore !== null && m.manualTeamBScore !== null;
  const members = (team: TeamPlayer[]) => new Set(team.filter((p) => !p.isGuest).map((p) => p.participantId));
  const goals = (team: TeamPlayer[]) => {
    const ids = members(team);
    return m.events.filter((e) => e.type === "GOAL" && ids.has(e.clubMemberId)).length;
  };
  return {
    isManualScore,
    teamAScore: isManualScore ? (m.manualTeamAScore as number) : goals(m.teamA),
    teamBScore: isManualScore ? (m.manualTeamBScore as number) : goals(m.teamB)
  };
}

export function matchDto(id: string, m: MatchDoc, announcement: AnnouncementDoc | null) {
  const confirmed = announcement ? inPriorityOrder(announcement.entries, "CONFIRMED") : [];
  const guestsById = new Map((announcement?.entries ?? []).filter((e) => e.participantType === "GUEST").map((e) => [e.id, e]));
  const memberIds = (team: TeamPlayer[]) => team.filter((p) => !p.isGuest).map((p) => p.participantId);
  const guests = (team: TeamPlayer[]) =>
    team.filter((p) => p.isGuest).map((p) => guestsById.get(p.participantId)).filter((e): e is EntryDoc => !!e).map(guestDto);
  const events = [...m.events].sort((a, b) => a.createdAt.toMillis() - b.createdAt.toMillis());
  const eventDto = (e: MatchEventDoc) => ({ id: e.id, matchId: id, clubMemberId: e.clubMemberId, type: e.type, minute: e.minute, createdAt: iso(e.createdAt) });
  const ofType = (type: MatchEventType) => events.filter((e) => e.type === type).map(eventDto);
  const score = scoreOf(m);
  return {
    id,
    clubId: m.clubId,
    scheduledAt: iso(m.scheduledAt),
    status: m.status,
    enrolledPlayers: confirmed.filter((e) => e.participantType === "MEMBER").map((e) => e.clubMemberId as string),
    teamA: memberIds(m.teamA),
    teamB: memberIds(m.teamB),
    durationMinutes: m.durationMinutes,
    minutesPlayed: Object.fromEntries(
      [...m.teamA, ...m.teamB].filter((p) => !p.isGuest).map((p) => [p.participantId, p.minutesPlayed ?? m.durationMinutes])
    ),
    enrolledGuests: confirmed.filter((e) => e.participantType === "GUEST").map(guestDto),
    teamAGuests: guests(m.teamA),
    teamBGuests: guests(m.teamB),
    isManualScore: score.isManualScore,
    ratingChanges: m.status === "COMPLETED"
      ? Object.fromEntries(Object.entries(m.ratingChanges ?? {}).map(([k, v]) => [k, Math.round(v)]))
      : {},
    scheduleDate: m.scheduleDate,
    teamAScore: score.teamAScore,
    teamBScore: score.teamBScore,
    goals: ofType("GOAL"),
    assists: ofType("ASSIST"),
    yellowCards: ofType("YELLOW_CARD"),
    redCards: ofType("RED_CARD"),
    createdAt: iso(m.createdAt),
    updatedAt: iso(m.updatedAt)
  };
}

// endregion
