package com.kikepb.chat.domain.di

import com.kikepb.chat.domain.usecases.CreateChatUseCase
import com.kikepb.chat.domain.usecases.FetchChatsUseCase
import com.kikepb.chat.domain.usecases.GetChatParticipantUseCase
import com.kikepb.chat.domain.usecases.GetChatsUseCase
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val chatDomainModule = module {
    singleOf(::GetChatParticipantUseCase)
    singleOf(::CreateChatUseCase)
    singleOf(::GetChatsUseCase)
    singleOf(::FetchChatsUseCase)
}