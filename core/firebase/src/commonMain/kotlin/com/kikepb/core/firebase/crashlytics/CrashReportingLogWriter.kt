package com.kikepb.core.firebase.crashlytics

import co.touchlab.kermit.LogWriter
import co.touchlab.kermit.Severity
import com.kikepb.core.domain.crash.CrashReporter

/**
 * Bridges the app logger (Kermit) to Crashlytics: info and up become breadcrumbs attached to the next report, and
 * errors with a throwable are recorded as non-fatal issues. Log messages must not carry personal data.
 */
class CrashReportingLogWriter(
    private val crashReporter: CrashReporter
) : LogWriter() {

    override fun isLoggable(tag: String, severity: Severity): Boolean = severity >= Severity.Info

    override fun log(severity: Severity, message: String, tag: String, throwable: Throwable?) {
        crashReporter.log("${severity.name.first()}/$tag: $message")
        if (throwable != null && severity >= Severity.Error) {
            crashReporter.recordException(throwable, customKeys = mapOf("log_message" to message))
        }
    }
}
