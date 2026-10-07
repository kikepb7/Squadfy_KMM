package com.kikepb.core.domain.notification

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.update

/**
 * Pushes received while the app is in the foreground (AC-009-04). When the push belongs to a club whose
 * screens are visible, they refresh and show a snackbar instead of a system notification.
 */
class InAppPushCenter {

    // Several screens of the same club can be visible in turn (match tab, match detail): a count per club
    private val visibleClubs = MutableStateFlow<Map<String, Int>>(emptyMap())

    private val _messages = MutableSharedFlow<PushMessage>(extraBufferCapacity = 16)
    val messages: SharedFlow<PushMessage> = _messages.asSharedFlow()

    fun onClubVisible(clubId: String) = visibleClubs.update { it + (clubId to (it[clubId] ?: 0) + 1) }

    fun onClubHidden(clubId: String) = visibleClubs.update { current ->
        val count = (current[clubId] ?: 0) - 1
        if (count <= 0) current - clubId else current + (clubId to count)
    }

    fun isClubVisible(clubId: String): Boolean = (visibleClubs.value[clubId] ?: 0) > 0

    /** Returns true when a visible screen handles the push, so no system notification is needed. */
    fun onForegroundMessage(message: PushMessage): Boolean {
        val clubId = message.clubId ?: return false
        if (!isClubVisible(clubId)) return false
        return _messages.tryEmit(message)
    }
}

/** Remembers whether the notification permission was already requested (AC-009-01: only once, with an explanation). */
interface NotificationPromptStore {
    suspend fun wasAsked(): Boolean
    suspend fun markAsked()
}
