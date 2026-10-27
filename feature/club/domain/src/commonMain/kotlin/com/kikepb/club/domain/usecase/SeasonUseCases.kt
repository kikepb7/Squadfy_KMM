package com.kikepb.club.domain.usecase

import com.kikepb.club.domain.error.ClubError
import com.kikepb.club.domain.model.SeasonDatesPolicy
import com.kikepb.club.domain.model.SeasonModel
import com.kikepb.club.domain.repository.SeasonRepository
import com.kikepb.core.domain.util.Error
import com.kikepb.core.domain.util.Result
import com.kikepb.core.domain.util.mapError
import kotlinx.datetime.LocalDate
import com.kikepb.core.domain.analytics.AnalyticsTracker
import com.kikepb.core.domain.analytics.AnalyticsEvents
import com.kikepb.core.domain.analytics.NoOpAnalyticsTracker
import com.kikepb.core.domain.util.onSuccess

class GetSeasonsUseCase(private val seasonRepository: SeasonRepository) {
    suspend operator fun invoke(clubId: String): Result<List<SeasonModel>, ClubError> = seasonRepository.getSeasons(clubId)
}

sealed interface UpdateSeasonError : Error {
    /** AC-020-03: not a day of September and one of June of the next year. */
    data object InvalidDates : UpdateSeasonError
    /** A closed season cannot change. */
    data object Closed : UpdateSeasonError
    data class Remote(val error: ClubError) : UpdateSeasonError
}

/** Owner and admins pick the start and end days of the season in progress or the next one (APP-RN-S3). */
class UpdateSeasonUseCase(
    private val seasonRepository: SeasonRepository,
    private val analytics: AnalyticsTracker = NoOpAnalyticsTracker
) {
    suspend operator fun invoke(clubId: String, season: SeasonModel, startsOn: LocalDate, endsOn: LocalDate): Result<SeasonModel, UpdateSeasonError> {
        if (!season.isEditable) return Result.Failure(UpdateSeasonError.Closed)
        if (!SeasonDatesPolicy.isValid(season.startYear, startsOn, endsOn)) return Result.Failure(UpdateSeasonError.InvalidDates)
        return seasonRepository.updateSeason(clubId, season.id, startsOn, endsOn)
            .onSuccess { analytics.logEvent(AnalyticsEvents.seasonUpdated()) }
            .mapError { UpdateSeasonError.Remote(it) }
    }
}
