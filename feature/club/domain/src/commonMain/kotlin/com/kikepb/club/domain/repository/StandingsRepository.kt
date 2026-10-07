package com.kikepb.club.domain.repository

import com.kikepb.club.domain.error.ClubError
import com.kikepb.club.domain.model.MyRating
import com.kikepb.club.domain.model.PlayerStats
import com.kikepb.club.domain.model.RatingEntry
import com.kikepb.club.domain.model.StatsEntry
import com.kikepb.club.domain.model.StatsSortBy
import com.kikepb.core.domain.util.Result

/** Rating and stats classifications (spec 008, BE-003/004). Network-first (ADR-0006). */
interface StandingsRepository {
    suspend fun getRatings(clubId: String): Result<List<RatingEntry>, ClubError>
    suspend fun getMyRating(clubId: String): Result<MyRating, ClubError>
    suspend fun getStats(clubId: String, sortBy: StatsSortBy): Result<List<StatsEntry>, ClubError>
    suspend fun getMyStats(clubId: String): Result<PlayerStats, ClubError>
}
