package com.kikepb.chat.domain.usecases

import com.kikepb.chat.domain.fake.FakeChatRepository
import com.kikepb.core.domain.util.DataError
import com.kikepb.core.domain.util.Result
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class AddParticipantsToChatUseCaseTest {

    private val repository = FakeChatRepository()
    private val useCase = AddParticipantsToChatUseCase(chatRepository = repository)

    @Test
    fun `GIVEN valid inputs WHEN addParticipantsToChat THEN returns updated chat`() = runTest {
        val chat = FakeChatRepository.defaultChatModel()
        repository.addParticipantsResult = Result.Success(chat)

        val result = useCase.addParticipantsToChat(chatId = "chat-1", userIds = listOf("user-2"))

        assertIs<Result.Success<*>>(result)
        assertEquals(chat, (result as Result.Success).data)
    }

    @Test
    fun `GIVEN repository fails WHEN addParticipantsToChat THEN returns error`() = runTest {
        repository.addParticipantsResult = Result.Failure(error = DataError.Remote.NOT_FOUND)

        val result = useCase.addParticipantsToChat(chatId = "chat-1", userIds = listOf("user-2"))

        assertIs<Result.Failure<*>>(result)
        assertEquals(DataError.Remote.NOT_FOUND, (result as Result.Failure).error)
    }
}
