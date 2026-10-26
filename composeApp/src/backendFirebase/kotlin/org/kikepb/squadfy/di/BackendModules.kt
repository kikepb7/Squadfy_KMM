package org.kikepb.squadfy.di

import com.kikepb.chat.data.di.platformChatPushModule
import com.kikepb.chat.datafirebase.di.chatFirebaseDataModule
import com.kikepb.club.datafirebase.di.clubFirebaseDataModule
import com.kikepb.core.firebase.di.coreFirebaseModule
import com.kikepb.core.firebase.init.FirebaseInitializer
import org.koin.core.Koin
import org.koin.core.module.Module

/**
 * `SQUADFY_BACKEND=firebase` (default, ADR-0010): Firebase for everything. The only piece taken from the own-backend
 * chat module is the device's FCM token provider.
 */
internal val backendModules: List<Module> = listOf(
    coreFirebaseModule,
    platformChatPushModule,
    chatFirebaseDataModule,
    clubFirebaseDataModule
)

internal fun Koin.startBackend() = get<FirebaseInitializer>().initialize()

internal fun Koin.onAppForeground() = get<FirebaseInitializer>().onForeground()
