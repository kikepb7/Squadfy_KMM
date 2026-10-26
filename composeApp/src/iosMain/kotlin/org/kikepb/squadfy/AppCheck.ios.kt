package org.kikepb.squadfy

import org.kikepb.squadfy.di.needsAppCheck

/**
 * Spec 019 AC-019-20: read by `AppDelegate.swift`, which installs the App Check provider before `FirebaseApp.configure()`
 * (debug provider in Debug, App Attest in Release). False for the own backend and while using the emulators.
 */
fun shouldInstallAppCheck(): Boolean = needsAppCheck
