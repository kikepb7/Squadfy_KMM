package com.kikepb.club.domain.usecase

import com.kikepb.club.domain.model.MatchSignupModel
import com.kikepb.club.domain.repository.ClubRepository
import com.kikepb.core.domain.util.DataError
import com.kikepb.core.domain.util.Result

class ListSignupsUseCase(private val clubRepository: ClubRepository) {
    suspend operator fun invoke(matchId: String): Result<List<MatchSignupModel>, DataError.Remote> =
        clubRepository.listSignups(matchId = matchId)
}
