package com.kikepb.chat.domain.usecases

import com.kikepb.chat.domain.fake.FakeChatParticipantService
import com.kikepb.chat.domain.models.ChatParticipantModel
import com.kikepb.core.domain.util.DataError
import com.kikepb.core.domain.util.Result
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class GetChatParticipantUseCaseTest {

    private val service = FakeChatParticipantService()
    private val useCase = GetChatParticipantUseCase(chatParticipantService = service)

    @Test
    fun `GIVEN query matches participant WHEN invoke THEN returns participant`() = runTest {
        val expected = ChatParticipantModel(
            userId = "found-user",
            username = "founduser",
            profilePictureUrl = null
        )
        service.searchParticipantResult = Result.Success(expected)

        val result = useCase.invoke(query = "founduser")

        assertIs<Result.Success<*>>(result)
        assertEquals(expected, (result as Result.Success).data)
    }

    @Test
    fun `GIVEN no participant matches query WHEN invoke THEN returns NOT_FOUND error`() = runTest {
        service.searchParticipantResult = Result.Failure(error = DataError.Remote.NOT_FOUND)

        val result = useCase.invoke(query = "unknown")

        assertIs<Result.Failure<*>>(result)
        assertEquals(DataError.Remote.NOT_FOUND, (result as Result.Failure).error)
    }
}
