package com.kikepb.club.data.datasource.remote

import com.kikepb.club.domain.error.ClubError
import com.kikepb.club.domain.model.SeasonModel
import com.kikepb.club.domain.repository.SeasonRepository
import com.kikepb.core.domain.util.Result
import kotlinx.datetime.LocalDate

/** The frozen own backend has no seasons (spec 020 gap): the app hides the season selector and settings. */
class NoSeasonRepository : SeasonRepository {
    override suspend fun getSeasons(clubId: String): Result<List<SeasonModel>, ClubError> = Result.Success(emptyList())

    override suspend fun updateSeason(clubId: String, seasonId: String, startsOn: LocalDate, endsOn: LocalDate): Result<SeasonModel, ClubError> =
        Result.Failure(ClubError.NotFound)
}
