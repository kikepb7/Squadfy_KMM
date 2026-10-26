package com.kikepb.chat.presentation.fake

import com.kikepb.chat.domain.repository.presence.PresenceRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class FakePresenceRepository : PresenceRepository {

    val online = MutableStateFlow(false)
    val typing = MutableStateFlow<Set<String>>(emptySet())
    val typingCalls = mutableListOf<Boolean>()

    override fun observeOnline(userId: String): Flow<Boolean> = online
    override fun observeTyping(chatId: String): Flow<Set<String>> = typing
    override suspend fun setTyping(chatId: String, typing: Boolean) { typingCalls += typing }
}
