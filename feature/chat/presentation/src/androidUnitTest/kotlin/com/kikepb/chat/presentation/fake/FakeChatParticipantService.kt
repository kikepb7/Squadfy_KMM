package com.kikepb.chat.presentation.fake

import com.kikepb.chat.domain.models.ChatParticipantModel
import com.kikepb.chat.domain.models.ProfilePictureUploadUrlsModel
import com.kikepb.chat.domain.repository.participant.ChatParticipantService
import com.kikepb.core.domain.util.DataError
import com.kikepb.core.domain.util.EmptyResult
import com.kikepb.core.domain.util.Result

class FakeChatParticipantService : ChatParticipantService {

    var searchParticipantsResult: Result<List<ChatParticipantModel>, DataError.Remote> = Result.Success(emptyList())

    override suspend fun searchParticipants(query: String) = searchParticipantsResult

    override suspend fun getLocalParticipant(): Result<ChatParticipantModel, DataError.Remote> =
        Result.Success(ChatParticipantModel("user-1", "testuser", null))

    override suspend fun getProfilePictureUploadUrl(mimeType: String): Result<ProfilePictureUploadUrlsModel, DataError.Remote> =
        Result.Success(ProfilePictureUploadUrlsModel("upload-url", "public-url", emptyMap()))

    override suspend fun uploadProfilePicture(
        uploadUrl: String,
        imageBytes: ByteArray,
        headers: Map<String, String>
    ): EmptyResult<DataError.Remote> = Result.Success(Unit)

    override suspend fun confirmProfilePictureUpload(publicUrl: String): EmptyResult<DataError.Remote> =
        Result.Success(Unit)

    override suspend fun deleteProfilePicture(): EmptyResult<DataError.Remote> =
        Result.Success(Unit)
}
