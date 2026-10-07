package com.kikepb.club.domain.usecase

import com.kikepb.club.domain.error.ClubError
import com.kikepb.club.domain.model.MatchEventType
import com.kikepb.club.domain.model.MatchFormat
import com.kikepb.club.domain.model.MatchModel
import com.kikepb.club.domain.model.MatchStatus
import com.kikepb.club.domain.model.TeamBalanceModel
import com.kikepb.club.domain.repository.MatchRepository
import com.kikepb.core.domain.util.Result
import kotlin.time.Instant

class GetMatchUseCase(private val repository: MatchRepository) {
    suspend operator fun invoke(matchId: String): Result<MatchModel, ClubError> = repository.getMatch(matchId)
}

class GetClubMatchesUseCase(private val repository: MatchRepository) {
    suspend operator fun invoke(clubId: String, status: MatchStatus? = null): Result<List<MatchModel>, ClubError> =
        repository.getClubMatches(clubId, status)
}

class GetTeamBalanceUseCase(private val repository: MatchRepository) {
    suspend operator fun invoke(matchId: String): Result<TeamBalanceModel?, ClubError> = repository.getTeamBalance(matchId)
}

/** BE-003 RN-7/8: AUTO re-draws; MANUAL needs two disjoint, non-empty teams whose sizes differ by at most 1. */
class GenerateTeamsUseCase(private val repository: MatchRepository) {
    suspend fun auto(matchId: String): Result<MatchModel, ClubError> = repository.generateTeams(matchId, teamA = null, teamB = null)

    suspend fun manual(matchId: String, teamA: List<String>, teamB: List<String>): Result<MatchModel, ClubError> =
        repository.generateTeams(matchId, teamA = teamA, teamB = teamB)

    companion object {
        fun isValidManualSplit(teamA: Collection<String>, teamB: Collection<String>): Boolean =
            teamA.isNotEmpty() && teamB.isNotEmpty() && teamA.intersect(teamB.toSet()).isEmpty() &&
                kotlin.math.abs(teamA.size - teamB.size) <= 1
    }
}

class CreateExtraMatchUseCase(private val repository: MatchRepository) {
    suspend operator fun invoke(clubId: String, scheduledAt: Instant, format: MatchFormat?, durationMinutes: Int?): Result<MatchModel, ClubError> =
        repository.createExtraMatch(clubId, scheduledAt, format, durationMinutes)
}

class CancelMatchUseCase(private val repository: MatchRepository) {
    suspend operator fun invoke(matchId: String): Result<MatchModel, ClubError> = repository.cancel(matchId)
}

class CompleteMatchUseCase(private val repository: MatchRepository) {
    suspend operator fun invoke(matchId: String): Result<MatchModel, ClubError> = repository.complete(matchId)
}

class ReopenMatchUseCase(private val repository: MatchRepository) {
    suspend operator fun invoke(matchId: String): Result<MatchModel, ClubError> = repository.reopen(matchId)
}

class AddMatchEventUseCase(private val repository: MatchRepository) {
    suspend operator fun invoke(matchId: String, clubMemberId: String, type: MatchEventType, minute: Int?): Result<MatchModel, ClubError> =
        repository.addEvent(matchId, clubMemberId, type, minute?.takeIf { it in VALID_MINUTES })

    companion object {
        val VALID_MINUTES = 1..120
    }
}

class DeleteMatchEventUseCase(private val repository: MatchRepository) {
    suspend operator fun invoke(matchId: String, eventId: String): Result<MatchModel, ClubError> = repository.deleteEvent(matchId, eventId)
}

class SetPlayerMinutesUseCase(private val repository: MatchRepository) {
    suspend operator fun invoke(matchId: String, clubMemberId: String, minutes: Int): Result<MatchModel, ClubError> =
        repository.setMinutes(matchId, clubMemberId, minutes)
}

/** BE-008 RN-E: manual official score 0-99 while SCHEDULED. */
class SetManualScoreUseCase(private val repository: MatchRepository) {
    suspend operator fun invoke(matchId: String, teamAScore: Int, teamBScore: Int): Result<MatchModel, ClubError> =
        repository.setManualScore(matchId, teamAScore.coerceIn(SCORE_RANGE), teamBScore.coerceIn(SCORE_RANGE))

    companion object {
        val SCORE_RANGE = 0..99
    }
}

class ClearManualScoreUseCase(private val repository: MatchRepository) {
    suspend operator fun invoke(matchId: String): Result<MatchModel, ClubError> = repository.clearManualScore(matchId)
}
