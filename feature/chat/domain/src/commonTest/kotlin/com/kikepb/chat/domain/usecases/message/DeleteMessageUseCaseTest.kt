package com.kikepb.chat.domain.usecases.message

import com.kikepb.chat.domain.fake.FakeMessageRepository
import com.kikepb.core.domain.util.DataError
import com.kikepb.core.domain.util.Result
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class DeleteMessageUseCaseTest {

    private val repository = FakeMessageRepository()
    private val useCase = DeleteMessageUseCase(messageRepository = repository)

    @Test
    fun `GIVEN valid messageId WHEN deleteMessage THEN delegates to repository`() = runTest {
        repository.deleteMessageResult = Result.Success(Unit)

        val result = useCase.deleteMessage(messageId = "msg-1")

        assertIs<Result.Success<*>>(result)
        assertEquals("msg-1", repository.lastDeletedMessageId)
    }

    @Test
    fun `GIVEN repository fails WHEN deleteMessage THEN returns error`() = runTest {
        repository.deleteMessageResult = Result.Failure(error = DataError.Remote.NOT_FOUND)

        val result = useCase.deleteMessage(messageId = "msg-1")

        assertIs<Result.Failure<*>>(result)
        assertEquals(DataError.Remote.NOT_FOUND, (result as Result.Failure).error)
    }
}
