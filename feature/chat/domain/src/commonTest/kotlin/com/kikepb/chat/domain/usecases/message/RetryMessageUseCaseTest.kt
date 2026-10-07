package com.kikepb.chat.domain.usecases.message

import com.kikepb.chat.domain.fake.FakeMessageRepository
import com.kikepb.core.domain.util.DataError
import com.kikepb.core.domain.util.Result
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class RetryMessageUseCaseTest {

    private val repository = FakeMessageRepository()
    private val useCase = RetryMessageUseCase(messageRepository = repository)

    @Test
    fun `GIVEN valid messageId WHEN retryMessage THEN delegates to repository`() = runTest {
        repository.retryMessageResult = Result.Success(Unit)

        val result = useCase.retryMessage(messageId = "msg-1")

        assertIs<Result.Success<*>>(result)
        assertEquals("msg-1", repository.lastRetryMessageId)
    }

    @Test
    fun `GIVEN repository fails WHEN retryMessage THEN returns error`() = runTest {
        repository.retryMessageResult = Result.Failure(error = DataError.ConnectionModel.MESSAGE_SEND_FAILED)

        val result = useCase.retryMessage(messageId = "msg-1")

        assertIs<Result.Failure<*>>(result)
    }
}
