package com.kikepb.chat.domain.di

import com.kikepb.chat.domain.usecases.GetChatParticipantUseCase
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val chatDomainModule = module {
    singleOf(::GetChatParticipantUseCase)
}