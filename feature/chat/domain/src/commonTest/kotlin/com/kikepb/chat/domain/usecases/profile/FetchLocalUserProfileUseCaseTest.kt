package com.kikepb.chat.domain.usecases.profile

import com.kikepb.chat.domain.fake.FakeChatParticipantRepository
import com.kikepb.chat.domain.models.ChatParticipantModel
import com.kikepb.core.domain.util.DataError
import com.kikepb.core.domain.util.Result
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class FetchLocalUserProfileUseCaseTest {

    private val repository = FakeChatParticipantRepository()
    private val useCase = FetchLocalUserProfileUseCase(chatParticipantRepository = repository)

    @Test
    fun `GIVEN local profile exists WHEN fetchLocalUserProfile THEN returns participant`() = runTest {
        val expected = ChatParticipantModel(
            userId = "user-1",
            username = "testuser",
            profilePictureUrl = "https://example.com/pic.jpg"
        )
        repository.fetchLocalParticipantResult = Result.Success(expected)

        val result = useCase.fetchLocalUserProfile()

        assertIs<Result.Success<*>>(result)
        assertEquals(expected, (result as Result.Success).data)
    }

    @Test
    fun `GIVEN no local profile WHEN fetchLocalUserProfile THEN returns error`() = runTest {
        repository.fetchLocalParticipantResult = Result.Failure(error = DataError.Local.NOT_FOUND)

        val result = useCase.fetchLocalUserProfile()

        assertIs<Result.Failure<*>>(result)
    }
}
