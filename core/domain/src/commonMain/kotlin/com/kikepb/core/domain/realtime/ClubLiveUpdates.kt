package com.kikepb.core.domain.realtime

import kotlinx.coroutines.flow.Flow

/** What changed in a club (backend spec 012 RN-A1): the app reloads that part by REST (RN-A2). */
enum class ClubDataScope {
    /** A match, its announcement, enrollments, guests, teams, events, score or minutes. */
    MATCH,
    /** The weekly schedule or its exceptions. */
    SCHEDULE,
    /** Member absences. */
    ABSENCES
}

data class ClubDataChange(
    val clubId: String,
    val scope: ClubDataScope,
    val matchId: String? = null
)

/**
 * Live notices for one club (spec 015 AC-015-06), carried by the same WebSocket as the chat (RN-A3).
 * Collecting keeps the connection open while a club screen is visible.
 */
interface ClubLiveUpdates {
    fun observe(clubId: String): Flow<ClubDataChange>
}
