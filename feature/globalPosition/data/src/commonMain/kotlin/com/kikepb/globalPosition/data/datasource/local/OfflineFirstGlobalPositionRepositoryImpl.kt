package com.kikepb.globalPosition.data.datasource.local

import com.kikepb.club.database.SquadfyClubDatabase
import com.kikepb.core.data.networking.get
import com.kikepb.core.domain.util.DataError
import com.kikepb.core.domain.util.EmptyResult
import com.kikepb.core.domain.util.Result
import com.kikepb.core.domain.util.asEmptyResult
import com.kikepb.core.domain.util.onSuccess
import com.kikepb.globalPosition.data.dto.ClubDto
import com.kikepb.globalPosition.data.mapper.toEntity
import com.kikepb.globalPosition.data.mapper.toGlobalPositionDomain
import com.kikepb.globalPosition.domain.model.ClubModel
import com.kikepb.globalPosition.domain.model.MatchEvent
import com.kikepb.globalPosition.domain.model.MatchModel
import com.kikepb.globalPosition.domain.model.MatchStatus
import com.kikepb.globalPosition.domain.model.NewsModel
import com.kikepb.globalPosition.domain.repository.GlobalPositionRepository
import io.ktor.client.HttpClient
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlin.time.Clock.System
import kotlin.time.Duration.Companion.days

class OfflineFirstGlobalPositionRepositoryImpl(
    private val httpClient: HttpClient,
    private val db: SquadfyClubDatabase
) : GlobalPositionRepository {

    private val _participatingMatchIds = MutableStateFlow<Set<String>>(emptySet())

    override fun getUserClubs(): Flow<List<ClubModel>> =
        db.clubDao.observeAllClubs()
            .map { entities -> entities.map { it.toGlobalPositionDomain() } }

    override suspend fun fetchUserClubs(): EmptyResult<DataError.Remote> =
        httpClient.get<List<ClubDto>>(route = "/club")
            .onSuccess { clubs ->
                db.clubDao.syncClubs(clubs = clubs.map { it.toEntity() })
            }
            .asEmptyResult()

    override fun observeNextMatch(): Flow<Pair<MatchModel, Boolean>?> =
        mockNextMatch?.let { match ->
            combine(flowOf(match), _participatingMatchIds) { m, participating ->
                Pair(m, m.id in participating)
            }
        } ?: flowOf(null)

    override suspend fun toggleMatchParticipation(matchId: String): EmptyResult<DataError.Remote> {
        _participatingMatchIds.value = _participatingMatchIds.value.let { current ->
            if (matchId in current) current - matchId else current + matchId
        }
        return Result.Success(Unit)
    }

    override suspend fun getRecentMatches(): Result<List<MatchModel>, DataError.Remote> =
        Result.Success(data = mockRecentMatches)

    override suspend fun getLatestNews(): Result<List<NewsModel>, DataError.Remote> =
        Result.Success(data = mockNews)


    private val now = System.now()
    private val mockNextMatch = MatchModel(
        id = "next_1",
        clubId = "",
        scheduledAt = now + 4.days,
        status = MatchStatus.SCHEDULED,
        enrolledPlayers = listOf("player_1", "player_2", "player_3"),
        teamA = emptyList(),
        teamB = emptyList(),
        teamAScore = 0,
        teamBScore = 0,
        goals = emptyList(),
        assists = emptyList(),
        yellowCards = emptyList(),
        redCards = emptyList()
    )

    private val mockRecentMatches = listOf(
        MatchModel(
            id = "m1",
            clubId = "",
            scheduledAt = now - 3.days,
            status = MatchStatus.FINISHED,
            enrolledPlayers = listOf("player_1", "player_2", "player_3", "player_4"),
            teamA = listOf("player_1", "player_2"),
            teamB = listOf("player_3", "player_4"),
            teamAScore = 3,
            teamBScore = 1,
            goals = listOf(
                MatchEvent(playerId = "player_1", minute = 15),
                MatchEvent(playerId = "player_1", minute = 42),
                MatchEvent(playerId = "player_2", minute = 67)
            ),
            assists = listOf(
                MatchEvent(playerId = "player_2", minute = 15),
                MatchEvent(playerId = "player_1", minute = 42)
            ),
            yellowCards = emptyList(),
            redCards = emptyList()
        ),
        MatchModel(
            id = "m2",
            clubId = "",
            scheduledAt = now - 10.days,
            status = MatchStatus.FINISHED,
            enrolledPlayers = listOf("player_1", "player_3", "player_5"),
            teamA = listOf("player_1"),
            teamB = listOf("player_3", "player_5"),
            teamAScore = 2,
            teamBScore = 0,
            goals = listOf(
                MatchEvent(playerId = "player_1", minute = 20),
                MatchEvent(playerId = "player_1", minute = 55)
            ),
            assists = emptyList(),
            yellowCards = listOf(MatchEvent(playerId = "player_3", minute = 38)),
            redCards = emptyList()
        ),
        MatchModel(
            id = "m3",
            clubId = "",
            scheduledAt = now - 17.days,
            status = MatchStatus.FINISHED,
            enrolledPlayers = listOf("player_2", "player_3", "player_4", "player_5"),
            teamA = listOf("player_2", "player_3"),
            teamB = listOf("player_4", "player_5"),
            teamAScore = 2,
            teamBScore = 2,
            goals = listOf(
                MatchEvent(playerId = "player_2", minute = 10),
                MatchEvent(playerId = "player_4", minute = 30),
                MatchEvent(playerId = "player_3", minute = 60),
                MatchEvent(playerId = "player_5", minute = 80)
            ),
            assists = emptyList(),
            yellowCards = emptyList(),
            redCards = emptyList()
        )
    )

    private val mockNews = listOf(
        NewsModel(
            id = "n1",
            title = "Victoria épica del Equipo Blanco en el derbi de la jornada",
            summary = "Carlos Ruiz marcó dos veces para darle la victoria al equipo en el partido más emocionante de la temporada.",
            imageUrl = null,
            publishedAt = "Hace 2h",
            category = "J10",
            source = "Squadfy News",
            clubName = "San Marcos FC",
            jornada = "J10"
        ),
        NewsModel(
            id = "n2",
            title = "Los Pinos suma su quinta victoria consecutiva",
            summary = "Raúl Medina y Kike Delgado firmaron los goles de la victoria ante un rival muy complicado.",
            imageUrl = null,
            publishedAt = "Ayer",
            category = "J6",
            source = "Squadfy News",
            clubName = "Los Pinos",
            jornada = "J6"
        ),
        NewsModel(
            id = "n3",
            title = "Convocatoria para la Jornada 11 ya disponible",
            summary = "El próximo partido es el sábado 26 de abril a las 17:00. Confirma tu asistencia antes del viernes.",
            imageUrl = null,
            publishedAt = "Hace 5h",
            category = "Aviso",
            source = "Squadfy",
            clubName = "San Marcos FC",
            jornada = null
        )
    )
}
