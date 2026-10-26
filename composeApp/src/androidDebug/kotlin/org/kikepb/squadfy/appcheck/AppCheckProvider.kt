package org.kikepb.squadfy.appcheck

import com.google.firebase.appcheck.AppCheckProviderFactory
import com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory

/**
 * Debug builds (spec 019 AC-019-20): the debug provider logs a token ("Enter this debug secret…") that is registered
 * once in Firebase console > App Check > Apps > Manage debug tokens. It is never part of a release build.
 */
internal fun appCheckProviderFactory(): AppCheckProviderFactory = DebugAppCheckProviderFactory.getInstance()
