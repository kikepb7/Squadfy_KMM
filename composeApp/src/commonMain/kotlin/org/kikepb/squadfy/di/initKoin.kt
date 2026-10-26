package org.kikepb.squadfy.di

import com.kikepb.auth.presentation.di.authPresentationModule
import com.kikepb.chat.domain.di.chatDomainModule
import com.kikepb.chat.presentation.di.chatPresentationModule
import com.kikepb.core.data.di.coreDataModule
import com.kikepb.core.presentation.di.corePresentationModule
import com.kikepb.domain.di.authDomainModule
import com.kikepb.club.domain.di.clubDomainModule
import com.kikepb.club.presentation.di.clubPresentationModule
import com.kikepb.global_position.presentation.di.globalPositionPresentationModule
import org.koin.core.context.startKoin
import org.koin.dsl.KoinAppDeclaration

fun initKoin(config: KoinAppDeclaration? = null) {
    val koin = startKoin {
        config?.invoke(this)
        modules(backendModules)
        modules(
            coreDataModule,
            authPresentationModule,
            authDomainModule,
            appModule,
            chatDomainModule,
            chatPresentationModule,
            globalPositionPresentationModule,
            clubDomainModule,
            clubPresentationModule,
            corePresentationModule
        )
    }.koin
    // Spec 019: Firebase bootstrap (emulators, crash reporting, Remote Config) right after Koin, before any screen
    koin.startBackend()
}
