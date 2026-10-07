package com.kikepb.club.data.dto

import kotlinx.serialization.Serializable

/** `MatchAnnouncementEntryDto` (BACKEND.md §9 + BE-008: guests carry `guestName` and a null `clubMemberId`). */
@Serializable
data class MatchAnnouncementEntryDTO(
    val id: String,
    val matchAnnouncementId: String? = null,
    val participantType: String = "MEMBER",
    val clubMemberId: String? = null,
    val guestName: String? = null,
    val guestPosition: String? = null,
    val invitedByMemberId: String? = null,
    val status: String,
    val enrolledAt: String
)

@Serializable
data class MatchAnnouncementDTO(
    val id: String,
    val matchId: String,
    val clubId: String,
    val maxPlayers: Int,
    val confirmedCount: Int = 0,
    val waitlistCount: Int = 0,
    val opensAt: String,
    val closesAt: String,
    val drawAt: String? = null,
    val status: String,
    val entries: List<MatchAnnouncementEntryDTO> = emptyList(),
    val waitlist: List<MatchAnnouncementEntryDTO> = emptyList(),
    val createdAt: String? = null,
    val updatedAt: String? = null
)

@Serializable
data class CurrentMatchAnnouncementDTO(
    val announcement: MatchAnnouncementDTO,
    val matchScheduledAt: String,
    val myStatus: String = "NOT_ENROLLED",
    val myWaitlistPosition: Int? = null
)

@Serializable
data class AddGuestRequestDTO(
    val name: String,
    val position: String? = null
)
