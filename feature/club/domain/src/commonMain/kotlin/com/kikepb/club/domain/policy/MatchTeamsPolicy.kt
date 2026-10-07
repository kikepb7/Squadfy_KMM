package com.kikepb.club.domain.policy

import com.kikepb.club.domain.model.AnnouncementStatus
import com.kikepb.club.domain.model.MatchAnnouncementModel
import com.kikepb.club.domain.model.MatchModel
import com.kikepb.club.domain.model.MatchStatus
import com.kikepb.club.domain.model.Team
import com.kikepb.club.domain.usecase.GenerateTeamsUseCase
import kotlin.time.Instant

/** What the detail shows while a match has no teams yet (AC-006-02, AC-006-09). */
sealed interface TeamsPending {
    /** The announcement is still open: teams come when it closes. */
    data class AtClose(val closesAt: Instant) : TeamsPending
    /** Closed, waiting for the configured draw time (BE-008 RN-D3). */
    data class AtDraw(val drawAt: Instant) : TeamsPending
    data object Pending : TeamsPending
}

/** A participant of the manual adjustment: a member (`clubMemberId`) or a guest (`guestId`). */
data class TeamSlot(val id: String, val isGuest: Boolean)

data class TeamsSplit(val teamA: List<TeamSlot>, val teamB: List<TeamSlot>) {
    val isValid: Boolean
        get() = GenerateTeamsUseCase.isValidManualSplit(teamA.map { it.id }, teamB.map { it.id })

    /** Moves [id] to the other team (AC-006-05). */
    fun move(id: String): TeamsSplit {
        teamA.firstOrNull { it.id == id }?.let { return TeamsSplit(teamA = teamA - it, teamB = teamB + it) }
        teamB.firstOrNull { it.id == id }?.let { return TeamsSplit(teamA = teamA + it, teamB = teamB - it) }
        return this
    }

    fun teamOf(id: String): Team? = when {
        teamA.any { it.id == id } -> Team.A
        teamB.any { it.id == id } -> Team.B
        else -> null
    }
}

object MatchTeamsPolicy {

    /** BE-003 RN-7/8: teams can be rectified only while the match is SCHEDULED (AC-006-06). */
    fun canRectify(match: MatchModel, isManager: Boolean): Boolean = isManager && match.status == MatchStatus.SCHEDULED

    fun pending(match: MatchModel, announcement: MatchAnnouncementModel?, now: Instant): TeamsPending? {
        if (match.hasTeams || match.status != MatchStatus.SCHEDULED) return null
        announcement ?: return TeamsPending.Pending
        return when {
            announcement.status == AnnouncementStatus.OPEN || now < announcement.closesAt -> TeamsPending.AtClose(announcement.closesAt)
            now < announcement.drawAt -> TeamsPending.AtDraw(announcement.drawAt)
            else -> TeamsPending.Pending
        }
    }

    /**
     * Starting point of the manual adjustment: the current teams, or the confirmed participants
     * split in two halves when there are no teams yet.
     */
    fun initialSplit(match: MatchModel): TeamsSplit {
        if (match.hasTeams) {
            return TeamsSplit(
                teamA = match.teamA.map { TeamSlot(it, isGuest = false) } + match.teamAGuests.map { TeamSlot(it.guestId, isGuest = true) },
                teamB = match.teamB.map { TeamSlot(it, isGuest = false) } + match.teamBGuests.map { TeamSlot(it.guestId, isGuest = true) }
            )
        }
        val everyone = match.enrolledPlayers.map { TeamSlot(it, isGuest = false) } + match.enrolledGuests.map { TeamSlot(it.guestId, isGuest = true) }
        val (a, b) = everyone.withIndex().partition { it.index % 2 == 0 }
        return TeamsSplit(teamA = a.map { it.value }, teamB = b.map { it.value })
    }
}
