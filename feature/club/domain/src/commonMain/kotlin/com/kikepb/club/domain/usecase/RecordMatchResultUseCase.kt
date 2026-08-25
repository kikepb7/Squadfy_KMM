package com.kikepb.club.domain.usecase

import com.kikepb.club.domain.model.ClubMatchModel
import com.kikepb.club.domain.repository.ClubRepository
import com.kikepb.club.domain.repository.PlayerStatInput
import com.kikepb.core.domain.util.DataError
import com.kikepb.core.domain.util.Result

class RecordMatchResultUseCase(private val clubRepository: ClubRepository) {
    suspend operator fun invoke(
        matchId: String,
        teamAScore: Int,
        teamBScore: Int,
        playerStats: List<PlayerStatInput>
    ): Result<ClubMatchModel, DataError.Remote> =
        clubRepository.recordMatchResult(matchId = matchId, teamAScore = teamAScore, teamBScore = teamBScore, playerStats = playerStats)
}
