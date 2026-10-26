package com.kikepb.auth.presentation.di

import com.kikepb.auth.presentation.email_verification.EmailVerificationViewModel
import com.kikepb.auth.presentation.forgot_password.ForgotPasswordViewModel
import com.kikepb.auth.presentation.login.LoginViewModel
import com.kikepb.auth.presentation.register.RegisterViewModel
import com.kikepb.auth.presentation.register_success.RegisterSuccessViewModel
import com.kikepb.auth.presentation.reset_password.ResetPasswordViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf
import com.kikepb.auth.presentation.complete_profile.CompleteProfileViewModel
import com.kikepb.auth.presentation.social.SocialSignInViewModel
import org.koin.dsl.module

val authPresentationModule = module {
    viewModelOf(::RegisterViewModel)
    viewModelOf(::RegisterSuccessViewModel)
    viewModelOf(::EmailVerificationViewModel)
    viewModelOf(::LoginViewModel)
    viewModelOf(::ForgotPasswordViewModel)
    viewModelOf(::ResetPasswordViewModel)
    // Spec 019: Google and Apple sign-in
    viewModelOf(::SocialSignInViewModel)
    viewModel { params -> CompleteProfileViewModel(suggestedUsername = params.get(), authRepository = get(), sessionStorage = get()) }
}