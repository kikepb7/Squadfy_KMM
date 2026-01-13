package com.kikepb.chat.data.di

import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.kikepb.chat.data.datasource.local.OfflineFirstChatRepositoryImpl
import com.kikepb.chat.data.datasource.remote.message.KtorChatMessageService
import com.kikepb.chat.data.datasource.remote.participant.KtorChatParticipantService
import com.kikepb.chat.data.datasource.remote.KtorChatService
import com.kikepb.chat.data.datasource.remote.participant.OfflineFirstChatParticipantRepositoryImpl
import com.kikepb.chat.data.network.ConnectionRetryHandler
import com.kikepb.chat.data.network.KtorWebSocketConnector
import com.kikepb.chat.data.notification.KtorDeviceTokenRepositoryImpl
import com.kikepb.chat.data.websocket.local.OfflineFirstMessageRepositoryImpl
import com.kikepb.chat.data.websocket.remote.WebSocketChatConnectionClient
import com.kikepb.chat.database.DatabaseFactory
import com.kikepb.chat.domain.notification.DeviceTokenService
import com.kikepb.chat.domain.repository.chat.ChatConnectionClient
import com.kikepb.chat.domain.repository.chat.ChatRepository
import com.kikepb.chat.domain.repository.chat.ChatService
import com.kikepb.chat.domain.repository.message.ChatMessageService
import com.kikepb.chat.domain.repository.message.MessageRepository
import com.kikepb.chat.domain.repository.participant.ChatParticipantRepository
import com.kikepb.chat.domain.repository.participant.ChatParticipantService
import kotlinx.serialization.json.Json
import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

expect val platformChatDataModule: Module
val chatDataModule = module {
    includes(platformChatDataModule)

    singleOf(::KtorChatParticipantService) bind ChatParticipantService::class
    singleOf(::KtorChatService) bind ChatService::class
    singleOf(::OfflineFirstChatRepositoryImpl) bind ChatRepository::class
    singleOf(::OfflineFirstMessageRepositoryImpl) bind MessageRepository::class
    singleOf(::OfflineFirstChatParticipantRepositoryImpl) bind ChatParticipantRepository::class
    singleOf(::WebSocketChatConnectionClient) bind ChatConnectionClient::class
    singleOf(::KtorChatMessageService) bind ChatMessageService::class
    singleOf(::KtorDeviceTokenRepositoryImpl) bind DeviceTokenService::class
    singleOf(::ConnectionRetryHandler)
    singleOf(::KtorWebSocketConnector)
    single {
        Json { ignoreUnknownKeys = true }
    }
    single {
        get<DatabaseFactory>()
            .create()
            .setDriver(BundledSQLiteDriver())
            .build()
    }
}