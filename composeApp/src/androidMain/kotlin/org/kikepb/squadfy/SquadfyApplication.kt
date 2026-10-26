package org.kikepb.squadfy

import android.app.Application
import android.content.pm.ApplicationInfo
import com.google.firebase.FirebaseApp
import com.google.firebase.appcheck.FirebaseAppCheck
import org.kikepb.squadfy.appcheck.appCheckProviderFactory
import org.kikepb.squadfy.auth.CurrentActivity
import org.kikepb.squadfy.di.needsAppCheck
import org.kikepb.squadfy.di.initKoin
import org.kikepb.squadfy.push.PushNotifier
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger

class SquadfyApplication: Application() {

    override fun onCreate() {
        super.onCreate()
        // Credential Manager's sheet is anchored to the activity on screen (spec 019)
        registerActivityLifecycleCallbacks(CurrentActivity)
        // Spec 019 AC-019-20: App Check before anything talks to Firebase
        if (needsAppCheck && FirebaseApp.getApps(this).isNotEmpty()) {
            FirebaseAppCheck.getInstance().installAppCheckProviderFactory(appCheckProviderFactory())
        }
        initKoin {
            androidContext(this@SquadfyApplication)
            // AC-011-04: Koin logs only in debuggable builds
            if (applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE != 0) androidLogger()
        }
        PushNotifier.createChannels(this)
    }
}