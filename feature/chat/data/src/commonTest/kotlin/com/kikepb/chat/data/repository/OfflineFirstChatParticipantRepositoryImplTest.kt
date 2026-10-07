package com.kikepb.chat.data.repository

import com.kikepb.chat.data.datasource.remote.participant.OfflineFirstChatParticipantRepositoryImpl
import com.kikepb.chat.data.fake.FakeChatParticipantService
import com.kikepb.chat.data.fake.FakeSessionStorage
import com.kikepb.core.domain.auth.model.AuthInfoModel
import com.kikepb.core.domain.auth.model.UserModel
import com.kikepb.core.domain.util.DataError
import com.kikepb.core.domain.util.Result
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class OfflineFirstChatParticipantRepositoryImplTest {

    private val sessionStorage = FakeSessionStorage()
    private val participantService = FakeChatParticipantService()
    private val repository = OfflineFirstChatParticipantRepositoryImpl(
        sessionStorage = sessionStorage,
        chatParticipantService = participantService
    )

    @Test
    fun `GIVEN service returns participant WHEN fetchLocalParticipant THEN returns participant`() = runTest {
        val expected = FakeChatParticipantService.defaultParticipant()
        participantService.getLocalParticipantResult = Result.Success(expected)

        val result = repository.fetchLocalParticipant()

        assertIs<Result.Success<*>>(result)
        assertEquals(expected, (result as Result.Success).data)
    }

    @Test
    fun `GIVEN authenticated session WHEN fetchLocalParticipant succeeds THEN sessionStorage is updated with participant data`() = runTest {
        val existingAuthInfo = defaultAuthInfoModel()
        sessionStorage.set(existingAuthInfo)

        val participant = FakeChatParticipantService.defaultParticipant().copy(
            userId = "server-user-id",
            username = "serveruser",
            profilePictureUrl = "https://cdn.example.com/pic.jpg"
        )
        participantService.getLocalParticipantResult = Result.Success(participant)

        repository.fetchLocalParticipant()

        val savedInfo = sessionStorage.savedInfo
        assertNotNull(savedInfo)
        assertEquals("server-user-id", savedInfo.user.id)
        assertEquals("serveruser", savedInfo.user.username)
        assertEquals("https://cdn.example.com/pic.jpg", savedInfo.user.profilePictureUrl)
    }

    @Test
    fun `GIVEN no auth session WHEN fetchLocalParticipant succeeds THEN sessionStorage remains null`() = runTest {
        // sessionStorage is null by default
        participantService.getLocalParticipantResult = Result.Success(FakeChatParticipantService.defaultParticipant())

        repository.fetchLocalParticipant()

        assertNull(sessionStorage.savedInfo)
    }

    @Test
    fun `GIVEN service fails WHEN fetchLocalParticipant THEN returns error and sessionStorage is NOT updated`() = runTest {
        sessionStorage.set(defaultAuthInfoModel())
        participantService.getLocalParticipantResult = Result.Failure(error = DataError.Remote.NOT_FOUND)

        val result = repository.fetchLocalParticipant()

        assertIs<Result.Failure<*>>(result)
        assertEquals(DataError.Remote.NOT_FOUND, (result as Result.Failure).error)
        // Session should remain as it was — no update on failure
        assertEquals("user-id", sessionStorage.savedInfo?.user?.id)
    }

    @Test
    fun `GIVEN all steps succeed WHEN uploadProfilePicture THEN returns success`() = runTest {
        val result = repository.uploadProfilePicture(
            imageBytes = byteArrayOf(1, 2, 3),
            mimeType = "image/jpeg"
        )

        assertIs<Result.Success<*>>(result)
    }

    @Test
    fun `GIVEN all steps succeed WHEN uploadProfilePicture THEN sessionStorage profilePictureUrl is updated`() = runTest {
        sessionStorage.set(defaultAuthInfoModel())
        val uploadUrls = FakeChatParticipantService.defaultUploadUrls()
        participantService.getProfilePictureUploadUrlResult = Result.Success(uploadUrls)

        repository.uploadProfilePicture(
            imageBytes = byteArrayOf(1, 2, 3),
            mimeType = "image/jpeg"
        )

        assertEquals(uploadUrls.publicUrl, sessionStorage.savedInfo?.user?.profilePictureUrl)
    }

    @Test
    fun `GIVEN all steps succeed WHEN uploadProfilePicture THEN upload is called with correct url and bytes`() = runTest {
        val uploadUrls = FakeChatParticipantService.defaultUploadUrls()
        participantService.getProfilePictureUploadUrlResult = Result.Success(uploadUrls)

        val bytes = byteArrayOf(10, 20, 30)
        repository.uploadProfilePicture(imageBytes = bytes, mimeType = "image/png")

        assertEquals(uploadUrls.uploadUrl, participantService.lastUploadUrl)
    }

    @Test
    fun `GIVEN getProfilePictureUploadUrl fails WHEN uploadProfilePicture THEN returns error without uploading`() = runTest {
        participantService.getProfilePictureUploadUrlResult =
            Result.Failure(error = DataError.Remote.SERVER_ERROR)

        val result = repository.uploadProfilePicture(
            imageBytes = byteArrayOf(1, 2, 3),
            mimeType = "image/jpeg"
        )

        assertIs<Result.Failure<*>>(result)
        assertEquals(DataError.Remote.SERVER_ERROR, (result as Result.Failure).error)
        // upload should NOT have been called
        assertNull(participantService.lastUploadUrl)
    }

    @Test
    fun `GIVEN uploadProfilePicture step fails WHEN uploadProfilePicture THEN returns error without confirming`() = runTest {
        participantService.uploadProfilePictureResult =
            Result.Failure(error = DataError.Remote.PAYLOAD_TOO_LARGE)

        val result = repository.uploadProfilePicture(
            imageBytes = byteArrayOf(1, 2, 3),
            mimeType = "image/jpeg"
        )

        assertIs<Result.Failure<*>>(result)
        assertEquals(DataError.Remote.PAYLOAD_TOO_LARGE, (result as Result.Failure).error)
        // confirm should NOT have been called
        assertNull(participantService.lastConfirmedPublicUrl)
    }

    @Test
    fun `GIVEN confirmProfilePictureUpload fails WHEN uploadProfilePicture THEN returns error`() = runTest {
        participantService.confirmProfilePictureUploadResult =
            Result.Failure(error = DataError.Remote.SERVER_ERROR)

        val result = repository.uploadProfilePicture(
            imageBytes = byteArrayOf(1, 2, 3),
            mimeType = "image/jpeg"
        )

        assertIs<Result.Failure<*>>(result)
    }

    @Test
    fun `GIVEN service succeeds WHEN deleteProfilePicture THEN returns success`() = runTest {
        val result = repository.deleteProfilePicture()

        assertIs<Result.Success<*>>(result)
    }

    @Test
    fun `GIVEN authenticated session WHEN deleteProfilePicture succeeds THEN profilePictureUrl is cleared in session`() = runTest {
        val authInfo = defaultAuthInfoModel().copy(
            user = defaultAuthInfoModel().user.copy(profilePictureUrl = "https://cdn.example.com/pic.jpg")
        )
        sessionStorage.set(authInfo)

        repository.deleteProfilePicture()

        assertNull(sessionStorage.savedInfo?.user?.profilePictureUrl)
    }

    @Test
    fun `GIVEN service fails WHEN deleteProfilePicture THEN returns error and session is NOT modified`() = runTest {
        val authInfo = defaultAuthInfoModel().copy(
            user = defaultAuthInfoModel().user.copy(profilePictureUrl = "https://cdn.example.com/pic.jpg")
        )
        sessionStorage.set(authInfo)
        participantService.deleteProfilePictureResult = Result.Failure(error = DataError.Remote.SERVER_ERROR)

        val result = repository.deleteProfilePicture()

        assertIs<Result.Failure<*>>(result)
        // profilePictureUrl should remain unchanged
        assertEquals("https://cdn.example.com/pic.jpg", sessionStorage.savedInfo?.user?.profilePictureUrl)
    }

    private fun defaultAuthInfoModel() = AuthInfoModel(
        accessToken = "access-token",
        refreshToken = "refresh-token",
        user = UserModel(
            id = "user-id",
            email = "user@example.com",
            username = "testuser",
            hasVerifiedEmail = true,
            profilePictureUrl = null
        )
    )
}
