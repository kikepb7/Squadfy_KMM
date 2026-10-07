package org.kikepb.squadfy

import android.app.Application
import android.content.pm.ApplicationInfo
import org.kikepb.squadfy.di.initKoin
import org.kikepb.squadfy.push.PushNotifier
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger

class SquadfyApplication: Application() {

    override fun onCreate() {
        super.onCreate()
        initKoin {
            androidContext(this@SquadfyApplication)
            // AC-011-04: Koin logs only in debuggable builds
            if (applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE != 0) androidLogger()
        }
        PushNotifier.createChannels(this)
    }
}