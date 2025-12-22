package com.kikepb.chat.data.di

import com.kikepb.chat.data.lifecycle.AppLifecycleObserver
import com.kikepb.chat.data.network.ConnectionErrorHandler
import com.kikepb.chat.data.network.ConnectivityObserver
import com.kikepb.chat.database.DatabaseFactory
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

actual val platformChatDataModule = module {
    single { DatabaseFactory(context = androidContext()) }
    singleOf(::AppLifecycleObserver)
    singleOf(::ConnectivityObserver)
    singleOf(::ConnectionErrorHandler)
}