package com.kikepb.chat.domain.usecases

import com.kikepb.chat.domain.fake.FakeChatRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GetChatsUseCaseTest {

    private val repository = FakeChatRepository()
    private val useCase = GetChatsUseCase(chatRepository = repository)

    @Test
    fun `GIVEN no chats WHEN getChats THEN emits empty list`() = runTest {
        val chats = useCase.getChats().first()

        assertTrue(chats.isEmpty())
    }

    @Test
    fun `GIVEN chats in repository WHEN getChats THEN emits list`() = runTest {
        val expected = listOf(FakeChatRepository.defaultChatModel())
        repository.emitChats(expected)

        val chats = useCase.getChats().first()

        assertEquals(expected, chats)
    }
}
