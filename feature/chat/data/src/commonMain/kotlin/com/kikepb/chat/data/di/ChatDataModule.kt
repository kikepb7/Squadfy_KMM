package com.kikepb.chat.data.di

import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.kikepb.chat.data.datasource.local.OfflineFirstChatRepository
import com.kikepb.chat.data.datasource.remote.KtorChatParticipantService
import com.kikepb.chat.data.datasource.remote.KtorChatService
import com.kikepb.chat.database.DatabaseFactory
import com.kikepb.chat.domain.repository.ChatRepository
import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

expect val platformChatDataModule: Module
val chatDataModule = module {
    includes(platformChatDataModule)

    singleOf(::KtorChatParticipantService) bind KtorChatParticipantService::class
    singleOf(::KtorChatService) bind KtorChatService::class
    singleOf(::OfflineFirstChatRepository) bind ChatRepository::class
    single {
        get<DatabaseFactory>()
            .create()
            .setDriver(BundledSQLiteDriver())
            .build()
    }
}