package com.kikepb.chat.domain.usecases

import com.kikepb.chat.domain.fake.FakeChatRepository
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertTrue

class DeleteAllChatsUseCaseTest {

    private val repository = FakeChatRepository()
    private val useCase = DeleteAllChatsUseCase(chatRepository = repository)

    @Test
    fun `WHEN deleteAllChats THEN repository deleteAllChats is called`() = runTest {
        useCase.deleteAllChats()

        assertTrue(repository.deleteAllChatsCalled)
    }
}
