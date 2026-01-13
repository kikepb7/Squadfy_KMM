package com.kikepb.chat.domain.usecases

import com.kikepb.chat.domain.notification.DeviceTokenService

class UnregisterTokenUseCase(private val deviceTokenService: DeviceTokenService) {
    suspend fun unregisterToken(token: String) = deviceTokenService.unregisterToken(token = token)
}