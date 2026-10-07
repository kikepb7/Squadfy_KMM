package com.kikepb.club.domain.model

import kotlin.time.Instant

enum class AnnouncementStatus { OPEN, CLOSED, CANCELLED, UNKNOWN }

enum class EntryStatus { CONFIRMED, WAITLISTED }

enum class ParticipantType { MEMBER, GUEST }

enum class MyEnrollmentStatus { NOT_ENROLLED, CONFIRMED, WAITLISTED }

/** A sign-up of a member or of a guest added by a member (BE-002 RN-7, BE-008 RN-A). */
data class AnnouncementEntry(
    /** For guests this is the `guestId`. */
    val id: String,
    val participantType: ParticipantType,
    val clubMemberId: String?,
    val guestName: String?,
    val guestPosition: PlayerPosition?,
    val invitedByMemberId: String?,
    val status: EntryStatus,
    val enrolledAt: Instant
) {
    val isGuest: Boolean get() = participantType == ParticipantType.GUEST
}

/** The sign-up window of a match (`MatchAnnouncementDto`). */
data class MatchAnnouncementModel(
    val id: String,
    val matchId: String,
    val clubId: String,
    val maxPlayers: Int,
    val confirmedCount: Int,
    val waitlistCount: Int,
    val opensAt: Instant,
    val closesAt: Instant,
    /** When teams are published (BE-008 RN-D); defaults to [closesAt] when the backend does not send it. */
    val drawAt: Instant,
    val status: AnnouncementStatus,
    /** Confirmed: members first, then guests, in order of sign-up. */
    val entries: List<AnnouncementEntry>,
    /** Waitlist in promotion order (members first). */
    val waitlist: List<AnnouncementEntry>
)

/** The next match's announcement plus my own status (`CurrentMatchAnnouncementDto`). */
data class CurrentAnnouncementModel(
    val announcement: MatchAnnouncementModel,
    val matchScheduledAt: Instant,
    val myStatus: MyEnrollmentStatus,
    val myWaitlistPosition: Int?
)
