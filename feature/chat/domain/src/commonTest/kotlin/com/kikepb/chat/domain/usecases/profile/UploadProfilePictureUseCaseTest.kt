package com.kikepb.chat.domain.usecases.profile

import com.kikepb.chat.domain.fake.FakeChatParticipantRepository
import com.kikepb.core.domain.util.DataError
import com.kikepb.core.domain.util.Result
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertIs

class UploadProfilePictureUseCaseTest {

    private val repository = FakeChatParticipantRepository()
    private val useCase = UploadProfilePictureUseCase(chatParticipantRepository = repository)

    @Test
    fun `GIVEN valid image bytes WHEN uploadProfilePicture THEN returns success`() = runTest {
        repository.uploadProfilePictureResult = Result.Success(Unit)

        val result = useCase.uploadProfilePicture(
            imageBytes = byteArrayOf(1, 2, 3),
            mimeType = "image/jpeg"
        )

        assertIs<Result.Success<*>>(result)
    }

    @Test
    fun `GIVEN repository fails WHEN uploadProfilePicture THEN returns error`() = runTest {
        repository.uploadProfilePictureResult = Result.Failure(error = DataError.Remote.PAYLOAD_TOO_LARGE)

        val result = useCase.uploadProfilePicture(
            imageBytes = byteArrayOf(1, 2, 3),
            mimeType = "image/jpeg"
        )

        assertIs<Result.Failure<*>>(result)
    }
}
