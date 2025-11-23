package com.kikepb.chat.data

import com.kikepb.chat.data.dto.ChatDTO
import com.kikepb.chat.data.dto.request.CreateChatRequestDTO
import com.kikepb.chat.data.mappers.toDomain
import com.kikepb.chat.domain.models.ChatModel
import com.kikepb.chat.domain.repository.ChatRepository
import com.kikepb.core.data.networking.post
import com.kikepb.core.domain.util.DataError
import com.kikepb.core.domain.util.Result
import com.kikepb.core.domain.util.map
import io.ktor.client.HttpClient

class ChatRepositoryImpl(private val httpClient: HttpClient) : ChatRepository {

    override suspend fun createChat(otherUserIds: List<String>): Result<ChatModel, DataError.Remote> =
        httpClient.post<CreateChatRequestDTO, ChatDTO>(
            route = "/chat",
            body = CreateChatRequestDTO(otherUserIds = otherUserIds)
        ).map { it.toDomain() }

}