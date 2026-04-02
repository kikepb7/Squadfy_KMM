package org.kikepb.squadfy.di

import com.kikepb.auth.presentation.di.authPresentationModule
import com.kikepb.chat.data.di.chatDataModule
import com.kikepb.chat.domain.di.chatDomainModule
import com.kikepb.chat.presentation.di.chatPresentationModule
import com.kikepb.core.data.di.coreDataModule
import com.kikepb.core.presentation.di.corePresentationModule
import com.kikepb.domain.di.authDomainModule
import com.kikepb.club.data.di.clubDataModule
import com.kikepb.club.domain.di.clubDomainModule
import com.kikepb.club.presentation.di.clubPresentationModule
import com.kikepb.economy.data.di.economyDataModule
import com.kikepb.economy.domain.di.economyDomainModule
import com.kikepb.economy.presentation.di.economyPresentationModule
import com.kikepb.global_position.data.di.globalPositionDataModule
import com.kikepb.global_position.domain.di.globalPositionDomainModule
import com.kikepb.global_position.presentation.di.globalPositionPresentationModule
import com.kikepb.onboarding.data.di.onboardingDataModule
import com.kikepb.onboarding.domain.di.onboardingDomainModule
import com.kikepb.onboarding.presentation.di.onboardingPresentationModule
import org.koin.core.context.startKoin
import org.koin.dsl.KoinAppDeclaration

fun initKoin(config: KoinAppDeclaration? = null) {
    startKoin {
        config?.invoke(this)
        modules(
            coreDataModule,
            authPresentationModule,
            authDomainModule,
            appModule,
            chatDataModule,
            chatDomainModule,
            chatPresentationModule,
            globalPositionDataModule,
            globalPositionDomainModule,
            globalPositionPresentationModule,
            economyDataModule,
            economyDomainModule,
            economyPresentationModule,
            onboardingDataModule,
            onboardingDomainModule,
            onboardingPresentationModule,
            clubDataModule,
            clubDomainModule,
            clubPresentationModule,
            corePresentationModule
        )
    }
}