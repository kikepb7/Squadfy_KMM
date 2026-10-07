package com.kikepb.chat.domain.usecases.profile

import com.kikepb.chat.domain.fake.FakeChatParticipantRepository
import com.kikepb.core.domain.util.DataError
import com.kikepb.core.domain.util.Result
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertIs

class DeleteProfilePictureUseCaseTest {

    private val repository = FakeChatParticipantRepository()
    private val useCase = DeleteProfilePictureUseCase(chatParticipantRepository = repository)

    @Test
    fun `GIVEN picture exists WHEN deleteProfilePicture THEN returns success`() = runTest {
        repository.deleteProfilePictureResult = Result.Success(Unit)

        val result = useCase.deleteProfilePicture()

        assertIs<Result.Success<*>>(result)
    }

    @Test
    fun `GIVEN repository fails WHEN deleteProfilePicture THEN returns error`() = runTest {
        repository.deleteProfilePictureResult = Result.Failure(error = DataError.Remote.SERVER_ERROR)

        val result = useCase.deleteProfilePicture()

        assertIs<Result.Failure<*>>(result)
    }
}
