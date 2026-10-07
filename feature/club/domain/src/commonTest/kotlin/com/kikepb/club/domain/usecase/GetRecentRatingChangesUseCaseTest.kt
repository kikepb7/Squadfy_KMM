package com.kikepb.club.domain.usecase

import com.kikepb.club.domain.error.ClubError
import com.kikepb.club.domain.model.MatchEventType
import com.kikepb.club.domain.model.MatchFormat
import com.kikepb.club.domain.model.MatchModel
import com.kikepb.club.domain.model.MatchStatus
import com.kikepb.club.domain.model.TeamBalanceModel
import com.kikepb.club.domain.repository.MatchRepository
import com.kikepb.core.domain.util.Result
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Instant

class GetRecentRatingChangesUseCaseTest {

    private class CompletedMatches(private val matches: List<MatchModel>) : MatchRepository {
        var requestedStatus: MatchStatus? = null
        override suspend fun getClubMatches(clubId: String, status: MatchStatus?): Result<List<MatchModel>, ClubError> {
            requestedStatus = status
            return Result.Success(matches)
        }
        override suspend fun getMatch(matchId: String) = TODO("not used")
        override suspend fun createExtraMatch(clubId: String, scheduledAt: Instant, format: MatchFormat?, durationMinutes: Int?) = TODO("not used")
        override suspend fun cancel(matchId: String) = TODO("not used")
        override suspend fun complete(matchId: String) = TODO("not used")
        override suspend fun reopen(matchId: String) = TODO("not used")
        override suspend fun generateTeams(matchId: String, teamA: List<String>?, teamB: List<String>?) = TODO("not used")
        override suspend fun getTeamBalance(matchId: String): Result<TeamBalanceModel?, ClubError> = TODO("not used")
        override suspend fun addEvent(matchId: String, clubMemberId: String, type: MatchEventType, minute: Int?) = TODO("not used")
        override suspend fun deleteEvent(matchId: String, eventId: String) = TODO("not used")
        override suspend fun setMinutes(matchId: String, clubMemberId: String, minutes: Int) = TODO("not used")
        override suspend fun setManualScore(matchId: String, teamAScore: Int, teamBScore: Int) = TODO("not used")
        override suspend fun clearManualScore(matchId: String) = TODO("not used")
    }

    @Test
    fun `AC-008-08 recent rating changes are newest first and skip matches the member did not play`() = runTest {
        val repository = CompletedMatches(
            listOf(
                completed("old", "2026-09-01T18:00:00Z", mapOf("me" to 8)),
                completed("new", "2026-10-01T18:00:00Z", mapOf("me" to -5)),
                completed("absent", "2026-09-15T18:00:00Z", mapOf("other" to 3))
            )
        )

        val changes = (GetRecentRatingChangesUseCase(repository)("club", "me") as Result.Success).data

        assertEquals(MatchStatus.COMPLETED, repository.requestedStatus)
        assertEquals(listOf("new" to -5, "old" to 8), changes.map { it.matchId to it.change })
    }

    private fun completed(id: String, at: String, changes: Map<String, Int>) = MatchModel(
        id = id, clubId = "club", scheduledAt = Instant.parse(at), status = MatchStatus.COMPLETED, enrolledPlayers = emptyList(),
        enrolledGuests = emptyList(), teamA = emptyList(), teamB = emptyList(), teamAGuests = emptyList(), teamBGuests = emptyList(),
        durationMinutes = 60, minutesPlayed = emptyMap(), teamAScore = 0, teamBScore = 0, isManualScore = false, events = emptyList(),
        ratingChanges = changes, scheduleDate = null
    )
}
