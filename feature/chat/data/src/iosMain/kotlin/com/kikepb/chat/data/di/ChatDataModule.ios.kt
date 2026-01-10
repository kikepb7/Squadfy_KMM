package com.kikepb.chat.data.di

import com.kikepb.chat.data.lifecycle.AppLifecycleObserver
import com.kikepb.chat.data.network.ConnectionErrorHandler
import com.kikepb.chat.data.network.ConnectivityObserver
import com.kikepb.chat.data.notification.FirebasePushNotificationRepositoryImpl
import com.kikepb.chat.database.DatabaseFactory
import com.kikepb.chat.domain.notification.PushNotificationService
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

actual val platformChatDataModule = module {
    single { DatabaseFactory() }
    singleOf(::AppLifecycleObserver)
    singleOf(::ConnectivityObserver)
    singleOf(::ConnectionErrorHandler)
    singleOf(::FirebasePushNotificationRepositoryImpl) bind PushNotificationService::class
}