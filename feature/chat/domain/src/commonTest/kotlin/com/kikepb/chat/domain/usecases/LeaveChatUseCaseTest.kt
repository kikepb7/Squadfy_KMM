package com.kikepb.chat.domain.usecases

import com.kikepb.chat.domain.fake.FakeChatRepository
import com.kikepb.core.domain.util.DataError
import com.kikepb.core.domain.util.Result
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class LeaveChatUseCaseTest {

    private val repository = FakeChatRepository()
    private val useCase = LeaveChatUseCase(chatRepository = repository)

    @Test
    fun `GIVEN repository returns success WHEN leaveChat THEN returns success`() = runTest {
        repository.leaveChatResult = Result.Success(Unit)

        val result = useCase.leaveChat(chatId = "chat-1")

        assertIs<Result.Success<*>>(result)
    }

    @Test
    fun `GIVEN repository returns failure WHEN leaveChat THEN returns error`() = runTest {
        repository.leaveChatResult = Result.Failure(error = DataError.Remote.NOT_FOUND)

        val result = useCase.leaveChat(chatId = "chat-1")

        assertIs<Result.Failure<*>>(result)
        assertEquals(DataError.Remote.NOT_FOUND, (result as Result.Failure).error)
    }
}
