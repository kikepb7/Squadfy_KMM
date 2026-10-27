package com.kikepb.club.domain.repository

import com.kikepb.club.domain.error.ClubError
import com.kikepb.club.domain.model.SeasonModel
import com.kikepb.core.domain.util.Result
import kotlinx.datetime.LocalDate

/** Seasons of a club (spec 020). The frozen own backend has none: its repository answers an empty list. */
interface SeasonRepository {
    /** Newest first. */
    suspend fun getSeasons(clubId: String): Result<List<SeasonModel>, ClubError>
    suspend fun updateSeason(clubId: String, seasonId: String, startsOn: LocalDate, endsOn: LocalDate): Result<SeasonModel, ClubError>
}
