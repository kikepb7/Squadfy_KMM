package com.kikepb.club.domain.usecase

import com.kikepb.club.domain.repository.ClubRepository
import com.kikepb.core.domain.util.DataError
import com.kikepb.core.domain.util.EmptyResult

class RemoveSignupUseCase(private val clubRepository: ClubRepository) {
    suspend operator fun invoke(matchId: String, signupId: String): EmptyResult<DataError.Remote> =
        clubRepository.removeSignup(matchId = matchId, signupId = signupId)
}
