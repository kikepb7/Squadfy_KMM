package com.kikepb.globalPosition.domain.repository

import com.kikepb.core.domain.util.DataError
import com.kikepb.core.domain.util.Result
import com.kikepb.globalPosition.domain.model.MatchModel
import com.kikepb.globalPosition.domain.model.NewsModel

/** Sample home sections behind HOME_RECENT_MATCHES / HOME_NEWS; clubs come from the club feature (spec 010). */
interface GlobalPositionRepository {
    suspend fun getRecentMatches(): Result<List<MatchModel>, DataError.Remote>
    suspend fun getLatestNews(): Result<List<NewsModel>, DataError.Remote>
}
