package com.kikepb.chat.domain.usecases

import com.kikepb.chat.domain.fake.FakeChatRepository
import com.kikepb.core.domain.util.DataError
import com.kikepb.core.domain.util.Result
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class FetchChatByIdUseCaseTest {

    private val repository = FakeChatRepository()
    private val useCase = FetchChatByIdUseCase(chatRepository = repository)

    @Test
    fun `GIVEN repository returns success WHEN fetchChatById THEN returns success`() = runTest {
        repository.fetchChatByIdResult = Result.Success(Unit)

        val result = useCase.fetchChatById(chatId = "chat-1")

        assertIs<Result.Success<*>>(result)
    }

    @Test
    fun `GIVEN repository returns failure WHEN fetchChatById THEN returns error`() = runTest {
        repository.fetchChatByIdResult = Result.Failure(error = DataError.Remote.NOT_FOUND)

        val result = useCase.fetchChatById(chatId = "chat-999")

        assertIs<Result.Failure<*>>(result)
        assertEquals(DataError.Remote.NOT_FOUND, (result as Result.Failure).error)
    }
}
