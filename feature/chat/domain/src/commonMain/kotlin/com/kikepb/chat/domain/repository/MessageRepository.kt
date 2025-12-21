package com.kikepb.chat.domain.repository

import com.kikepb.chat.domain.models.ChatMessageDeliveryStatus
import com.kikepb.core.domain.util.DataError
import com.kikepb.core.domain.util.EmptyResult

interface MessageRepository {
    suspend fun updateMessageDeliveryStatus(
        messageId: String,
        status: ChatMessageDeliveryStatus
    ): EmptyResult<DataError.Local>
}