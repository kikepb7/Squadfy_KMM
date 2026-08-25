package com.kikepb.club.domain.usecase

import com.kikepb.club.domain.model.ClubScheduleExceptionModel
import com.kikepb.club.domain.repository.ClubRepository
import com.kikepb.core.domain.util.DataError
import com.kikepb.core.domain.util.Result

class ListScheduleExceptionsUseCase(private val clubRepository: ClubRepository) {
    suspend operator fun invoke(clubId: String): Result<List<ClubScheduleExceptionModel>, DataError.Remote> =
        clubRepository.listScheduleExceptions(clubId = clubId)
}
