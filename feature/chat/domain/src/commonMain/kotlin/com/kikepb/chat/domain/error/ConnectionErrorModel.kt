package com.kikepb.chat.domain.error

import com.kikepb.core.domain.util.Error

enum class ConnectionErrorModel: Error {
    NOT_CONNECTED,
    MESSAGE_SEND_FAILED
}