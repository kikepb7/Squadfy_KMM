package com.kikepb.chat.data.websocket.local

import com.kikepb.chat.database.SquadfyChatDatabase
import com.kikepb.chat.domain.models.ChatMessageDeliveryStatus
import com.kikepb.chat.domain.repository.MessageRepository
import com.kikepb.core.data.database.safeDatabaseUpdate
import com.kikepb.core.domain.util.DataError
import com.kikepb.core.domain.util.EmptyResult
import kotlin.time.Clock

class OfflineFirstMessageRepositoryImpl(
    private val database: SquadfyChatDatabase
): MessageRepository {

    override suspend fun updateMessageDeliveryStatus(
        messageId: String,
        status: ChatMessageDeliveryStatus
    ): EmptyResult<DataError.Local> =
        safeDatabaseUpdate {
            database.chatMessageDao.updateDeliveryStatus(
                messageId = messageId,
                status = status.name,
                timestamp = Clock.System.now().toEpochMilliseconds()
            )
        }
}