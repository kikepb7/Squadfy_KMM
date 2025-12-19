package com.kikepb.chat.data.di

import com.kikepb.chat.data.lifecycle.AppLifecycleObserver
import com.kikepb.chat.database.DatabaseFactory
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

actual val platformChatDataModule = module {
    single { DatabaseFactory() }
    singleOf(::AppLifecycleObserver)
}