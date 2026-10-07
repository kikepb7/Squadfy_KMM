package com.kikepb.chat.domain.usecases

import com.kikepb.chat.domain.fake.FakeChatRepository
import com.kikepb.core.domain.util.DataError
import com.kikepb.core.domain.util.Result
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class FetchChatsUseCaseTest {

    private val repository = FakeChatRepository()
    private val useCase = FetchChatsUseCase(chatRepository = repository)

    @Test
    fun `GIVEN repository returns success WHEN fetchChats THEN returns chat list`() = runTest {
        val chats = listOf(FakeChatRepository.defaultChatModel())
        repository.fetchChatsResult = Result.Success(chats)

        val result = useCase.fetchChats()

        assertIs<Result.Success<*>>(result)
        assertEquals(chats, (result as Result.Success).data)
    }

    @Test
    fun `GIVEN repository returns failure WHEN fetchChats THEN returns error`() = runTest {
        repository.fetchChatsResult = Result.Failure(error = DataError.Remote.SERVER_ERROR)

        val result = useCase.fetchChats()

        assertIs<Result.Failure<*>>(result)
        assertEquals(DataError.Remote.SERVER_ERROR, (result as Result.Failure).error)
    }
}
