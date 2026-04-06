package com.kikepb.globalPosition.domain.usecase

import com.kikepb.globalPosition.domain.model.ClubModel
import com.kikepb.globalPosition.domain.repository.GlobalPositionRepository
import kotlinx.coroutines.flow.Flow

class GetUserClubsUseCase(private val repository: GlobalPositionRepository) {
    operator fun invoke(): Flow<List<ClubModel>> = repository.getUserClubs()
}
