package com.kikepb.chat.data.network

import com.kikepb.chat.domain.models.ConnectionStateModel

expect class ConnectionErrorHandler {
    fun getConnectionStateForError(cause: Throwable): ConnectionStateModel
    fun transformException(exception: Throwable): Throwable
    fun isRetriableError(cause: Throwable): Boolean
}