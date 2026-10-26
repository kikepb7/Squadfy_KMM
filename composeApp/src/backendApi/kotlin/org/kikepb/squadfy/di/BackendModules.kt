package org.kikepb.squadfy.di

import com.kikepb.chat.data.di.chatDataModule
import com.kikepb.club.data.di.clubDataModule
import com.kikepb.core.data.di.coreApiNetworkModule
import com.kikepb.core.data.di.coreApiTelemetryModule
import org.koin.core.Koin
import org.koin.core.module.Module

/** `SQUADFY_BACKEND=api`: the own backend (Ktor + Room), frozen by ADR-0010. */
internal val backendModules: List<Module> = listOf(
    coreApiNetworkModule,
    coreApiTelemetryModule,
    chatDataModule,
    clubDataModule
)

internal fun Koin.startBackend() = Unit

internal fun Koin.onAppForeground() = Unit
