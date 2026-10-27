package com.kikepb.club.presentation.fake

import com.kikepb.club.domain.error.ClubError
import com.kikepb.club.domain.model.SeasonModel
import com.kikepb.club.domain.model.SeasonStatus
import com.kikepb.club.domain.repository.SeasonRepository
import com.kikepb.core.domain.util.Result
import kotlinx.datetime.LocalDate

fun season(startYear: Int, status: SeasonStatus, startDay: Int = 1, endDay: Int = 30) = SeasonModel(
    id = "$startYear-${(startYear + 1) % 100}",
    name = "$startYear/${(startYear + 1) % 100}",
    startsOn = LocalDate(startYear, 9, startDay),
    endsOn = LocalDate(startYear + 1, 6, endDay),
    status = status
)

class FakeSeasonRepository : SeasonRepository {
    var seasons: List<SeasonModel> = emptyList()
    var updateResult: Result<SeasonModel, ClubError>? = null
    val updates = mutableListOf<Triple<String, LocalDate, LocalDate>>()

    override suspend fun getSeasons(clubId: String): Result<List<SeasonModel>, ClubError> = Result.Success(seasons)

    override suspend fun updateSeason(clubId: String, seasonId: String, startsOn: LocalDate, endsOn: LocalDate): Result<SeasonModel, ClubError> {
        updates += Triple(seasonId, startsOn, endsOn)
        return updateResult ?: Result.Success(seasons.first { it.id == seasonId }.copy(startsOn = startsOn, endsOn = endsOn))
    }
}
