/**
 * Squadfy domain server on Cloud Functions (spec 019, ADR-0010). Every write of clubs, schedules, announcements,
 * matches, ratings and absences goes through a callable or a scheduled function here; Firestore rules only let members
 * read. The pure domain logic in `domain/` is ported from `Squadfy_Backend` with its tests.
 */
import "./config";

export { completeProfile } from "./account/profile";
export { deleteAccount, onAuthUserDeleted } from "./account/deleteAccount";
export {
  banMember,
  changeMemberRole,
  createClub,
  joinClub,
  leaveClub,
  regenerateInvitationCode,
  removeMember,
  setClubLogo,
  setMyClubPicture,
  transferOwnership,
  unbanMember,
  updateClub,
  updateMyMembership
} from "./clubs/clubs";
export { createSchedule, getSchedule, updateSchedule, getScheduleExceptions, addScheduleException, deleteScheduleException } from "./match/schedule";
export {
  addGuest, enroll, getAnnouncementByMatch, getAnnouncementHistory, getCurrentAnnouncement, removeGuest, withdraw
} from "./match/announcements";
export {
  addMatchEvent, cancelMatch, clearMatchScore, completeMatch, createExtraMatch, deleteMatchEvent, generateTeams, getClubMatches,
  getMatch, getTeamBalance, reopenMatch, setMatchScore, setPlayerMinutes
} from "./match/matches";
export { getMyRating, getMyStats, getRatings, getStats } from "./match/standings";
export { createAbsence, deleteAbsence, getAbsences } from "./match/absences";
export { getClubNotificationSettings, setClubMuted } from "./notifications/settings";
export { closeAnnouncementsAndDraw, planUpcomingMatches, runJobsNow, sendAnnouncementNotifications } from "./match/jobs";
export { addChatParticipants, createChat, leaveChat, onChatMessageCreated, onChatMessageDeleted, onUserProfileUpdated } from "./chat/chat";
