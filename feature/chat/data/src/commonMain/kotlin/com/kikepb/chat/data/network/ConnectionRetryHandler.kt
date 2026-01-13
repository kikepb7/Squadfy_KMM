package com.kikepb.chat.data.network

import com.kikepb.chat.data.utils.MAX_DELAY_CONNECTION_RETRY
import kotlinx.coroutines.delay
import kotlin.math.pow

class ConnectionRetryHandler(
    private val connectionErrorHandler: ConnectionErrorHandler
) {
    private var shouldSkipBackoff = false

    private fun createBackoffDelay(attempt: Long): Long {
        val delayTime = (2f.pow(attempt.toInt()) * 2000L).toLong()
        return minOf(a = delayTime, b = MAX_DELAY_CONNECTION_RETRY)
    }

    fun shouldRetry(cause: Throwable, attempt: Long): Boolean =
        connectionErrorHandler.isRetriableError(cause = cause)

    suspend fun applyRetryDelay(attempt: Long) {
        if (!shouldSkipBackoff) {
            val delay = createBackoffDelay(attempt = attempt)
            delay(timeMillis = delay)
        } else shouldSkipBackoff = false
    }

    fun resetDelay() = {
        shouldSkipBackoff = true
    }
}