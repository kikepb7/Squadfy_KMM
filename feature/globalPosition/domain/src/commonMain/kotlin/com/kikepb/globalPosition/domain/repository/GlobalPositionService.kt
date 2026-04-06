package com.kikepb.globalPosition.domain.repository

import com.kikepb.core.domain.util.Result
import com.kikepb.core.domain.util.DataError
import com.kikepb.globalPosition.domain.model.ClubModel
import com.kikepb.globalPosition.domain.model.MatchModel
import com.kikepb.globalPosition.domain.model.NewsModel

interface GlobalPositionService {
    suspend fun getUserClubs(): Result<List<ClubModel>, DataError.Remote>
    suspend fun getRecentMatches(): Result<List<MatchModel>, DataError.Remote>
    suspend fun getLatestNews(): Result<List<NewsModel>, DataError.Remote>
}