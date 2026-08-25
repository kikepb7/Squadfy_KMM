package com.kikepb.club.domain.usecase

import com.kikepb.club.domain.model.ClubMatchModel
import com.kikepb.club.domain.repository.ClubRepository
import com.kikepb.core.domain.util.DataError
import com.kikepb.core.domain.util.Result

class CreateMatchUseCase(private val clubRepository: ClubRepository) {
    suspend operator fun invoke(clubId: String, scheduledAt: String?, signupOpensAt: String?, signupClosesAt: String?): Result<ClubMatchModel, DataError.Remote> =
        clubRepository.createMatch(clubId = clubId, scheduledAt = scheduledAt, signupOpensAt = signupOpensAt, signupClosesAt = signupClosesAt)
}
