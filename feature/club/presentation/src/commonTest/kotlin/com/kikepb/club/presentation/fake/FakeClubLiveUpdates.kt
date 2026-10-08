package com.kikepb.club.presentation.fake

import com.kikepb.core.domain.realtime.ClubDataChange
import com.kikepb.core.domain.realtime.ClubLiveUpdates
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.filter

/** Emits what the test sends with [emit], like a `CLUB_DATA_CHANGED` from the socket. */
class FakeClubLiveUpdates : ClubLiveUpdates {
    private val changes = MutableSharedFlow<ClubDataChange>(extraBufferCapacity = 8)
    override fun observe(clubId: String): Flow<ClubDataChange> = changes.filter { it.clubId == clubId }
    suspend fun emit(change: ClubDataChange) = changes.emit(change)
}
