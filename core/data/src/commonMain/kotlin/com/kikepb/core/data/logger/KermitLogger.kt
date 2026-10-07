package com.kikepb.core.data.logger

import co.touchlab.kermit.Logger
import co.touchlab.kermit.Severity
import com.kikepb.core.data.BuildKonfig
import com.kikepb.core.domain.logger.SquadfyLogger

object KermitLogger: SquadfyLogger {

    init {
        // AC-011-04: release builds only keep warnings and errors
        Logger.setMinSeverity(if (BuildKonfig.IS_RELEASE) Severity.Warn else Severity.Verbose)
    }

    override fun debug(message: String) = Logger.d(messageString = message)

    override fun info(message: String) = Logger.i(messageString = message)

    override fun warn(message: String) = Logger.w(messageString = message)

    override fun error(message: String, throwable: Throwable?) = Logger.e(messageString = message, throwable = throwable)
}
