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
import com.kikepb.globalPosition.domain.model.MatchModel
import com.kikepb.globalPosition.domain.model.MatchStatus
import com.kikepb.globalPosition.domain.model.NewsModel
import com.kikepb.globalPosition.domain.repository.GlobalPositionRepository
import io.ktor.client.HttpClient
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class OfflineFirstGlobalPositionRepositoryImpl(
    private val httpClient: HttpClient,
    private val db: SquadfyClubDatabase
) : GlobalPositionRepository {

    // ── Offline-first: DB as source of truth ───────────────────────────────

    override fun getUserClubs(): Flow<List<ClubModel>> =
        db.clubDao.observeAllClubs()
            .map { entities -> entities.map { it.toGlobalPositionDomain() } }

    override suspend fun fetchUserClubs(): EmptyResult<DataError.Remote> =
        httpClient.get<List<ClubDto>>(route = "/club")
            .onSuccess { clubs ->
                db.clubDao.syncClubs(clubs = clubs.map { it.toEntity() })
            }
            .asEmptyResult()

    // ── Matches and News (mocks, unchanged) ────────────────────────────────

    override suspend fun getRecentMatches(): Result<List<MatchModel>, DataError.Remote> =
        Result.Success(data = mockMatches)

    override suspend fun getLatestNews(): Result<List<NewsModel>, DataError.Remote> =
        Result.Success(data = mockNews)

    private val mockMatches = listOf(
        MatchModel(
            id = "m1",
            homeTeamName = "Team 1",
            homeTeamLogoUrl = null,
            awayTeamName = "Team 2",
            awayTeamLogoUrl = null,
            homeScore = 3,
            awayScore = 1,
            date = "Ayer · 19:00",
            competition = "Liga Amateur",
            status = MatchStatus.FINISHED
        ),
        MatchModel(
            id = "m2",
            homeTeamName = "Team 1",
            homeTeamLogoUrl = null,
            awayTeamName = "Team 2",
            awayTeamLogoUrl = null,
            homeScore = 2,
            awayScore = 2,
            date = "Hace 3 días",
            competition = "Copa Verano",
            status = MatchStatus.FINISHED
        ),
        MatchModel(
            id = "m3",
            homeTeamName = "Team 1",
            homeTeamLogoUrl = null,
            awayTeamName = "Team 2",
            awayTeamLogoUrl = null,
            homeScore = null,
            awayScore = null,
            date = "Mañana · 20:30",
            competition = "Liga Amateur",
            status = MatchStatus.SCHEDULED
        )
    )

    private val mockNews = listOf(
        NewsModel(
            id = "n1",
            title = "Arranca la temporada de verano 2025",
            summary = "La liga amateur abre el plazo de inscripción para los equipos de la provincia a partir del próximo lunes.",
            imageUrl = null,
            publishedAt = "Hace 2 horas",
            category = "Liga",
            source = "Squadfy News"
        ),
        NewsModel(
            id = "n2",
            title = "Torneo de fútbol 7: más de 40 equipos inscritos",
            summary = "El torneo más esperado del año supera todas las expectativas con un récord histórico de participación.",
            imageUrl = null,
            publishedAt = "Ayer",
            category = "Torneos",
            source = "Squadfy News"
        ),
        NewsModel(
            id = "n3",
            title = "Consejos para evitar lesiones en la pretemporada",
            summary = "Expertos en medicina deportiva dan las claves para iniciar la temporada con buen pie y reducir el riesgo de lesiones.",
            imageUrl = null,
            publishedAt = "Hace 3 días",
            category = "Salud",
            source = "Squadfy Blog"
        ),
        NewsModel(
            id = "n4",
            title = "Nuevo sistema de clasificación en la app",
            summary = "Squadfy estrena una clasificación mejorada con estadísticas detalladas por jugador y equipo en tiempo real.",
            imageUrl = null,
            publishedAt = "Hace 5 días",
            category = "App",
            source = "Squadfy"
        )
    )
}
