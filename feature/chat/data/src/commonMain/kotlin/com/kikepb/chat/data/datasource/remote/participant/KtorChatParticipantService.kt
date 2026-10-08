package com.kikepb.chat.data.datasource.remote.participant

import com.kikepb.chat.data.dto.ChatParticipantDTO
import com.kikepb.chat.data.dto.response.ConfirmProfilePictureRequestDTO
import com.kikepb.chat.data.dto.response.ProfilePictureUploadUrlsResponseDTO
import com.kikepb.chat.data.mappers.toDomain
import com.kikepb.chat.domain.models.ChatParticipantModel
import com.kikepb.chat.domain.models.ProfilePictureUploadUrlsModel
import com.kikepb.chat.domain.repository.participant.ChatParticipantService
import com.kikepb.core.data.networking.delete
import com.kikepb.core.data.networking.get
import com.kikepb.core.data.networking.post
import com.kikepb.core.data.networking.put
import com.kikepb.core.data.networking.safeCall
import com.kikepb.core.domain.util.DataError
import com.kikepb.core.domain.util.EmptyResult
import com.kikepb.core.domain.util.Result
import com.kikepb.core.domain.util.map
import com.kikepb.core.domain.auth.repository.SessionStorage
import io.ktor.client.HttpClient
import kotlinx.coroutines.flow.firstOrNull
import io.ktor.client.request.header
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.client.request.url

class KtorChatParticipantService(
    private val httpClient: HttpClient,
    private val sessionStorage: SessionStorage
): ChatParticipantService {

    override suspend fun searchParticipants(query: String): Result<List<ChatParticipantModel>, DataError.Remote> =
        httpClient.get<List<ChatParticipantDTO>>(
            route = "/users/search",
            queryParams = mapOf("q" to query)
        ).map { participants -> participants.map { it.toDomain() } }

    override suspend fun getLocalParticipant(): Result<ChatParticipantModel, DataError.Remote> {
        // v1 has no "my participant" route: the public profile is read by user id (GET /users/{userId})
        val userId = sessionStorage.observeAuthInfo().firstOrNull()?.user?.id
            ?: return Result.Failure(DataError.Remote.UNAUTHORIZED)
        return httpClient.get<ChatParticipantDTO>(
            route = "/users/$userId"
        ).map { it.toDomain() }
    }

    override suspend fun getProfilePictureUploadUrl(mimeType: String): Result<ProfilePictureUploadUrlsModel, DataError.Remote> =
        httpClient.post<Unit, ProfilePictureUploadUrlsResponseDTO>(
            route = "/me/profile-picture/upload-url",
            queryParams = mapOf(
                "mimeType" to mimeType
            ),
            body = Unit
        ).map { it.toDomain() }

    override suspend fun uploadProfilePicture(
        uploadUrl: String,
        imageBytes: ByteArray,
        headers: Map<String, String>
    ): EmptyResult<DataError.Remote> =
        safeCall {
            httpClient.put {
                url(urlString = uploadUrl)
                headers.forEach { (key, value) ->
                    header(key, value)
                }
                setBody(body = imageBytes)
            }
        }

    override suspend fun confirmProfilePictureUpload(publicUrl: String): EmptyResult<DataError.Remote> =
        httpClient.put<ConfirmProfilePictureRequestDTO, Unit>(
            route = "/me/profile-picture",
            body = ConfirmProfilePictureRequestDTO(publicUrl = publicUrl)
        )

    override suspend fun deleteProfilePicture(): EmptyResult<DataError.Remote> =
        httpClient.delete(route = "/me/profile-picture")
}