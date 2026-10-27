package com.kikepb.club.domain.repository

import com.kikepb.club.domain.error.ClubError
import com.kikepb.club.domain.model.MyRating
import com.kikepb.club.domain.model.PlayerStats
import com.kikepb.club.domain.model.RatingEntry
import com.kikepb.club.domain.model.StatsEntry
import com.kikepb.club.domain.model.StatsSortBy
import com.kikepb.core.domain.util.Result
import kotlinx.datetime.LocalDate

/**
 * Rating and stats classifications (spec 008, BE-003/004). Network-first (ADR-0006). [seasonId] picks a season (spec
 * 020); null is the current one, or every match where the backend has no seasons (variant `api`).
 */
interface StandingsRepository {
    suspend fun getRatings(clubId: String, seasonId: String? = null): Result<List<RatingEntry>, ClubError>
    suspend fun getMyRating(clubId: String, seasonId: String? = null): Result<MyRating, ClubError>
    /** [period] null = the whole season (or every completed match); otherwise inclusive dates in the club time zone (backend spec 012 RN-B). */
    suspend fun getStats(
        clubId: String,
        sortBy: StatsSortBy,
        period: ClosedRange<LocalDate>? = null,
        seasonId: String? = null
    ): Result<List<StatsEntry>, ClubError>
    suspend fun getMyStats(clubId: String): Result<PlayerStats, ClubError>
}
