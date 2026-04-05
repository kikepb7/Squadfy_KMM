package com.kikepb.globalPosition.domain.usecase

import com.kikepb.core.domain.util.DataError
import com.kikepb.core.domain.util.Result
import com.kikepb.globalPosition.domain.model.ClubModel
import com.kikepb.globalPosition.domain.repository.GlobalPositionRepository

class GetUserClubsUseCase(private val repository: GlobalPositionRepository) {
    suspend operator fun invoke(): Result<List<ClubModel>, DataError.Remote> = repository.getUserClubs()
}
