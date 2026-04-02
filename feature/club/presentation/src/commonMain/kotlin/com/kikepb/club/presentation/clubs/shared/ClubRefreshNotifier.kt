package com.kikepb.club.presentation.clubs.shared

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

class ClubRefreshNotifier {
    private val _refreshEvents = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val refreshEvents = _refreshEvents.asSharedFlow()

    fun requestRefresh() {
        _refreshEvents.tryEmit(Unit)
    }
}
