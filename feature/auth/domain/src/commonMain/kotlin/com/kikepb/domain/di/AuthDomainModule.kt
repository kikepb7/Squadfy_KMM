package com.kikepb.domain.di

import com.kikepb.domain.usecase.AuthRegisterUseCase
import com.kikepb.domain.usecase.AuthVerifyEmailUseCase
import com.kikepb.domain.usecase.ForgotPasswordUseCase
import com.kikepb.domain.usecase.LoginUseCase
import com.kikepb.domain.usecase.ResendEmailVerificationUseCase
import com.kikepb.domain.usecase.ResetPasswordUseCase
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val authDomainModule = module {
//    single<SafeFlowUseCaseDelegate> { SafeFlowUseCaseDelegate.Default(get() ) }
//    singleOf(::GlobalErrorManager)

    singleOf(::AuthRegisterUseCase)
    singleOf(::AuthVerifyEmailUseCase)
    singleOf(::ResendEmailVerificationUseCase)
    singleOf(::LoginUseCase)
    singleOf(::ForgotPasswordUseCase)
    singleOf(::ResetPasswordUseCase)
}