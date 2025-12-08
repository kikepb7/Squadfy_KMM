package com.kikepb.chat.data.di

import com.kikepb.chat.database.DatabaseFactory
import org.koin.dsl.module

actual val platformChatDataModule = module {
    single { DatabaseFactory() }
}