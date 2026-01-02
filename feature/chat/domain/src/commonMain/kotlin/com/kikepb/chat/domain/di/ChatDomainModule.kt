package com.kikepb.chat.domain.di

import com.kikepb.chat.domain.usecases.AddParticipantsToChatUseCase
import com.kikepb.chat.domain.usecases.CreateChatUseCase
import com.kikepb.chat.domain.usecases.FetchChatByIdUseCase
import com.kikepb.chat.domain.usecases.FetchChatsUseCase
import com.kikepb.chat.domain.usecases.GetActiveParticipantsByChatIdUseCase
import com.kikepb.chat.domain.usecases.GetChatInfoByIdUseCase
import com.kikepb.chat.domain.usecases.GetChatParticipantUseCase
import com.kikepb.chat.domain.usecases.GetChatsUseCase
import com.kikepb.chat.domain.usecases.LeaveChatUseCase
import com.kikepb.chat.domain.usecases.message.DeleteMessageUseCase
import com.kikepb.chat.domain.usecases.message.FetchMessagesUseCase
import com.kikepb.chat.domain.usecases.message.GetMessagesForChatUseCase
import com.kikepb.chat.domain.usecases.message.RetryMessageUseCase
import com.kikepb.chat.domain.usecases.message.SendMessageUseCase
import com.kikepb.chat.domain.usecases.profile.ChangePasswordUseCase
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val chatDomainModule = module {
    singleOf(::GetChatParticipantUseCase)
    singleOf(::CreateChatUseCase)
    singleOf(::GetChatsUseCase)
    singleOf(::GetChatInfoByIdUseCase)
    singleOf(::FetchChatsUseCase)
    singleOf(::FetchChatByIdUseCase)
    singleOf(::LeaveChatUseCase)
    singleOf(::AddParticipantsToChatUseCase)
    singleOf(::GetActiveParticipantsByChatIdUseCase)
    singleOf(::FetchMessagesUseCase)
    singleOf(::GetMessagesForChatUseCase)
    singleOf(::SendMessageUseCase)
    singleOf(::RetryMessageUseCase)
    singleOf(::DeleteMessageUseCase)
    singleOf(::ChangePasswordUseCase)
}