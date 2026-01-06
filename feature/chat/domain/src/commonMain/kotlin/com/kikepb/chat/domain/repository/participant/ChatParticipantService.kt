package com.kikepb.chat.domain.repository.participant

import com.kikepb.chat.domain.models.ChatParticipantModel
import com.kikepb.chat.domain.models.ProfilePictureUploadUrlsModel
import com.kikepb.core.domain.util.DataError
import com.kikepb.core.domain.util.EmptyResult
import com.kikepb.core.domain.util.Result

interface ChatParticipantService {
    suspend fun searchParticipant(query: String): Result<ChatParticipantModel, DataError.Remote>
    suspend fun getLocalParticipant(): Result<ChatParticipantModel, DataError.Remote>
    suspend fun getProfilePictureUploadUrl(mimeType: String): Result<ProfilePictureUploadUrlsModel, DataError.Remote>
    suspend fun uploadProfilePicture(uploadUrl: String, imageBytes: ByteArray, headers: Map<String, String>): EmptyResult<DataError.Remote>
    suspend fun confirmProfilePictureUpload(publicUrl: String): EmptyResult<DataError.Remote>
    suspend fun deleteProfilePicture(): EmptyResult<DataError.Remote>
}