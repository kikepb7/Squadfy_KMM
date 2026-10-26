/**
 * The Firestore data model (spec 019, `specs/contracts/firebase.md`). Keep in sync with FirestorePaths.kt and
 * firestore.rules.
 */
export const paths = {
  user: (uid: string) => `users/${uid}`,
  devices: (uid: string) => `users/${uid}/devices`,
  clubSettings: (uid: string, clubId: string) => `users/${uid}/clubSettings/${clubId}`,
  username: (lower: string) => `usernames/${lower}`,
  invitationCode: (code: string) => `invitationCodes/${code}`,
  club: (clubId: string) => `clubs/${clubId}`,
  members: (clubId: string) => `clubs/${clubId}/members`,
  member: (clubId: string, memberId: string) => `clubs/${clubId}/members/${memberId}`,
  schedule: (clubId: string) => `clubs/${clubId}/schedule/current`,
  scheduleExceptions: (clubId: string) => `clubs/${clubId}/scheduleExceptions`,
  matches: (clubId: string) => `clubs/${clubId}/matches`,
  match: (clubId: string, matchId: string) => `clubs/${clubId}/matches/${matchId}`,
  announcements: (clubId: string) => `clubs/${clubId}/announcements`,
  announcement: (clubId: string, matchId: string) => `clubs/${clubId}/announcements/${matchId}`,
  ratings: (clubId: string) => `clubs/${clubId}/ratings`,
  rating: (clubId: string, memberId: string) => `clubs/${clubId}/ratings/${memberId}`,
  absences: (clubId: string) => `clubs/${clubId}/absences`,
  chats: () => "chats",
  chat: (chatId: string) => `chats/${chatId}`,
  messages: (chatId: string) => `chats/${chatId}/messages`
};

/**
 * A membership id is unique across clubs (teams, ratings and stats use it) and stable when a former member comes back
 * (BE-001 RN-12): `{clubId}_{uid}`. Firebase ids contain no underscore.
 */
export const memberIdOf = (clubId: string, uid: string) => `${clubId}_${uid}`;

export function parseMemberId(memberId: string): { clubId: string; uid: string } | null {
  const index = memberId.indexOf("_");
  if (index <= 0 || index === memberId.length - 1) return null;
  return { clubId: memberId.slice(0, index), uid: memberId.slice(index + 1) };
}
