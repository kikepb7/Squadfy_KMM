package com.kikepb.club.data.mappers

import com.kikepb.club.data.dto.CurrentMatchAnnouncementDTO
import com.kikepb.club.data.dto.MatchAnnouncementDTO
import com.kikepb.club.data.dto.MatchAnnouncementEntryDTO
import com.kikepb.club.domain.model.AnnouncementEntry
import com.kikepb.club.domain.model.AnnouncementStatus
import com.kikepb.club.domain.model.CurrentAnnouncementModel
import com.kikepb.club.domain.model.EntryStatus
import com.kikepb.club.domain.model.MatchAnnouncementModel
import com.kikepb.club.domain.model.MyEnrollmentStatus
import com.kikepb.club.domain.model.ParticipantType
import com.kikepb.club.domain.model.PlayerPosition
import kotlin.time.Instant

fun MatchAnnouncementEntryDTO.toDomain(): AnnouncementEntry = AnnouncementEntry(
    id = id,
    participantType = if (participantType == "GUEST") ParticipantType.GUEST else ParticipantType.MEMBER,
    clubMemberId = clubMemberId,
    guestName = guestName,
    guestPosition = PlayerPosition.fromRaw(guestPosition),
    invitedByMemberId = invitedByMemberId,
    status = if (status == "WAITLISTED") EntryStatus.WAITLISTED else EntryStatus.CONFIRMED,
    enrolledAt = Instant.parse(enrolledAt)
)

fun MatchAnnouncementDTO.toDomain(): MatchAnnouncementModel {
    val closes = Instant.parse(closesAt)
    return MatchAnnouncementModel(
        id = id,
        matchId = matchId,
        clubId = clubId,
        maxPlayers = maxPlayers,
        confirmedCount = confirmedCount,
        waitlistCount = waitlistCount,
        opensAt = Instant.parse(opensAt),
        closesAt = closes,
        drawAt = drawAt?.let(Instant::parse) ?: closes,
        status = AnnouncementStatus.entries.firstOrNull { it.name == status } ?: AnnouncementStatus.UNKNOWN,
        entries = entries.map { it.toDomain() },
        waitlist = waitlist.map { it.toDomain() }
    )
}

fun CurrentMatchAnnouncementDTO.toDomain(): CurrentAnnouncementModel = CurrentAnnouncementModel(
    announcement = announcement.toDomain(),
    matchScheduledAt = Instant.parse(matchScheduledAt),
    myStatus = MyEnrollmentStatus.entries.firstOrNull { it.name == myStatus } ?: MyEnrollmentStatus.NOT_ENROLLED,
    myWaitlistPosition = myWaitlistPosition
)
