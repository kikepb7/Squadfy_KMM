package com.kikepb.club.data.di

import com.kikepb.club.database.DatabaseFactory
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.dsl.module

actual val platformClubDataModule: Module = module {
    single { DatabaseFactory(context = androidContext()) }
}
