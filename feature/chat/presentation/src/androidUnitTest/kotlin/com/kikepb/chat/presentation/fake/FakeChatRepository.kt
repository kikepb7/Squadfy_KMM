package com.kikepb.chat.presentation.fake

import com.kikepb.chat.domain.models.ChatInfoModel
import com.kikepb.chat.domain.models.ChatMessageDeliveryStatus
import com.kikepb.chat.domain.models.ChatMessageModel
import com.kikepb.chat.domain.models.ChatModel
import com.kikepb.chat.domain.models.ChatParticipantModel
import com.kikepb.chat.domain.repository.chat.ChatRepository
import com.kikepb.core.domain.util.DataError
import com.kikepb.core.domain.util.EmptyResult
import com.kikepb.core.domain.util.Result
import com.kikepb.core.domain.util.Result.Success
import kotlin.time.Instant
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class FakeChatRepository : ChatRepository {

    private val chatsFlow = MutableStateFlow<List<ChatModel>>(emptyList())
    private val chatInfoFlow = MutableStateFlow(defaultChatInfoModel())

    var fetchChatsResult: Result<List<ChatModel>, DataError.Remote> = Success(emptyList())
    var fetchChatByIdResult: EmptyResult<DataError.Remote> = Success(Unit)
    var createChatResult: Result<ChatModel, DataError.Remote> = Success(defaultChatModel())
    var leaveChatResult: EmptyResult<DataError.Remote> = Success(Unit)
    var addParticipantsResult: Result<ChatModel, DataError.Remote> = Success(defaultChatModel())
    var deleteAllChatsCalled = false

    override fun getChats(): Flow<List<ChatModel>> = chatsFlow
    override fun getChatInfoById(chatId: String): Flow<ChatInfoModel> = chatInfoFlow
    override fun getActiveParticipantsByChatId(chatId: String): Flow<List<ChatParticipantModel>> =
        MutableStateFlow(emptyList())

    override suspend fun fetchChats(): Result<List<ChatModel>, DataError.Remote> = fetchChatsResult
    override suspend fun fetchChatById(chatId: String): EmptyResult<DataError.Remote> = fetchChatByIdResult
    override suspend fun createChat(otherUserIds: List<String>): Result<ChatModel, DataError.Remote> = createChatResult
    override suspend fun leaveChat(chatId: String): EmptyResult<DataError.Remote> = leaveChatResult
    override suspend fun addParticipantsToChat(chatId: String, userIds: List<String>): Result<ChatModel, DataError.Remote> = addParticipantsResult

    override suspend fun deleteAllChats() {
        deleteAllChatsCalled = true
        chatsFlow.value = emptyList()
    }

    fun emitChats(chats: List<ChatModel>) { chatsFlow.value = chats }

    companion object {
        fun defaultParticipant(userId: String = "user-1", username: String = "testuser") =
            ChatParticipantModel(userId = userId, username = username, profilePictureUrl = null)

        fun defaultMessage(id: String = "msg-1", chatId: String = "chat-1", senderId: String = "user-1") =
            ChatMessageModel(
                id = id,
                chatId = chatId,
                content = "Hello",
                createdAt = Instant.fromEpochMilliseconds(1_700_000_000_000L),
                senderId = senderId,
                deliveryStatus = ChatMessageDeliveryStatus.SENT
            )

        fun defaultChatModel(id: String = "chat-1") = ChatModel(
            id = id,
            participants = listOf(defaultParticipant()),
            lastActivityAt = Instant.fromEpochMilliseconds(1_700_000_000_000L),
            lastMessage = null
        )

        fun defaultChatInfoModel() = ChatInfoModel(
            chat = defaultChatModel(),
            messages = emptyList()
        )
    }
}
