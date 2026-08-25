package com.kikepb.club.domain.usecase

import com.kikepb.club.domain.model.ClubScheduleExceptionModel
import com.kikepb.club.domain.repository.ClubRepository
import com.kikepb.core.domain.util.DataError
import com.kikepb.core.domain.util.Result

class AddScheduleExceptionUseCase(private val clubRepository: ClubRepository) {
    suspend operator fun invoke(clubId: String, date: String, reason: String?): Result<ClubScheduleExceptionModel, DataError.Remote> =
        clubRepository.addScheduleException(clubId = clubId, date = date, reason = reason)
}
