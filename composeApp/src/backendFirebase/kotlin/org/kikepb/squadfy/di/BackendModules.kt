package org.kikepb.squadfy.di

import com.kikepb.chat.data.di.platformChatPushModule
import com.kikepb.chat.datafirebase.di.chatFirebaseDataModule
import com.kikepb.chat.domain.repository.presence.PresenceRepository
import com.kikepb.club.datafirebase.di.clubFirebaseDataModule
import com.kikepb.core.firebase.di.coreFirebaseModule
import com.kikepb.core.firebase.init.FirebaseInitializer
import com.kikepb.core.firebase.init.isAppCheckNeeded
import org.kikepb.squadfy.auth.platformSocialSignInModule
import org.koin.core.Koin
import org.koin.core.module.Module

/**
 * `SQUADFY_BACKEND=firebase` (default, ADR-0010): Firebase for everything. The only piece taken from the own-backend
 * chat module is the device's FCM token provider.
 */
internal val backendModules: List<Module> = listOf(
    coreFirebaseModule,
    platformSocialSignInModule,
    platformChatPushModule,
    chatFirebaseDataModule,
    clubFirebaseDataModule
)

internal fun Koin.startBackend() {
    get<FirebaseInitializer>().initialize()
    // Presence keeps "online" while signed in; it opens the Realtime Database, so only after the emulators are set
    get<PresenceRepository>()
}

/** Spec 019 AC-019-20: whether the platform installs Firebase App Check before Firebase starts. */
internal val needsAppCheck: Boolean get() = isAppCheckNeeded

internal fun Koin.onAppForeground() = get<FirebaseInitializer>().onForeground()
