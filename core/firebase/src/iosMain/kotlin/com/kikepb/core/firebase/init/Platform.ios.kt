package com.kikepb.core.firebase.init

import co.touchlab.crashkios.crashlytics.enableCrashlytics
import co.touchlab.crashkios.crashlytics.setCrashlyticsUnhandledExceptionHook

internal actual val defaultEmulatorHost: String = "localhost"

internal actual fun installCrashHandler() {
    enableCrashlytics()
    setCrashlyticsUnhandledExceptionHook()
}
