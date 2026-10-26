package org.kikepb.squadfy.di

import com.kikepb.chat.data.di.chatDataModule
import com.kikepb.club.data.di.clubDataModule
import com.kikepb.core.data.di.coreApiHttpModule
import com.kikepb.core.firebase.di.coreFirebaseModule
import com.kikepb.core.firebase.init.FirebaseInitializer
import org.koin.core.Koin
import org.koin.core.module.Module

/**
 * `SQUADFY_BACKEND=firebase` (default, ADR-0010). While spec 019 is in progress, the features not ported yet keep
 * their own-backend implementation; each phase swaps its module for the `data-firebase` one.
 */
internal val backendModules: List<Module> = listOf(
    coreFirebaseModule,
    coreApiHttpModule,
    chatDataModule,
    clubDataModule
)

internal fun Koin.startBackend() = get<FirebaseInitializer>().initialize()

internal fun Koin.onAppForeground() = get<FirebaseInitializer>().onForeground()
