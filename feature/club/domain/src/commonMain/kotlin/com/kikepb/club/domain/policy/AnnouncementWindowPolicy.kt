package com.kikepb.club.domain.policy

import com.kikepb.club.domain.model.AnnouncementEntry
import com.kikepb.club.domain.model.AnnouncementStatus
import com.kikepb.club.domain.model.CurrentAnnouncementModel
import com.kikepb.club.domain.model.EntryStatus
import com.kikepb.club.domain.model.MatchAnnouncementModel
import com.kikepb.club.domain.model.MyEnrollmentStatus
import com.kikepb.club.domain.model.ParticipantType
import kotlin.time.Duration
import kotlin.time.Instant

enum class WindowState { NOT_OPEN, OPEN, CLOSED, CANCELLED }

/**
 * APP-RN-01: the UI considers the announcement open only while `status == OPEN` and
 * `opensAt <= now < closesAt`; the backend may keep `status = OPEN` up to 5 minutes after the close.
 */
object AnnouncementWindowPolicy {

    fun state(announcement: MatchAnnouncementModel, now: Instant): WindowState = when {
        announcement.status == AnnouncementStatus.CANCELLED -> WindowState.CANCELLED
        announcement.status != AnnouncementStatus.OPEN -> WindowState.CLOSED
        now < announcement.opensAt -> WindowState.NOT_OPEN
        now >= announcement.closesAt -> WindowState.CLOSED
        else -> WindowState.OPEN
    }

    /** Time left until the next window boundary (open or close), or null when nothing is pending. */
    fun timeToNextChange(announcement: MatchAnnouncementModel, now: Instant): Duration? = when (state(announcement, now)) {
        WindowState.NOT_OPEN -> announcement.opensAt - now
        WindowState.OPEN -> announcement.closesAt - now
        WindowState.CLOSED, WindowState.CANCELLED -> null
    }

    /**
     * My status after an enroll/withdraw: those endpoints return the announcement, not `myStatus`,
     * so it is derived from my `clubMemberId` (guests never count as me).
     */
    fun withMyStatus(current: CurrentAnnouncementModel, updated: MatchAnnouncementModel, myMemberId: String?): CurrentAnnouncementModel {
        fun AnnouncementEntry.isMe() = participantType == ParticipantType.MEMBER && myMemberId != null && clubMemberId == myMemberId
        val waitlistIndex = updated.waitlist.indexOfFirst { it.isMe() }
        val myStatus = when {
            updated.entries.any { it.isMe() && it.status == EntryStatus.CONFIRMED } -> MyEnrollmentStatus.CONFIRMED
            waitlistIndex >= 0 -> MyEnrollmentStatus.WAITLISTED
            else -> MyEnrollmentStatus.NOT_ENROLLED
        }
        return current.copy(
            announcement = updated,
            myStatus = myStatus,
            myWaitlistPosition = if (waitlistIndex >= 0) waitlistIndex + 1 else null
        )
    }

    /** BE-008 RN-A4: a member can add at most 2 guests per announcement. */
    const val MAX_GUESTS_PER_MEMBER = 2

    fun myGuestsCount(announcement: MatchAnnouncementModel, myMemberId: String?): Int =
        (announcement.entries + announcement.waitlist).count { it.isGuest && myMemberId != null && it.invitedByMemberId == myMemberId }
}
