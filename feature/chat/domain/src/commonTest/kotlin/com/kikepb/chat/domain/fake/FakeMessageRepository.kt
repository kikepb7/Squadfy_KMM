package com.kikepb.chat.domain.fake

import com.kikepb.chat.domain.models.ChatMessageDeliveryStatus
import com.kikepb.chat.domain.models.ChatMessageModel
import com.kikepb.chat.domain.models.MessageWithSenderModel
import com.kikepb.chat.domain.models.OutgoingNewMessageModel
import com.kikepb.chat.domain.repository.message.MessageRepository
import com.kikepb.core.domain.util.DataError
import com.kikepb.core.domain.util.EmptyResult
import com.kikepb.core.domain.util.Result
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class FakeMessageRepository : MessageRepository {

    var fetchMessagesResult: Result<List<ChatMessageModel>, DataError> = Result.Success(emptyList())
    var sendMessageResult: EmptyResult<DataError> = Result.Success(Unit)
    var retryMessageResult: EmptyResult<DataError> = Result.Success(Unit)
    var deleteMessageResult: EmptyResult<DataError.Remote> = Result.Success(Unit)
    var updateDeliveryStatusResult: EmptyResult<DataError.Local> = Result.Success(Unit)

    private val messagesFlow = MutableStateFlow<List<MessageWithSenderModel>>(emptyList())

    val sentMessages = mutableListOf<OutgoingNewMessageModel>()
    var lastRetryMessageId: String? = null
    var lastDeletedMessageId: String? = null

    override suspend fun fetchMessages(
        chatId: String,
        before: String?
    ): Result<List<ChatMessageModel>, DataError> = fetchMessagesResult

    override suspend fun sendMessage(message: OutgoingNewMessageModel): EmptyResult<DataError> {
        sentMessages.add(message)
        return sendMessageResult
    }

    override suspend fun retryMessage(messageId: String): EmptyResult<DataError> {
        lastRetryMessageId = messageId
        return retryMessageResult
    }

    override suspend fun deleteMessage(messageId: String): EmptyResult<DataError.Remote> {
        lastDeletedMessageId = messageId
        return deleteMessageResult
    }

    override fun getMessagesForChat(chatId: String): Flow<List<MessageWithSenderModel>> = messagesFlow

    override suspend fun updateMessageDeliveryStatus(
        messageId: String,
        status: ChatMessageDeliveryStatus
    ): EmptyResult<DataError.Local> = updateDeliveryStatusResult
}
