package com.kikepb.club.domain.usecase

import com.kikepb.club.domain.model.MatchSignupModel
import com.kikepb.club.domain.repository.ClubRepository
import com.kikepb.core.domain.util.DataError
import com.kikepb.core.domain.util.Result

class SignUpForMatchUseCase(private val clubRepository: ClubRepository) {
    suspend operator fun invoke(matchId: String): Result<MatchSignupModel, DataError.Remote> =
        clubRepository.signUpForMatch(matchId = matchId)
}
