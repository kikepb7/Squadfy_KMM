package com.kikepb.chat.domain.usecases.message

import com.kikepb.chat.domain.fake.FakeChatRepository
import com.kikepb.chat.domain.fake.FakeMessageRepository
import com.kikepb.core.domain.util.DataError
import com.kikepb.core.domain.util.Result
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class FetchMessagesUseCaseTest {

    private val repository = FakeMessageRepository()
    private val useCase = FetchMessagesUseCase(messageRepository = repository)

    @Test
    fun `GIVEN repository returns messages WHEN fetchMessages THEN returns message list`() = runTest {
        val messages = listOf(FakeChatRepository.defaultMessageModel())
        repository.fetchMessagesResult = Result.Success(messages)

        val result = useCase.fetchMessages(chatId = "chat-1", before = null)

        assertIs<Result.Success<*>>(result)
        assertEquals(messages, (result as Result.Success).data)
    }

    @Test
    fun `GIVEN before cursor provided WHEN fetchMessages THEN delegates to repository`() = runTest {
        repository.fetchMessagesResult = Result.Success(emptyList())

        val result = useCase.fetchMessages(chatId = "chat-1", before = "some-cursor")

        assertIs<Result.Success<*>>(result)
        assertTrue((result as Result.Success).data.isEmpty())
    }

    @Test
    fun `GIVEN repository fails WHEN fetchMessages THEN returns error`() = runTest {
        repository.fetchMessagesResult = Result.Failure(error = DataError.Remote.SERVER_ERROR)

        val result = useCase.fetchMessages(chatId = "chat-1", before = null)

        assertIs<Result.Failure<*>>(result)
    }
}
