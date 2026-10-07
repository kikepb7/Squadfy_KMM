package com.kikepb.chat.presentation.fake

import com.kikepb.chat.domain.notification.DeviceTokenService
import com.kikepb.core.domain.util.DataError
import com.kikepb.core.domain.util.EmptyResult
import com.kikepb.core.domain.util.Result

class FakeDeviceTokenService : DeviceTokenService {

    var registerTokenResult: EmptyResult<DataError.Remote> = Result.Success(Unit)
    var unregisterTokenResult: EmptyResult<DataError.Remote> = Result.Success(Unit)

    override suspend fun registerToken(token: String, platform: String) = registerTokenResult
    override suspend fun unregisterToken(token: String) = unregisterTokenResult
}
