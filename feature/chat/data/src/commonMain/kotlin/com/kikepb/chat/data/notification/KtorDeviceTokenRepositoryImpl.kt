package com.kikepb.chat.data.notification

import com.kikepb.chat.data.dto.request.RegisterDeviceTokenRequestDTO
import com.kikepb.chat.domain.notification.DeviceTokenService
import com.kikepb.core.data.networking.delete
import com.kikepb.core.data.networking.post
import com.kikepb.core.domain.util.DataError
import com.kikepb.core.domain.util.EmptyResult
import io.ktor.client.HttpClient

class KtorDeviceTokenRepositoryImpl(
    private val httpClient: HttpClient
): DeviceTokenService {

    override suspend fun registerToken(token: String, platform: String): EmptyResult<DataError.Remote> =
        httpClient.post(
            route = "/notification/register",
            body = RegisterDeviceTokenRequestDTO(token = token, platform = platform)
        )

    override suspend fun unregisterToken(token: String): EmptyResult<DataError.Remote> =
        httpClient.delete(route = "/notification/$token")
}