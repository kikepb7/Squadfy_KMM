package com.kikepb.club.presentation.fake

import com.kikepb.club.domain.error.ClubError
import com.kikepb.club.domain.model.MyRating
import com.kikepb.club.domain.model.PlayerStats
import com.kikepb.club.domain.model.RatingEntry
import com.kikepb.club.domain.model.StatsEntry
import com.kikepb.club.domain.model.StatsSortBy
import com.kikepb.club.domain.repository.StandingsRepository
import com.kikepb.core.domain.util.Result

fun stats(memberId: String, goals: Int = 0, matches: Int = 0) = PlayerStats(
    clubMemberId = memberId, matchesPlayed = matches, wins = 0, draws = 0, losses = 0, goals = goals,
    assists = 0, yellowCards = 0, redCards = 0, minutesPlayed = matches * 60
)

class FakeStandingsRepository : StandingsRepository {
    var ratings: List<RatingEntry> = emptyList()
    var myRating: Result<MyRating, ClubError> = Result.Failure(ClubError.NotFound)
    var stats: List<StatsEntry> = emptyList()
    var myStats: PlayerStats? = null
    val calls = mutableListOf<String>()

    override suspend fun getRatings(clubId: String): Result<List<RatingEntry>, ClubError> {
        calls += "ratings"
        return Result.Success(ratings)
    }
    override suspend fun getMyRating(clubId: String): Result<MyRating, ClubError> {
        calls += "ratings/me"
        return myRating
    }
    override suspend fun getStats(clubId: String, sortBy: StatsSortBy): Result<List<StatsEntry>, ClubError> {
        calls += "stats:$sortBy"
        return Result.Success(stats)
    }
    override suspend fun getMyStats(clubId: String): Result<PlayerStats, ClubError> {
        calls += "stats/me"
        return myStats?.let { Result.Success(it) } ?: Result.Failure(ClubError.NotFound)
    }
}
