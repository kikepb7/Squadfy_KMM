package com.kikepb.chat.domain.notification

import com.kikepb.core.domain.util.DataError
import com.kikepb.core.domain.util.EmptyResult

interface DeviceTokenService {
    suspend fun registerToken(token: String, platform: String): EmptyResult<DataError.Remote>
    suspend fun unregisterToken(token: String): EmptyResult<DataError.Remote>
}