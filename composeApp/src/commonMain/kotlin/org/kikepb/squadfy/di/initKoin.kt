package org.kikepb.squadfy.di

import com.kikepb.auth.presentation.di.authPresentationModule
import com.kikepb.core.data.di.coreDataModule
import com.kikepb.domain.di.authDomainModule
import org.koin.core.context.startKoin
import org.koin.dsl.KoinAppDeclaration

fun initKoin(config: KoinAppDeclaration? = null) {
    startKoin {
        config?.invoke(this)
        modules(
            coreDataModule,
            authPresentationModule,
            authDomainModule,
            appModule
        )
    }
}