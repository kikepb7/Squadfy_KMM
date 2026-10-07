package com.kikepb.club.data.datasource.remote

import com.kikepb.club.data.dto.PlayerRatingDTO
import com.kikepb.club.data.dto.PlayerStatsDTO
import com.kikepb.club.data.dto.RatingLeaderboardEntryDTO
import com.kikepb.club.data.mappers.toDomain
import com.kikepb.club.data.mappers.toStatsEntries
import com.kikepb.club.domain.error.ClubError
import com.kikepb.club.domain.error.toClubError
import com.kikepb.club.domain.model.MyRating
import com.kikepb.club.domain.model.PlayerStats
import com.kikepb.club.domain.model.RatingEntry
import com.kikepb.club.domain.model.StatsEntry
import com.kikepb.club.domain.model.StatsSortBy
import com.kikepb.club.domain.repository.StandingsRepository
import com.kikepb.core.data.networking.apiGet
import com.kikepb.core.domain.util.Result
import com.kikepb.core.domain.util.map
import com.kikepb.core.domain.util.mapError
import io.ktor.client.HttpClient

/** Classifications are network-first (ADR-0006): the screen reloads them on open, so a completed match shows up at once. */
class KtorStandingsRepository(private val httpClient: HttpClient) : StandingsRepository {

    override suspend fun getRatings(clubId: String): Result<List<RatingEntry>, ClubError> =
        httpClient.apiGet<List<RatingLeaderboardEntryDTO>>(route = "/clubs/$clubId/ratings")
            .mapError { it.toClubError() }.map { list -> list.map { it.toDomain() } }

    override suspend fun getMyRating(clubId: String): Result<MyRating, ClubError> =
        httpClient.apiGet<PlayerRatingDTO>(route = "/clubs/$clubId/ratings/me").mapError { it.toClubError() }.map { it.toDomain() }

    override suspend fun getStats(clubId: String, sortBy: StatsSortBy): Result<List<StatsEntry>, ClubError> =
        httpClient.apiGet<List<PlayerStatsDTO>>(route = "/clubs/$clubId/stats", queryParams = mapOf("sortBy" to sortBy.name))
            .mapError { it.toClubError() }.map { it.toStatsEntries() }

    override suspend fun getMyStats(clubId: String): Result<PlayerStats, ClubError> =
        httpClient.apiGet<PlayerStatsDTO>(route = "/clubs/$clubId/stats/me").mapError { it.toClubError() }.map { it.toDomain() }
}
