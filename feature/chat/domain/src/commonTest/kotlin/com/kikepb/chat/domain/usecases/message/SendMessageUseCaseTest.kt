package com.kikepb.chat.domain.usecases.message

import com.kikepb.chat.domain.fake.FakeMessageRepository
import com.kikepb.chat.domain.models.OutgoingNewMessageModel
import com.kikepb.core.domain.util.DataError
import com.kikepb.core.domain.util.Result
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class SendMessageUseCaseTest {

    private val repository = FakeMessageRepository()
    private val useCase = SendMessageUseCase(messageRepository = repository)

    @Test
    fun `GIVEN valid message WHEN sendMessage THEN delegates to repository`() = runTest {
        val message = OutgoingNewMessageModel(
            chatId = "chat-1",
            messageId = "msg-1",
            content = "Hello!"
        )
        repository.sendMessageResult = Result.Success(Unit)

        val result = useCase.sendMessage(message = message)

        assertIs<Result.Success<*>>(result)
        assertEquals(message, repository.sentMessages.last())
    }

    @Test
    fun `GIVEN repository fails WHEN sendMessage THEN returns error`() = runTest {
        val message = OutgoingNewMessageModel(
            chatId = "chat-1",
            messageId = "msg-1",
            content = "Hello!"
        )
        repository.sendMessageResult = Result.Failure(error = DataError.ConnectionModel.MESSAGE_SEND_FAILED)

        val result = useCase.sendMessage(message = message)

        assertIs<Result.Failure<*>>(result)
    }
}
