package com.kikepb.chat.data.di

import com.kikepb.chat.data.ChatParticipantRepositoryImpl
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val chatDataModule = module {
    singleOf(::ChatParticipantRepositoryImpl) bind ChatParticipantRepositoryImpl::class
}