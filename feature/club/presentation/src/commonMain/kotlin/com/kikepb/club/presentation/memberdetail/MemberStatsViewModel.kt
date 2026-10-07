package com.kikepb.club.presentation.memberdetail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kikepb.club.domain.model.MatchRatingChange
import com.kikepb.club.domain.model.PlayerStats
import com.kikepb.club.domain.usecase.GetMyRatingUseCase
import com.kikepb.club.domain.usecase.GetMyStatsUseCase
import com.kikepb.club.domain.usecase.GetRatingLeaderboardUseCase
import com.kikepb.club.domain.usecase.GetRecentRatingChangesUseCase
import com.kikepb.club.domain.usecase.GetStatsLeaderboardUseCase
import com.kikepb.club.domain.usecase.ObserveMyMembershipUseCase
import com.kikepb.core.domain.util.onSuccess
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Stats, rating and recent match ratings of a member (AC-008-04/08). My own come from `/me`; another
 * member's are their rows of the club classifications, so nothing new is asked to the backend.
 */
class MemberStatsViewModel(
    private val observeMyMembershipUseCase: ObserveMyMembershipUseCase,
    private val getMyStatsUseCase: GetMyStatsUseCase,
    private val getMyRatingUseCase: GetMyRatingUseCase,
    private val getStatsLeaderboardUseCase: GetStatsLeaderboardUseCase,
    private val getRatingLeaderboardUseCase: GetRatingLeaderboardUseCase,
    private val getRecentRatingChangesUseCase: GetRecentRatingChangesUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val clubId = savedStateHandle.get<String>("clubId")
        ?: throw IllegalStateException("clubId is required")
    private val memberId = savedStateHandle.get<String>("memberId")
        ?: throw IllegalStateException("memberId is required")

    private val _state = MutableStateFlow(MemberStatsState())
    val state = _state.asStateFlow()

    init {
        viewModelScope.launch {
            val me = observeMyMembershipUseCase(clubId).filterNotNull().first()
            if (me.id == memberId) loadMine() else loadFromClassifications()
            getRecentRatingChangesUseCase(clubId, memberId).onSuccess { changes -> _state.update { it.copy(recentChanges = changes) } }
        }
    }

    private suspend fun loadMine() {
        getMyStatsUseCase(clubId).onSuccess { stats -> _state.update { it.copy(stats = stats) } }
        getMyRatingUseCase(clubId).onSuccess { mine -> _state.update { it.copy(rating = mine.rating, rank = mine.rank) } }
    }

    private suspend fun loadFromClassifications() {
        getStatsLeaderboardUseCase(clubId).onSuccess { rows ->
            _state.update { it.copy(stats = rows.firstOrNull { row -> row.stats.clubMemberId == memberId }?.stats) }
        }
        getRatingLeaderboardUseCase(clubId).onSuccess { rows ->
            rows.firstOrNull { it.clubMemberId == memberId }?.let { row -> _state.update { it.copy(rating = row.rating, rank = row.rank) } }
        }
    }
}

data class MemberStatsState(
    val stats: PlayerStats? = null,
    val rating: Int? = null,
    val rank: Int? = null,
    val recentChanges: List<MatchRatingChange> = emptyList()
)
