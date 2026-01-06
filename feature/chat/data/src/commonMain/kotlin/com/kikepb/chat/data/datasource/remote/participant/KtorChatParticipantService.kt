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
import com.kikepb.core.data.networking.safeCall
import com.kikepb.core.domain.util.DataError
import com.kikepb.core.domain.util.EmptyResult
import com.kikepb.core.domain.util.Result
import com.kikepb.core.domain.util.map
import io.ktor.client.HttpClient
import io.ktor.client.request.header
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.client.request.url

class KtorChatParticipantService(
    private val httpClient: HttpClient
): ChatParticipantService {

    override suspend fun searchParticipant(query: String): Result<ChatParticipantModel, DataError.Remote> =
        httpClient.get<ChatParticipantDTO>(
            route = "/participants",
            queryParams = mapOf(
                "query" to query
            )
        ).map { it.toDomain() }

    override suspend fun getLocalParticipant(): Result<ChatParticipantModel, DataError.Remote> =
        httpClient.get<ChatParticipantDTO>(
            route = "/participants"
        ).map { it.toDomain() }

    override suspend fun getProfilePictureUploadUrl(mimeType: String): Result<ProfilePictureUploadUrlsModel, DataError.Remote> =
        httpClient.post<Unit, ProfilePictureUploadUrlsResponseDTO>(
            route = "/participants/profile-picture-upload",
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
        httpClient.post< ConfirmProfilePictureRequestDTO, Unit>(
            route = "/participants/confirm-profile-picture",
            body = ConfirmProfilePictureRequestDTO(publicUrl = publicUrl)
        )

    override suspend fun deleteProfilePicture(): EmptyResult<DataError.Remote> =
        httpClient.delete(route = "/participants/profile-picture")
}