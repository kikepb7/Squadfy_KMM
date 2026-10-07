package com.kikepb.chat.domain.usecases

import com.kikepb.chat.domain.fake.FakeChatRepository
import com.kikepb.core.domain.util.DataError
import com.kikepb.core.domain.util.Result
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class CreateChatUseCaseTest {

    private val repository = FakeChatRepository()
    private val useCase = CreateChatUseCase(chatRepository = repository)

    @Test
    fun `GIVEN valid user ids WHEN createChat THEN returns created chat`() = runTest {
        val chat = FakeChatRepository.defaultChatModel()
        repository.createChatResult = Result.Success(chat)

        val result = useCase.createChat(otherUserIds = listOf("user-2", "user-3"))

        assertIs<Result.Success<*>>(result)
        assertEquals(chat, (result as Result.Success).data)
    }

    @Test
    fun `GIVEN repository fails WHEN createChat THEN returns error`() = runTest {
        repository.createChatResult = Result.Failure(error = DataError.Remote.SERVER_ERROR)

        val result = useCase.createChat(otherUserIds = listOf("user-2"))

        assertIs<Result.Failure<*>>(result)
        assertEquals(DataError.Remote.SERVER_ERROR, (result as Result.Failure).error)
    }
}
