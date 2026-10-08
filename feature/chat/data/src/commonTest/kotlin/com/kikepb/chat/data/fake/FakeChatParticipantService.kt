package com.kikepb.chat.data.fake

import com.kikepb.chat.domain.models.ChatParticipantModel
import com.kikepb.chat.domain.models.ProfilePictureUploadUrlsModel
import com.kikepb.chat.domain.repository.participant.ChatParticipantService
import com.kikepb.core.domain.util.DataError
import com.kikepb.core.domain.util.EmptyResult
import com.kikepb.core.domain.util.Result

class FakeChatParticipantService : ChatParticipantService {

    var searchParticipantsResult: Result<List<ChatParticipantModel>, DataError.Remote> = Result.Success(emptyList())

    var getLocalParticipantResult: Result<ChatParticipantModel, DataError.Remote> =
        Result.Success(defaultParticipant())

    var getProfilePictureUploadUrlResult: Result<ProfilePictureUploadUrlsModel, DataError.Remote> =
        Result.Success(defaultUploadUrls())

    var uploadProfilePictureResult: EmptyResult<DataError.Remote> = Result.Success(Unit)
    var confirmProfilePictureUploadResult: EmptyResult<DataError.Remote> = Result.Success(Unit)
    var deleteProfilePictureResult: EmptyResult<DataError.Remote> = Result.Success(Unit)

    // Capture arguments for assertion
    var lastUploadUrl: String? = null
    var lastUploadedBytes: ByteArray? = null
    var lastConfirmedPublicUrl: String? = null
    var lastGetUploadUrlMimeType: String? = null

    override suspend fun searchParticipants(query: String) = searchParticipantsResult

    override suspend fun getLocalParticipant() = getLocalParticipantResult

    override suspend fun getProfilePictureUploadUrl(mimeType: String): Result<ProfilePictureUploadUrlsModel, DataError.Remote> {
        lastGetUploadUrlMimeType = mimeType
        return getProfilePictureUploadUrlResult
    }

    override suspend fun uploadProfilePicture(
        uploadUrl: String,
        imageBytes: ByteArray,
        headers: Map<String, String>
    ): EmptyResult<DataError.Remote> {
        lastUploadUrl = uploadUrl
        lastUploadedBytes = imageBytes
        return uploadProfilePictureResult
    }

    override suspend fun confirmProfilePictureUpload(publicUrl: String): EmptyResult<DataError.Remote> {
        lastConfirmedPublicUrl = publicUrl
        return confirmProfilePictureUploadResult
    }

    override suspend fun deleteProfilePicture() = deleteProfilePictureResult

    companion object {
        fun defaultParticipant() = ChatParticipantModel(
            userId = "user-1",
            username = "testuser",
            profilePictureUrl = null
        )

        fun defaultUploadUrls() = ProfilePictureUploadUrlsModel(
            uploadUrl = "https://storage.example.com/upload",
            publicUrl = "https://cdn.example.com/user-1/pic.jpg",
            headers = mapOf("Content-Type" to "image/jpeg")
        )
    }
}
