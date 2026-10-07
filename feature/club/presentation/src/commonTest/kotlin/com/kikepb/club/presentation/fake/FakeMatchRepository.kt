package com.kikepb.club.presentation.fake

import com.kikepb.club.domain.error.ClubError
import com.kikepb.club.domain.model.MatchEventType
import com.kikepb.club.domain.model.MatchFormat
import com.kikepb.club.domain.model.MatchGuestModel
import com.kikepb.club.domain.model.MatchModel
import com.kikepb.club.domain.model.MatchStatus
import com.kikepb.club.domain.model.TeamBalanceModel
import com.kikepb.club.domain.repository.MatchRepository
import com.kikepb.core.domain.util.Result
import kotlin.time.Instant

fun match(
    status: MatchStatus = MatchStatus.SCHEDULED,
    enrolled: List<String> = listOf("me", "m-2", "m-3", "m-4"),
    teamA: List<String> = emptyList(),
    teamB: List<String> = emptyList(),
    teamAGuests: List<MatchGuestModel> = emptyList(),
    teamBGuests: List<MatchGuestModel> = emptyList(),
    scheduledAt: String = "2026-10-15T18:00:00Z"
) = MatchModel(
    id = "match-1", clubId = "club-1", scheduledAt = Instant.parse(scheduledAt), status = status,
    enrolledPlayers = enrolled, enrolledGuests = teamAGuests + teamBGuests, teamA = teamA, teamB = teamB,
    teamAGuests = teamAGuests, teamBGuests = teamBGuests, durationMinutes = 60, minutesPlayed = emptyMap(),
    teamAScore = 0, teamBScore = 0, isManualScore = false, events = emptyList(), ratingChanges = emptyMap(), scheduleDate = null
)

/** Records every call; each operation returns [next] (or [failure] when set). */
class FakeMatchRepository : MatchRepository {
    var current: Result<MatchModel, ClubError> = Result.Success(match())
    var balance: Result<TeamBalanceModel?, ClubError> = Result.Success(null)
    var clubMatches: Result<List<MatchModel>, ClubError> = Result.Success(emptyList())
    var next: MatchModel? = null
    var failure: ClubError? = null
    val calls = mutableListOf<String>()
    var lastTeams: Pair<List<String>?, List<String>?>? = null

    private fun answer(call: String): Result<MatchModel, ClubError> {
        calls += call
        failure?.let { return Result.Failure(it) }
        val updated = next ?: (current as Result.Success).data
        current = Result.Success(updated)
        return Result.Success(updated)
    }

    override suspend fun getMatch(matchId: String): Result<MatchModel, ClubError> {
        calls += "get"
        return current
    }
    override suspend fun getClubMatches(clubId: String, status: MatchStatus?) = clubMatches
    override suspend fun createExtraMatch(clubId: String, scheduledAt: Instant, format: MatchFormat?, durationMinutes: Int?) = answer("extra")
    override suspend fun cancel(matchId: String) = answer("cancel")
    override suspend fun complete(matchId: String) = answer("complete")
    override suspend fun reopen(matchId: String) = answer("reopen")
    override suspend fun generateTeams(matchId: String, teamA: List<String>?, teamB: List<String>?): Result<MatchModel, ClubError> {
        lastTeams = teamA to teamB
        return answer(if (teamA == null) "teams-auto" else "teams-manual")
    }
    override suspend fun getTeamBalance(matchId: String): Result<TeamBalanceModel?, ClubError> {
        calls += "balance"
        return balance
    }
    override suspend fun addEvent(matchId: String, clubMemberId: String, type: MatchEventType, minute: Int?) = answer("event-add:$clubMemberId:$type:$minute")
    override suspend fun deleteEvent(matchId: String, eventId: String) = answer("event-delete:$eventId")
    override suspend fun setMinutes(matchId: String, clubMemberId: String, minutes: Int) = answer("minutes:$clubMemberId:$minutes")
    override suspend fun setManualScore(matchId: String, teamAScore: Int, teamBScore: Int) = answer("score:$teamAScore-$teamBScore")
    override suspend fun clearManualScore(matchId: String) = answer("score-clear")
}
