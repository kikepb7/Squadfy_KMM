package com.kikepb.domain.di

import com.kikepb.domain.usecase.AuthRegisterUseCase
import com.kikepb.domain.usecase.AuthVerifyEmailUseCase
import com.kikepb.domain.usecase.LoginUseCase
import com.kikepb.domain.usecase.ResendEmailVerificationUseCase
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val authDomainModule = module {
    singleOf(::AuthRegisterUseCase)
    singleOf(::AuthVerifyEmailUseCase)
    singleOf(::ResendEmailVerificationUseCase)
    singleOf(::LoginUseCase)
}