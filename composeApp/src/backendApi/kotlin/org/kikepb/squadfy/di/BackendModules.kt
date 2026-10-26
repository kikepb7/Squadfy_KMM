package org.kikepb.squadfy.di

import com.kikepb.chat.data.di.chatDataModule
import com.kikepb.club.data.di.clubDataModule
import com.kikepb.core.data.di.coreApiNetworkModule
import com.kikepb.core.data.di.coreApiTelemetryModule
import com.kikepb.core.domain.auth.repository.NoSocialCredentialProvider
import com.kikepb.core.domain.auth.repository.SocialCredentialProvider
import com.kikepb.chat.domain.repository.presence.NoPresenceRepository
import com.kikepb.chat.domain.repository.presence.PresenceRepository
import org.koin.core.Koin
import org.koin.dsl.module
import org.koin.core.module.Module

/** `SQUADFY_BACKEND=api`: the own backend (Ktor + Room), frozen by ADR-0010. */
internal val backendModules: List<Module> = listOf(
    // No third-party sign-in with the own backend
    module {
        single<SocialCredentialProvider> { NoSocialCredentialProvider }
        single<PresenceRepository> { NoPresenceRepository }
    },
    coreApiNetworkModule,
    coreApiTelemetryModule,
    chatDataModule,
    clubDataModule
)

internal fun Koin.startBackend() = Unit

internal fun Koin.onAppForeground() = Unit

/** The own backend does not use Firebase App Check. */
internal val needsAppCheck: Boolean = false
