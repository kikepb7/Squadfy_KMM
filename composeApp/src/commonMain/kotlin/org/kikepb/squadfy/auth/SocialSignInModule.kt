package org.kikepb.squadfy.auth

import org.koin.core.module.Module

/**
 * The platform's Google/Apple sign-in sheets (spec 019 AC-019-23), bound only in the Firebase variant: the own backend
 * has no third-party sign-in and binds `NoSocialCredentialProvider`.
 */
internal expect val platformSocialSignInModule: Module
