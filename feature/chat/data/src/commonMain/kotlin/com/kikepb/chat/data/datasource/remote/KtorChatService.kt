package com.kikepb.chat.data.datasource.remote

import com.kikepb.chat.data.dto.ChatDTO
import com.kikepb.chat.data.dto.request.CreateChatRequestDTO
import com.kikepb.chat.data.mappers.toDomain
import com.kikepb.chat.domain.models.ChatModel
import com.kikepb.chat.domain.repository.ChatService
import com.kikepb.core.data.networking.get
import com.kikepb.core.data.networking.post
import com.kikepb.core.domain.util.DataError
import com.kikepb.core.domain.util.Result
import com.kikepb.core.domain.util.map
import io.ktor.client.HttpClient

class KtorChatService(private val httpClient: HttpClient) : ChatService {

    override suspend fun createChat(otherUserIds: List<String>): Result<ChatModel, DataError.Remote> =
        httpClient.post<CreateChatRequestDTO, ChatDTO>(
            route = "/chat",
            body = CreateChatRequestDTO(otherUserIds = otherUserIds)
        ).map { it.toDomain() }

    override suspend fun getChats(): Result<List<ChatModel>, DataError.Remote> =
        httpClient.get<List<ChatDTO>>(
            route = "/chat"
        ).map { chatDto ->
            chatDto.map { it.toDomain() }
        }

    override suspend fun getChatById(chatId: String): Result<ChatModel, DataError.Remote> =
        httpClient.get<ChatDTO>(
            route = "/chat/$chatId"
        ).map { it.toDomain() }
}