package org.kikepb.squadfy

import android.app.Application
import org.kikepb.squadfy.di.initKoin
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger

class SquadfyApplication: Application() {

    override fun onCreate() {
        super.onCreate()
        initKoin {
            androidContext(this@SquadfyApplication)
            androidLogger()
        }
    }
}