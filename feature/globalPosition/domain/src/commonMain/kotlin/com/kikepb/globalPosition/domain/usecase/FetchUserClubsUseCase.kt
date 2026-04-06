package com.kikepb.globalPosition.domain.usecase

import com.kikepb.core.domain.util.DataError
import com.kikepb.core.domain.util.EmptyResult
import com.kikepb.globalPosition.domain.repository.GlobalPositionRepository

class FetchUserClubsUseCase(private val repository: GlobalPositionRepository) {
    suspend operator fun invoke(): EmptyResult<DataError.Remote> = repository.fetchUserClubs()
}
