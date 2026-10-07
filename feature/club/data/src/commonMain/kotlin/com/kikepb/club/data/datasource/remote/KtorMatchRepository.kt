package com.kikepb.club.data.datasource.remote

import com.kikepb.club.data.dto.AddMatchEventRequestDTO
import com.kikepb.club.data.dto.CreateMatchRequestDTO
import com.kikepb.club.data.dto.GenerateTeamsRequestDTO
import com.kikepb.club.data.dto.MatchDTO
import com.kikepb.club.data.dto.MatchScoreRequestDTO
import com.kikepb.club.data.dto.SetPlayerMinutesRequestDTO
import com.kikepb.club.data.dto.TeamBalanceDTO
import com.kikepb.club.data.mappers.toDomain
import com.kikepb.club.domain.error.ClubError
import com.kikepb.club.domain.error.toClubError
import com.kikepb.club.domain.model.MatchEventType
import com.kikepb.club.domain.model.MatchFormat
import com.kikepb.club.domain.model.MatchModel
import com.kikepb.club.domain.model.MatchStatus
import com.kikepb.club.domain.model.TeamBalanceModel
import com.kikepb.club.domain.repository.MatchRepository
import com.kikepb.core.data.networking.apiDelete
import com.kikepb.core.data.networking.apiGet
import com.kikepb.core.data.networking.apiPost
import com.kikepb.core.data.networking.apiPut
import com.kikepb.core.domain.util.DataError
import com.kikepb.core.domain.util.RemoteError
import com.kikepb.core.domain.util.Result
import com.kikepb.core.domain.util.map
import com.kikepb.core.domain.util.mapError
import io.ktor.client.HttpClient
import kotlin.time.Instant

/** Matches are network-first (ADR-0006): the detail screen refreshes on open, after actions and on push. */
class KtorMatchRepository(private val httpClient: HttpClient) : MatchRepository {

    override suspend fun getMatch(matchId: String): Result<MatchModel, ClubError> =
        httpClient.apiGet<MatchDTO>(route = match(matchId)).asMatch()

    override suspend fun getClubMatches(clubId: String, status: MatchStatus?): Result<List<MatchModel>, ClubError> =
        httpClient.apiGet<List<MatchDTO>>(
            route = "/clubs/$clubId/matches",
            queryParams = status?.let { mapOf("status" to it.name) } ?: emptyMap()
        ).mapError { it.toClubError() }.map { list -> list.map { it.toDomain() } }

    override suspend fun createExtraMatch(
        clubId: String,
        scheduledAt: Instant,
        format: MatchFormat?,
        durationMinutes: Int?
    ): Result<MatchModel, ClubError> =
        httpClient.apiPost<CreateMatchRequestDTO, MatchDTO>(
            route = "/clubs/$clubId/matches",
            body = CreateMatchRequestDTO(scheduledAt = scheduledAt.toString(), format = format?.name, durationMinutes = durationMinutes)
        ).asMatch()

    override suspend fun cancel(matchId: String): Result<MatchModel, ClubError> =
        httpClient.apiPost<Unit, MatchDTO>(route = "${match(matchId)}/cancel", body = Unit).asMatch()

    override suspend fun complete(matchId: String): Result<MatchModel, ClubError> =
        httpClient.apiPost<Unit, MatchDTO>(route = "${match(matchId)}/complete", body = Unit).asMatch()

    override suspend fun reopen(matchId: String): Result<MatchModel, ClubError> =
        httpClient.apiPost<Unit, MatchDTO>(route = "${match(matchId)}/reopen", body = Unit).asMatch()

    override suspend fun generateTeams(matchId: String, teamA: List<String>?, teamB: List<String>?): Result<MatchModel, ClubError> {
        val manual = teamA != null && teamB != null
        return httpClient.apiPost<GenerateTeamsRequestDTO, MatchDTO>(
            route = "${match(matchId)}/teams",
            body = GenerateTeamsRequestDTO(
                mode = if (manual) "MANUAL" else "AUTO",
                manualTeamA = teamA.takeIf { manual },
                manualTeamB = teamB.takeIf { manual }
            )
        ).asMatch()
    }

    override suspend fun getTeamBalance(matchId: String): Result<TeamBalanceModel?, ClubError> =
        when (val result = httpClient.apiGet<TeamBalanceDTO>(route = "${match(matchId)}/team-balance")) {
            is Result.Success -> Result.Success(result.data.toDomain())
            // 409 = no teams yet: the panel is hidden (AC-006-03)
            is Result.Failure -> if (result.error.status == DataError.Remote.CONFLICT) Result.Success(null) else Result.Failure(result.error.toClubError())
        }

    override suspend fun addEvent(matchId: String, clubMemberId: String, type: MatchEventType, minute: Int?): Result<MatchModel, ClubError> =
        httpClient.apiPost<AddMatchEventRequestDTO, MatchDTO>(
            route = "${match(matchId)}/events",
            body = AddMatchEventRequestDTO(clubMemberId = clubMemberId, type = type.name, minute = minute)
        ).asMatch()

    override suspend fun deleteEvent(matchId: String, eventId: String): Result<MatchModel, ClubError> =
        httpClient.apiDelete<MatchDTO>(route = "${match(matchId)}/events/$eventId").asMatch()

    override suspend fun setMinutes(matchId: String, clubMemberId: String, minutes: Int): Result<MatchModel, ClubError> =
        httpClient.apiPut<SetPlayerMinutesRequestDTO, MatchDTO>(
            route = "${match(matchId)}/players/$clubMemberId/minutes",
            body = SetPlayerMinutesRequestDTO(minutes)
        ).asMatch()

    override suspend fun setManualScore(matchId: String, teamAScore: Int, teamBScore: Int): Result<MatchModel, ClubError> =
        httpClient.apiPut<MatchScoreRequestDTO, MatchDTO>(
            route = "${match(matchId)}/score",
            body = MatchScoreRequestDTO(teamAScore = teamAScore, teamBScore = teamBScore)
        ).asMatch()

    override suspend fun clearManualScore(matchId: String): Result<MatchModel, ClubError> =
        httpClient.apiDelete<MatchDTO>(route = "${match(matchId)}/score").asMatch()

    private fun Result<MatchDTO, RemoteError>.asMatch(): Result<MatchModel, ClubError> =
        mapError { it.toClubError() }.map { it.toDomain() }

    private fun match(matchId: String) = "/matches/$matchId"
}
