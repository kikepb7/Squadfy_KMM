package com.kikepb.domain.di

import com.kikepb.domain.usecase.AuthRegisterUseCase
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val authDomainModule = module {
    singleOf(::AuthRegisterUseCase)
}