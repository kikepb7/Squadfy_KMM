package com.kikepb.globalPosition.domain.usecase

import com.kikepb.core.domain.util.DataError
import com.kikepb.core.domain.util.Result
import com.kikepb.globalPosition.domain.model.MatchModel
import com.kikepb.globalPosition.domain.repository.GlobalPositionRepository

class GetRecentMatchesUseCase(private val repository: GlobalPositionRepository) {
    suspend operator fun invoke(): Result<List<MatchModel>, DataError.Remote> = repository.getRecentMatches()
}
