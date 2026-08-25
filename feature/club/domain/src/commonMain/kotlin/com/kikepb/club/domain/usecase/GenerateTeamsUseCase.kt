package com.kikepb.club.domain.usecase

import com.kikepb.club.domain.model.ClubMatchModel
import com.kikepb.club.domain.repository.ClubRepository
import com.kikepb.core.domain.util.DataError
import com.kikepb.core.domain.util.Result

class GenerateTeamsUseCase(private val clubRepository: ClubRepository) {
    suspend operator fun invoke(matchId: String): Result<ClubMatchModel, DataError.Remote> =
        clubRepository.generateTeams(matchId = matchId, mode = "AUTO", manualTeamA = null, manualTeamB = null)
}
