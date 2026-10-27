package org.kikepb.squadfy.di

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.kikepb.squadfy.MainViewModel
import org.kikepb.squadfy.debug.FeatureFlagsViewModel
import org.kikepb.squadfy.push.platformPushTopicsModule
import org.kikepb.squadfy.update.AppUpdateViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val appModule = module {
    includes(platformPushTopicsModule)
    viewModelOf(::MainViewModel)
    viewModelOf(::FeatureFlagsViewModel)
    viewModelOf(::AppUpdateViewModel)
    single {
        CoroutineScope(SupervisorJob() + Dispatchers.Default)
    }
}