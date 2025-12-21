package com.kikepb.chat.data.di

import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.kikepb.chat.data.datasource.local.OfflineFirstChatRepositoryImpl
import com.kikepb.chat.data.datasource.remote.KtorChatParticipantService
import com.kikepb.chat.data.datasource.remote.KtorChatService
import com.kikepb.chat.data.network.KtorWebSocketConnector
import com.kikepb.chat.data.websocket.local.OfflineFirstMessageRepositoryImpl
import com.kikepb.chat.data.websocket.remote.WebSocketChatConnectionClient
import com.kikepb.chat.database.DatabaseFactory
import com.kikepb.chat.domain.repository.ChatConnectionClient
import com.kikepb.chat.domain.repository.ChatRepository
import com.kikepb.chat.domain.repository.MessageRepository
import kotlinx.serialization.json.Json
import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

expect val platformChatDataModule: Module
val chatDataModule = module {
    includes(platformChatDataModule)

    singleOf(::KtorChatParticipantService) bind KtorChatParticipantService::class
    singleOf(::KtorChatService) bind KtorChatService::class
    singleOf(::OfflineFirstChatRepositoryImpl) bind ChatRepository::class
    singleOf(::OfflineFirstMessageRepositoryImpl) bind MessageRepository::class
    singleOf(::WebSocketChatConnectionClient) bind ChatConnectionClient::class
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