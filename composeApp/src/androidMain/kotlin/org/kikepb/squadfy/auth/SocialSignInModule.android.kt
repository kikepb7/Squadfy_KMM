package org.kikepb.squadfy.auth

import com.kikepb.core.domain.auth.repository.SocialCredentialProvider
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

internal actual val platformSocialSignInModule = module {
    single<SocialCredentialProvider> { AndroidSocialCredentialProvider(context = androidContext(), logger = get()) }
}
