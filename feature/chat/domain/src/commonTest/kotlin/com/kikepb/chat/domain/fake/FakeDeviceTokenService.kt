package com.kikepb.chat.domain.fake

import com.kikepb.chat.domain.notification.DeviceTokenService
import com.kikepb.core.domain.util.DataError
import com.kikepb.core.domain.util.EmptyResult
import com.kikepb.core.domain.util.Result

class FakeDeviceTokenService : DeviceTokenService {

    var registerTokenResult: EmptyResult<DataError.Remote> = Result.Success(Unit)
    var unregisterTokenResult: EmptyResult<DataError.Remote> = Result.Success(Unit)

    var lastRegisteredToken: String? = null
    var lastUnregisteredToken: String? = null

    override suspend fun registerToken(
        token: String,
        platform: String
    ): EmptyResult<DataError.Remote> {
        lastRegisteredToken = token
        return registerTokenResult
    }

    override suspend fun unregisterToken(token: String): EmptyResult<DataError.Remote> {
        lastUnregisteredToken = token
        return unregisterTokenResult
    }
}
