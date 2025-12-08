package com.kikepb.chat.data.di

import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.kikepb.chat.data.ChatParticipantRepositoryImpl
import com.kikepb.chat.data.ChatRepositoryImpl
import com.kikepb.chat.database.DatabaseFactory
import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

expect val platformChatDataModule: Module
val chatDataModule = module {
    includes(platformChatDataModule)

    singleOf(::ChatParticipantRepositoryImpl) bind ChatParticipantRepositoryImpl::class
    singleOf(::ChatRepositoryImpl) bind ChatRepositoryImpl::class
    single {
        get<DatabaseFactory>()
            .create()
            .setDriver(BundledSQLiteDriver())
            .build()
    }
}