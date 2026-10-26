/** Domain enums, as the backend sends them (BACKEND.md §10) and the app maps them. */
export type ClubMemberRole = "OWNER" | "ADMIN" | "CAPTAIN" | "PLAYER";
export type PlayerPosition = "GOALKEEPER" | "DEFENDER" | "MIDFIELDER" | "FORWARD";
export type EntryStatus = "CONFIRMED" | "WAITLISTED";
export type MatchEventType = "GOAL" | "ASSIST" | "YELLOW_CARD" | "RED_CARD";
export type MatchStatus = "SCHEDULED" | "COMPLETED" | "CANCELLED";
export type AnnouncementStatus = "OPEN" | "CLOSED" | "CANCELLED";
export type TeamSide = "A" | "B";

export const MATCH_FORMATS = {
  FIVE_A_SIDE: 5,
  SEVEN_A_SIDE: 7,
  ELEVEN_A_SIDE: 11
} as const;
export type MatchFormat = keyof typeof MATCH_FORMATS;

/** Confirmed places of the announcement; further enrollments go to the waitlist. */
export const maxPlayersOf = (format: MatchFormat): number => MATCH_FORMATS[format] * 2;
