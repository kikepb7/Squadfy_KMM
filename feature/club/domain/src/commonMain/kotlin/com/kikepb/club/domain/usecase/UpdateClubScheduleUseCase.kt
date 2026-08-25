package com.kikepb.club.domain.usecase

import com.kikepb.club.domain.model.ClubModel
import com.kikepb.club.domain.repository.ClubRepository
import com.kikepb.core.domain.util.DataError
import com.kikepb.core.domain.util.Result

class UpdateClubScheduleUseCase(private val clubRepository: ClubRepository) {
    suspend operator fun invoke(
        clubId: String,
        matchDayOfWeek: String?,
        matchStartTime: String?,
        matchEndTime: String?,
        seasonStartMonth: Int?,
        seasonStartDay: Int?,
        drawTime: String?
    ): Result<ClubModel, DataError.Remote> =
        clubRepository.updateSchedule(
            clubId = clubId,
            matchDayOfWeek = matchDayOfWeek,
            matchStartTime = matchStartTime,
            matchEndTime = matchEndTime,
            seasonStartMonth = seasonStartMonth,
            seasonStartDay = seasonStartDay,
            drawTime = drawTime
        )
}
