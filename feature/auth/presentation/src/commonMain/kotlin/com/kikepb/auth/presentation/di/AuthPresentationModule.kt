package com.kikepb.auth.presentation.di

import com.kikepb.auth.presentation.email_verification.EmailVerificationViewModel
import com.kikepb.auth.presentation.login.LoginViewModel
import com.kikepb.auth.presentation.register.RegisterViewModel
import com.kikepb.auth.presentation.register_success.RegisterSuccessViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val authPresentationModule = module {
    viewModelOf(::RegisterViewModel)
    viewModelOf(::RegisterSuccessViewModel)
    viewModelOf(::EmailVerificationViewModel)
    viewModelOf(::LoginViewModel)
}