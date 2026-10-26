/**
 * Squadfy domain server on Cloud Functions (spec 019, ADR-0010). Every write of clubs, schedules, announcements,
 * matches, ratings and absences goes through a callable or a scheduled function here; Firestore rules only let members
 * read. The pure domain logic in `domain/` is ported from `Squadfy_Backend` with its tests.
 */
import "./config";

export { completeProfile } from "./account/profile";
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
