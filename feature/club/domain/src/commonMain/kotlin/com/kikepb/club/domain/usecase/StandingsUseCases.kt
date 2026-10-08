package com.kikepb.club.domain.usecase

import kotlinx.datetime.LocalDate
import com.kikepb.club.domain.model.StatsPeriod
import com.kikepb.club.domain.error.ClubError
import com.kikepb.club.domain.model.MatchRatingChange
import com.kikepb.club.domain.model.MatchStatus
import com.kikepb.club.domain.model.MyRating
import com.kikepb.club.domain.model.PlayerStats
import com.kikepb.club.domain.model.RatingEntry
import com.kikepb.club.domain.model.StatsEntry
import com.kikepb.club.domain.model.StatsSortBy
import com.kikepb.club.domain.repository.MatchRepository
import com.kikepb.club.domain.repository.StandingsRepository
import com.kikepb.core.domain.util.Result
import com.kikepb.core.domain.util.map

class GetRatingLeaderboardUseCase(private val repository: StandingsRepository) {
    suspend operator fun invoke(clubId: String): Result<List<RatingEntry>, ClubError> = repository.getRatings(clubId)
}

class GetMyRatingUseCase(private val repository: StandingsRepository) {
    suspend operator fun invoke(clubId: String): Result<MyRating, ClubError> = repository.getMyRating(clubId)
}

class GetStatsLeaderboardUseCase(private val repository: StandingsRepository) {
    /** AC-015-07: [today] is the current date in the club time zone (APP-RN-03). */
    suspend operator fun invoke(
        clubId: String,
        sortBy: StatsSortBy = StatsSortBy.GOALS,
        period: StatsPeriod = StatsPeriod.ALL_TIME,
        today: LocalDate? = null
    ): Result<List<StatsEntry>, ClubError> =
        repository.getStats(clubId, sortBy, today?.let { period.rangeFor(it) })
}

class GetMyStatsUseCase(private val repository: StandingsRepository) {
    suspend operator fun invoke(clubId: String): Result<PlayerStats, ClubError> = repository.getMyStats(clubId)
}

/** AC-008-08: the member's rating variation in their last completed matches, newest first. */
class GetRecentRatingChangesUseCase(private val repository: MatchRepository) {
    suspend operator fun invoke(clubId: String, clubMemberId: String, limit: Int = DEFAULT_LIMIT): Result<List<MatchRatingChange>, ClubError> =
        repository.getClubMatches(clubId, MatchStatus.COMPLETED).map { matches ->
            matches
                .sortedByDescending { it.scheduledAt }
                .mapNotNull { match -> match.ratingChanges[clubMemberId]?.let { MatchRatingChange(match.id, match.scheduledAt, it) } }
                .take(limit)
        }

    companion object {
        const val DEFAULT_LIMIT = 5
    }
}

/** AC-008-06: nobody has played a completed match yet. */
fun List<RatingEntry>.noCompletedMatches(): Boolean = all { it.matchesRated == 0 }
