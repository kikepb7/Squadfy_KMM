package com.kikepb.globalPosition.domain.usecase

import com.kikepb.core.domain.util.DataError
import com.kikepb.core.domain.util.Result
import com.kikepb.globalPosition.domain.model.NewsModel
import com.kikepb.globalPosition.domain.repository.GlobalPositionRepository

class GetLatestNewsUseCase(private val repository: GlobalPositionRepository) {
    suspend operator fun invoke(): Result<List<NewsModel>, DataError.Remote> = repository.getLatestNews()
}
