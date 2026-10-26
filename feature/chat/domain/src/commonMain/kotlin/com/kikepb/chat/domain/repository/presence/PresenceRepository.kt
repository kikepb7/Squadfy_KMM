package com.kikepb.chat.domain.repository.presence

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

/** "Online" and "typing…" in the chat (spec 019 AC-019-16), kept by Firebase Realtime Database. */
interface PresenceRepository {
    fun observeOnline(userId: String): Flow<Boolean>
    /** Users typing in [chatId] right now, except me. */
    fun observeTyping(chatId: String): Flow<Set<String>>
    suspend fun setTyping(chatId: String, typing: Boolean)
}

/** Builds without presence (the frozen own backend). */
object NoPresenceRepository : PresenceRepository {
    override fun observeOnline(userId: String): Flow<Boolean> = flowOf(false)
    override fun observeTyping(chatId: String): Flow<Set<String>> = flowOf(emptySet())
    override suspend fun setTyping(chatId: String, typing: Boolean) = Unit
}
