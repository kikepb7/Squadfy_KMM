package com.kikepb.chat.presentation.di

import com.kikepb.chat.presentation.chat_detail.ChatDetailViewModel
import com.kikepb.chat.presentation.chat_list.ChatListViewModel
import com.kikepb.chat.presentation.chat_list_detail.ChatListDetailViewModel
import com.kikepb.chat.presentation.create_chat.CreateChatViewModel
import com.kikepb.chat.presentation.manage_chat.ManageChatViewModel
import com.kikepb.chat.presentation.profile.ProfileViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val chatPresentationModule = module {
    viewModelOf(::ChatListDetailViewModel)
    viewModelOf(::ChatDetailViewModel)
    viewModelOf(::CreateChatViewModel)
    viewModelOf(::ChatListViewModel)
    viewModelOf(::ManageChatViewModel)
    viewModelOf(::ProfileViewModel)
}