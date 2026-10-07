package com.kikepb.chat.domain.fake

import com.kikepb.chat.domain.models.ChatParticipantModel
import com.kikepb.chat.domain.models.ProfilePictureUploadUrlsModel
import com.kikepb.chat.domain.repository.participant.ChatParticipantService
import com.kikepb.core.domain.util.DataError
import com.kikepb.core.domain.util.EmptyResult
import com.kikepb.core.domain.util.Result
import com.kikepb.core.domain.util.Result.Success

class FakeChatParticipantService : ChatParticipantService {

    var searchParticipantResult: Result<ChatParticipantModel, DataError.Remote> =
        Success(
            ChatParticipantModel(
                userId = "found-user",
                username = "founduser",
                profilePictureUrl = null
            )
        )

    var getLocalParticipantResult: Result<ChatParticipantModel, DataError.Remote> =
        Success(
            ChatParticipantModel(
                userId = "user-1",
                username = "testuser",
                profilePictureUrl = null
            )
        )

    var getProfilePictureUploadUrlResult: Result<ProfilePictureUploadUrlsModel, DataError.Remote> =
        Success(ProfilePictureUploadUrlsModel("upload-url", "public-url", emptyMap()))

    var uploadProfilePictureResult: EmptyResult<DataError.Remote> = Success(Unit)
    var confirmProfilePictureUploadResult: EmptyResult<DataError.Remote> = Success(Unit)
    var deleteProfilePictureResult: EmptyResult<DataError.Remote> = Success(Unit)

    override suspend fun searchParticipant(query: String): Result<ChatParticipantModel, DataError.Remote> =
        searchParticipantResult

    override suspend fun getLocalParticipant(): Result<ChatParticipantModel, DataError.Remote> =
        getLocalParticipantResult

    override suspend fun getProfilePictureUploadUrl(mimeType: String): Result<ProfilePictureUploadUrlsModel, DataError.Remote> =
        getProfilePictureUploadUrlResult

    override suspend fun uploadProfilePicture(
        uploadUrl: String,
        imageBytes: ByteArray,
        headers: Map<String, String>
    ): EmptyResult<DataError.Remote> = uploadProfilePictureResult

    override suspend fun confirmProfilePictureUpload(publicUrl: String): EmptyResult<DataError.Remote> =
        confirmProfilePictureUploadResult

    override suspend fun deleteProfilePicture(): EmptyResult<DataError.Remote> =
        deleteProfilePictureResult
}
