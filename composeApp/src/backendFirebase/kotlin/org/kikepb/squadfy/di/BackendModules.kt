package org.kikepb.squadfy.di

import com.kikepb.chat.data.di.chatDataModule
import com.kikepb.club.datafirebase.di.clubFirebaseDataModule
import com.kikepb.core.data.di.coreApiHttpModule
import com.kikepb.core.firebase.di.coreFirebaseModule
import com.kikepb.core.firebase.init.FirebaseInitializer
import org.koin.core.Koin
import org.koin.core.module.Module

/**
 * `SQUADFY_BACKEND=firebase` (default, ADR-0010). While spec 019 is in progress, the chat keeps its own-backend
 * implementation; the `data-firebase` modules come last and override what it binds (club live updates).
 */
internal val backendModules: List<Module> = listOf(
    coreFirebaseModule,
    coreApiHttpModule,
    chatDataModule,
    clubFirebaseDataModule
)

internal fun Koin.startBackend() = get<FirebaseInitializer>().initialize()

internal fun Koin.onAppForeground() = get<FirebaseInitializer>().onForeground()
