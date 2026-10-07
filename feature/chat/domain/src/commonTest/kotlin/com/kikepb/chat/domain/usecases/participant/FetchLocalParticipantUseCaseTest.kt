package com.kikepb.chat.domain.usecases.participant

import com.kikepb.chat.domain.fake.FakeChatParticipantRepository
import com.kikepb.chat.domain.models.ChatParticipantModel
import com.kikepb.core.domain.util.DataError
import com.kikepb.core.domain.util.Result
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class FetchLocalParticipantUseCaseTest {

    private val repository = FakeChatParticipantRepository()
    private val useCase = FetchLocalParticipantUseCase(chatParticipantRepository = repository)

    @Test
    fun `GIVEN participant exists locally WHEN fetchLocalParticipant THEN returns participant`() = runTest {
        val expected = ChatParticipantModel(
            userId = "user-1",
            username = "testuser",
            profilePictureUrl = null
        )
        repository.fetchLocalParticipantResult = Result.Success(expected)

        val result = useCase.fetchLocalParticipant()

        assertIs<Result.Success<*>>(result)
        assertEquals(expected, (result as Result.Success).data)
    }

    @Test
    fun `GIVEN participant not found WHEN fetchLocalParticipant THEN returns error`() = runTest {
        repository.fetchLocalParticipantResult = Result.Failure(error = DataError.Local.NOT_FOUND)

        val result = useCase.fetchLocalParticipant()

        assertIs<Result.Failure<*>>(result)
        assertEquals(DataError.Local.NOT_FOUND, (result as Result.Failure).error)
    }
}
