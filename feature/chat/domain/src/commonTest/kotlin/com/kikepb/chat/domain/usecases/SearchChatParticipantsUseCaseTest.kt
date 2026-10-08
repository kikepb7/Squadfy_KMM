package com.kikepb.chat.domain.usecases

import com.kikepb.chat.domain.fake.FakeChatParticipantService
import com.kikepb.chat.domain.models.ChatParticipantModel
import com.kikepb.core.domain.util.DataError
import com.kikepb.core.domain.util.Result
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class SearchChatParticipantsUseCaseTest {

    private val service = FakeChatParticipantService()
    private val useCase = SearchChatParticipantsUseCase(chatParticipantService = service)

    @Test
    fun `AC-015-04 a partial query returns every match from the backend`() = runTest {
        val matches = listOf(
            ChatParticipantModel(userId = "u-1", username = "carlos", profilePictureUrl = null),
            ChatParticipantModel(userId = "u-2", username = "marcos", profilePictureUrl = null)
        )
        service.searchParticipantsResult = Result.Success(matches)

        val result = useCase(query = " ar ")

        assertEquals(Result.Success(matches), result)
        assertEquals("ar", service.lastSearchQuery)
    }

    @Test
    fun `AC-015-04 queries shorter than 2 characters are not sent`() = runTest {
        val result = useCase(query = "a")

        assertEquals(Result.Success(emptyList()), result)
        assertNull(service.lastSearchQuery)
    }

    @Test
    fun `AC-015-04 backend errors are returned as they are`() = runTest {
        service.searchParticipantsResult = Result.Failure(error = DataError.Remote.NO_INTERNET)

        assertEquals(Result.Failure(DataError.Remote.NO_INTERNET), useCase(query = "carlos"))
    }
}
