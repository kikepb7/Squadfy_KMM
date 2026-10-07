package com.kikepb.club.domain.repository

import com.kikepb.club.domain.error.ClubError
import com.kikepb.core.domain.util.Result

/** Per-club push mute (AC-009-05, BE-005). The waitlist promotion is sent even when muted. */
interface NotificationSettingsRepository {
    suspend fun isMuted(clubId: String): Result<Boolean, ClubError>
    suspend fun setMuted(clubId: String, muted: Boolean): Result<Boolean, ClubError>
}
