package com.kikepb.globalPosition.domain.usecase

import com.kikepb.globalPosition.domain.model.MatchModel
import com.kikepb.globalPosition.domain.repository.GlobalPositionRepository
import kotlinx.coroutines.flow.Flow

class GetNextMatchUseCase(private val repository: GlobalPositionRepository) {
    operator fun invoke(): Flow<Pair<MatchModel, Boolean>?> = repository.observeNextMatch()
}
