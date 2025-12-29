package com.kikepb.chat.data.datasource.remote

import com.kikepb.chat.data.dto.ChatMessageDTO
import com.kikepb.chat.data.mappers.toDomain
import com.kikepb.chat.data.utils.PAGE_SIZE
import com.kikepb.chat.domain.models.ChatMessageModel
import com.kikepb.chat.domain.repository.message.ChatMessageService
import com.kikepb.core.data.networking.delete
import com.kikepb.core.data.networking.get
import com.kikepb.core.domain.util.DataError
import com.kikepb.core.domain.util.EmptyResult
import com.kikepb.core.domain.util.Result
import com.kikepb.core.domain.util.map
import io.ktor.client.HttpClient

class KtorChatMessageService(
    private val httpClient: HttpClient
): ChatMessageService {

    override suspend fun fetchMessages(
        chatId: String,
        before: String?
    ): Result<List<ChatMessageModel>, DataError.Remote> =
        httpClient.get<List<ChatMessageDTO>>(
            route = "/chat/$chatId/messages",
            queryParams = buildMap {
                this["pageSize"] = PAGE_SIZE
                if (before != null) this["before"] = before
            }
        ).map { messagesDto ->
            messagesDto.map { it.toDomain() }
        }

    override suspend fun deleteMessage(messageId: String): EmptyResult<DataError.Remote> =
        httpClient.delete(route = "/message/$messageId")
}