package com.kikepb.chat.data

import com.kikepb.chat.data.dto.ChatParticipantDTO
import com.kikepb.chat.data.mappers.toDomain
import com.kikepb.chat.domain.models.ChatParticipantModel
import com.kikepb.chat.domain.repository.ChatParticipantRepository
import com.kikepb.core.data.networking.get
import com.kikepb.core.domain.util.DataError
import com.kikepb.core.domain.util.Result
import com.kikepb.core.domain.util.map
import io.ktor.client.HttpClient

class ChatParticipantRepositoryImpl(
    private val httpClient: HttpClient
): ChatParticipantRepository {


    override suspend fun searchParticipant(query: String): Result<ChatParticipantModel, DataError.Remote> =
        httpClient.get<ChatParticipantDTO>(
            route = "/participants",
            queryParams = mapOf(
                "query" to query
            )
        ).map { it.toDomain() }
}