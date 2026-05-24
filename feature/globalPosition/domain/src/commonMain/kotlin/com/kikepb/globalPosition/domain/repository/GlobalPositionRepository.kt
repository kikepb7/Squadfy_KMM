package com.kikepb.globalPosition.domain.repository

import com.kikepb.core.domain.util.DataError
import com.kikepb.core.domain.util.EmptyResult
import com.kikepb.core.domain.util.Result
import com.kikepb.globalPosition.domain.model.ClubModel
import com.kikepb.globalPosition.domain.model.MatchModel
import com.kikepb.globalPosition.domain.model.NewsModel
import kotlinx.coroutines.flow.Flow

interface GlobalPositionRepository {
    fun getUserClubs(): Flow<List<ClubModel>>
    suspend fun fetchUserClubs(): EmptyResult<DataError.Remote>
    fun observeNextMatch(): Flow<Pair<MatchModel, Boolean>?>
    suspend fun toggleMatchParticipation(matchId: String): EmptyResult<DataError.Remote>
    suspend fun getRecentMatches(): Result<List<MatchModel>, DataError.Remote>
    suspend fun getLatestNews(): Result<List<NewsModel>, DataError.Remote>
}
