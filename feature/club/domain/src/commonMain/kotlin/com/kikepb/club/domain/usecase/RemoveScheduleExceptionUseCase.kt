package com.kikepb.club.domain.usecase

import com.kikepb.club.domain.repository.ClubRepository
import com.kikepb.core.domain.util.DataError
import com.kikepb.core.domain.util.EmptyResult

class RemoveScheduleExceptionUseCase(private val clubRepository: ClubRepository) {
    suspend operator fun invoke(clubId: String, exceptionId: String): EmptyResult<DataError.Remote> =
        clubRepository.removeScheduleException(clubId = clubId, exceptionId = exceptionId)
}
