package com.kikepb.globalPosition.data.datasource.remote

import com.kikepb.core.data.networking.get
import com.kikepb.core.domain.util.DataError
import com.kikepb.core.domain.util.Result
import com.kikepb.core.domain.util.map
import com.kikepb.globalPosition.data.dto.ClubDto
import com.kikepb.globalPosition.data.dto.MatchDto
import com.kikepb.globalPosition.data.mapper.toDomain
import com.kikepb.globalPosition.domain.model.ClubModel
import com.kikepb.globalPosition.domain.model.MatchModel
import com.kikepb.globalPosition.domain.model.NewsModel
import com.kikepb.globalPosition.domain.repository.GlobalPositionService
import io.ktor.client.HttpClient

class KtorGlobalPositionRepositoryImpl(
    private val httpClient: HttpClient
) : GlobalPositionService {

    override suspend fun getUserClubs(): Result<List<ClubModel>, DataError.Remote> =
        httpClient.get<List<ClubDto>>(route = "/club")
            .map { clubDto -> clubDto.map { it.toDomain() } }

    // TODO: replace with real endpoints when available
    override suspend fun getRecentMatches(): Result<List<MatchModel>, DataError.Remote> =
        httpClient.get<List<MatchDto>>(route = "/match/recent")
            .map { dtos -> dtos.map { it.toDomain() } }

    override suspend fun getLatestNews(): Result<List<NewsModel>, DataError.Remote> =
        Result.Success(data = mockNews)

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
