package com.kikepb.chat.presentation.fake

import com.kikepb.chat.domain.models.ChatParticipantModel
import com.kikepb.chat.domain.repository.participant.ChatParticipantRepository
import com.kikepb.core.domain.util.DataError
import com.kikepb.core.domain.util.EmptyResult
import com.kikepb.core.domain.util.Result
import com.kikepb.core.domain.util.Result.Success

class FakeChatParticipantRepository : ChatParticipantRepository {

    var fetchLocalParticipantResult: Result<ChatParticipantModel, DataError> =
        Success(
            ChatParticipantModel(
                userId = "user-1",
                username = "testuser",
                profilePictureUrl = null
            )
        )
    var uploadProfilePictureResult: EmptyResult<DataError.Remote> = Success(Unit)
    var deleteProfilePictureResult: EmptyResult<DataError.Remote> = Success(Unit)

    override suspend fun fetchLocalParticipant(): Result<ChatParticipantModel, DataError> =
        fetchLocalParticipantResult

    override suspend fun uploadProfilePicture(
        imageBytes: ByteArray,
        mimeType: String
    ): EmptyResult<DataError.Remote> = uploadProfilePictureResult

    override suspend fun deleteProfilePicture(): EmptyResult<DataError.Remote> =
        deleteProfilePictureResult
}
