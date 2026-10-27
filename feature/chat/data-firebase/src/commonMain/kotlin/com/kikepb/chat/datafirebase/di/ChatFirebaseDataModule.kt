package com.kikepb.chat.datafirebase.di

import com.kikepb.chat.datafirebase.repository.FirebaseChatConnectionClient
import com.kikepb.chat.datafirebase.repository.FirebaseChatParticipantRepository
import com.kikepb.chat.datafirebase.repository.FirebaseChatRepository
import com.kikepb.chat.datafirebase.repository.FirebaseDeviceTokenService
import com.kikepb.chat.datafirebase.repository.FirebaseMessageRepository
import com.kikepb.chat.datafirebase.repository.FirebasePresenceRepository
import com.kikepb.chat.domain.repository.presence.PresenceRepository
import com.kikepb.chat.domain.notification.DeviceTokenService
import com.kikepb.chat.domain.repository.chat.ChatConnectionClient
import com.kikepb.chat.domain.repository.chat.ChatRepository
import com.kikepb.chat.domain.repository.message.MessageRepository
import com.kikepb.chat.domain.repository.participant.ChatParticipantRepository
import com.kikepb.chat.domain.repository.participant.ChatParticipantService
import org.koin.dsl.binds
import org.koin.dsl.module
import com.kikepb.core.domain.update.AppInfo

/** Spec 019: chat, participants, connection state and device tokens on Firebase. */
val chatFirebaseDataModule = module {
    single { FirebaseMessageRepository(auth = get(), firestore = get(), appScope = get()) }
    single<MessageRepository> { get<FirebaseMessageRepository>() }
    single<ChatRepository> { FirebaseChatRepository(auth = get(), firestore = get(), functions = get(), messages = get()) }
    single { FirebaseChatParticipantRepository(auth = get(), firestore = get(), images = get()) } binds
        arrayOf(ChatParticipantRepository::class, ChatParticipantService::class)
    single<ChatConnectionClient> { FirebaseChatConnectionClient(database = get(), appScope = get()) }
    single<DeviceTokenService> { FirebaseDeviceTokenService(auth = get(), firestore = get(), appVersion = get<AppInfo>().versionName) }
    // Created at start: marks me online as soon as I am signed in (AC-019-16)
    single<PresenceRepository> { FirebasePresenceRepository(auth = get(), database = get(), appScope = get()) }
}
