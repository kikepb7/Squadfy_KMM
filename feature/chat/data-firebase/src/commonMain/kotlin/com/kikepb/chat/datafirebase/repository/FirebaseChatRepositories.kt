package com.kikepb.chat.datafirebase.repository

import com.kikepb.chat.datafirebase.dto.AddParticipantsRequest
import com.kikepb.chat.datafirebase.dto.ChatDocDto
import com.kikepb.chat.datafirebase.dto.ChatIdRequest
import com.kikepb.chat.datafirebase.dto.ChatIdResponse
import com.kikepb.chat.datafirebase.dto.CreateChatRequest
import com.kikepb.chat.datafirebase.dto.DeviceDto
import com.kikepb.chat.datafirebase.dto.MessageDto
import com.kikepb.chat.datafirebase.dto.NewMessageDto
import com.kikepb.chat.datafirebase.dto.PictureUpdateDto
import com.kikepb.chat.datafirebase.dto.UserDto
import com.kikepb.chat.domain.models.ChatInfoModel
import com.kikepb.chat.domain.models.ChatMessageDeliveryStatus
import com.kikepb.chat.domain.models.ChatMessageModel
import com.kikepb.chat.domain.models.ChatModel
import com.kikepb.chat.domain.models.ChatParticipantModel
import com.kikepb.chat.domain.models.ConnectionStateModel
import com.kikepb.chat.domain.models.MessageWithSenderModel
import com.kikepb.chat.domain.models.OutgoingNewMessageModel
import com.kikepb.chat.domain.models.ProfilePictureUploadUrlsModel
import com.kikepb.chat.domain.notification.DeviceTokenService
import com.kikepb.chat.domain.repository.chat.ChatConnectionClient
import com.kikepb.chat.domain.repository.chat.ChatRepository
import com.kikepb.chat.domain.repository.message.MessageRepository
import com.kikepb.chat.domain.repository.participant.ChatParticipantRepository
import com.kikepb.chat.domain.repository.participant.ChatParticipantService
import com.kikepb.core.domain.util.DataError
import com.kikepb.core.domain.util.EmptyResult
import com.kikepb.core.domain.util.Result
import com.kikepb.core.domain.util.map
import com.kikepb.core.firebase.error.safeFirebaseCall
import com.kikepb.core.firebase.firestore.FirestorePaths
import com.kikepb.core.firebase.firestore.toInstant
import com.kikepb.core.firebase.firestore.toTimestamp
import com.kikepb.core.firebase.firestore.userCollection
import com.kikepb.core.firebase.firestore.userDocument
import com.kikepb.core.firebase.functions.FunctionsClient
import com.kikepb.core.firebase.storage.FirebaseImageStorage
import dev.gitlive.firebase.auth.FirebaseAuth
import dev.gitlive.firebase.database.FirebaseDatabase
import dev.gitlive.firebase.firestore.Direction
import dev.gitlive.firebase.firestore.DocumentSnapshot
import dev.gitlive.firebase.firestore.FirebaseFirestore
import dev.gitlive.firebase.firestore.ServerTimestampBehavior
import dev.gitlive.firebase.firestore.Timestamp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.datetime.TimeZone
import kotlin.time.Clock
import kotlin.time.Instant

/**
 * Chat on Firebase (spec 019 AC-019-15). Firestore listeners replace the WebSocket and the Room cache: the SDK keeps
 * its own offline cache and pending writes, so a message shows as SENDING until the server has it.
 */

private const val PAGE_SIZE = 30

private fun ChatDocDto.participantModels(): List<ChatParticipantModel> =
    participantIds.map { id -> ChatParticipantModel(id, participants[id]?.username.orEmpty(), participants[id]?.pictureUrl) }

private fun ChatDocDto.toModel(id: String): ChatModel {
    val last = lastMessage?.let {
        ChatMessageModel(
            id = it.id, chatId = id, content = it.content, senderId = it.senderId,
            createdAt = it.createdAt?.toInstant() ?: Instant.DISTANT_PAST, deliveryStatus = ChatMessageDeliveryStatus.SENT
        )
    }
    return ChatModel(
        id = id,
        participants = participantModels(),
        lastActivityAt = updatedAt?.toInstant() ?: Instant.DISTANT_PAST,
        lastMessage = last,
        lastMessageSenderUsername = last?.let { participants[it.senderId]?.username }
    )
}

private fun DocumentSnapshot.toChat(): ChatModel? = if (exists) data(ChatDocDto.serializer()).toModel(id) else null

class FirebaseChatRepository(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val functions: FunctionsClient,
    private val messages: FirebaseMessageRepository
) : ChatRepository {

    private val chats get() = firestore.collection(FirestorePaths.CHATS)

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun getChats(): Flow<List<ChatModel>> =
        auth.authStateChanged
            .onStart { emit(auth.currentUser) }
            .map { it?.uid }
            .distinctUntilChanged()
            .flatMapLatest { uid ->
                if (uid == null) flowOf(emptyList())
                else chats.where { "participantIds" contains uid }
                    .orderBy("updatedAt", Direction.DESCENDING)
                    .snapshots
                    .map { snapshot -> snapshot.documents.mapNotNull { it.toChat() } }
                    .catch { emit(emptyList()) }
            }

    override fun getChatInfoById(chatId: String): Flow<ChatInfoModel> =
        combine(
            chats.document(chatId).snapshots.map { it.toChat() }.catch { emit(null) },
            messages.getMessagesForChat(chatId)
        ) { chat, messages -> chat?.let { ChatInfoModel(chat = it, messages = messages) } }
            .map { it ?: ChatInfoModel(chat = emptyChat(chatId), messages = emptyList()) }

    override fun getActiveParticipantsByChatId(chatId: String): Flow<List<ChatParticipantModel>> =
        chats.document(chatId).snapshots
            .map { snapshot -> if (snapshot.exists) snapshot.data(ChatDocDto.serializer()).participantModels() else emptyList() }
            .catch { emit(emptyList()) }

    override suspend fun fetchChats(): Result<List<ChatModel>, DataError.Remote> {
        val uid = auth.currentUser?.uid ?: return Result.Failure(DataError.Remote.UNAUTHORIZED)
        return safeFirebaseCall {
            chats.where { "participantIds" contains uid }.orderBy("updatedAt", Direction.DESCENDING).get()
                .documents.mapNotNull { it.toChat() }
        }
    }

    override suspend fun fetchChatById(chatId: String): EmptyResult<DataError.Remote> =
        safeFirebaseCall { chats.document(chatId).get() }.map { }

    override suspend fun createChat(otherUserIds: List<String>): Result<ChatModel, DataError.Remote> {
        val created = functions.call<CreateChatRequest, ChatIdResponse>("createChat", CreateChatRequest(otherUserIds))
        val id = when (created) {
            is Result.Success -> created.data.id
            is Result.Failure -> return Result.Failure(created.error.status)
        }
        return safeFirebaseCall { chats.document(id).get().toChat() ?: emptyChat(id) }
    }

    override suspend fun leaveChat(chatId: String): EmptyResult<DataError.Remote> =
        when (val result = functions.run("leaveChat", ChatIdRequest(chatId))) {
            is Result.Success -> result
            is Result.Failure -> Result.Failure(result.error.status)
        }

    override suspend fun addParticipantsToChat(chatId: String, userIds: List<String>): Result<ChatModel, DataError.Remote> {
        val added = functions.run("addChatParticipants", AddParticipantsRequest(chatId, userIds))
        if (added is Result.Failure) return Result.Failure(added.error.status)
        return safeFirebaseCall { chats.document(chatId).get().toChat() ?: emptyChat(chatId) }
    }

    /** Nothing to clear: the Firestore cache only serves the signed-in user's reads, which the rules guard. */
    override suspend fun deleteAllChats() = Unit

    private fun emptyChat(id: String) = ChatModel(id = id, participants = emptyList(), lastActivityAt = Instant.DISTANT_PAST, lastMessage = null)
}

class FirebaseMessageRepository(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val appScope: CoroutineScope
) : MessageRepository {

    /** How many recent messages each chat listens to; pagination grows it. */
    private val windows = MutableStateFlow<Map<String, Int>>(emptyMap())
    /** Messages whose write the server refused, so the UI offers to retry (content kept to resend). */
    private val failed = MutableStateFlow<Map<String, OutgoingNewMessageModel>>(emptyMap())
    /** Chat of each message seen, so [deleteMessage] (which only gets the message id) finds its document. */
    private val chatOfMessage = mutableMapOf<String, String>()

    private fun messages(chatId: String) = firestore.collection(FirestorePaths.CHATS).document(chatId).collection(FirestorePaths.MESSAGES)

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun getMessagesForChat(chatId: String): Flow<List<MessageWithSenderModel>> {
        val participants = firestore.collection(FirestorePaths.CHATS).document(chatId).snapshots
            .map { snapshot -> if (snapshot.exists) snapshot.data(ChatDocDto.serializer()).participants else emptyMap() }
            .catch { emit(emptyMap()) }
        val window = windows.map { it[chatId] ?: PAGE_SIZE }.distinctUntilChanged()
        val docs = window.flatMapLatest { size ->
            messages(chatId).orderBy("createdAt", Direction.DESCENDING).limit(size.toLong())
                .snapshots(includeMetadataChanges = true)
                .map { snapshot -> snapshot.documents }
                .catch { emit(emptyList()) }
        }
        return combine(docs, participants, failed) { documents, people, failedMessages ->
            documents.forEach { chatOfMessage[it.id] = chatId }
            val sent = documents.map { doc ->
                val dto = doc.data(MessageDto.serializer(), ServerTimestampBehavior.ESTIMATE)
                val status = if (doc.metadata.hasPendingWrites) ChatMessageDeliveryStatus.SENDING else ChatMessageDeliveryStatus.SENT
                val message = ChatMessageModel(
                    id = doc.id, chatId = chatId, content = dto.content, senderId = dto.senderId,
                    createdAt = dto.createdAt?.toInstant() ?: Clock.System.now(), deliveryStatus = status
                )
                MessageWithSenderModel(message = message, sender = sender(dto.senderId, people), deliveryStatus = status)
            }
            val me = auth.currentUser?.uid.orEmpty()
            val retries = failedMessages.values.filter { it.chatId == chatId && sent.none { s -> s.message.id == it.messageId } }.map {
                val message = ChatMessageModel(it.messageId, chatId, it.content, Clock.System.now(), me, ChatMessageDeliveryStatus.FAILED)
                MessageWithSenderModel(message, sender(me, people), ChatMessageDeliveryStatus.FAILED)
            }
            (retries + sent).sortedByDescending { it.message.createdAt }
        }
    }

    private fun sender(uid: String, people: Map<String, com.kikepb.chat.datafirebase.dto.ParticipantDto>) =
        ChatParticipantModel(uid, people[uid]?.username.orEmpty(), people[uid]?.pictureUrl)

    /** A page of messages before [before] (an instant; null = the latest), and the listener grows to keep them live. */
    override suspend fun fetchMessages(chatId: String, before: String?): Result<List<ChatMessageModel>, DataError> {
        val cursor = before?.let { runCatching { Instant.parse(it).toTimestamp() }.getOrNull() ?: return Result.Success(emptyList()) }
        return safeFirebaseCall {
            val query = messages(chatId).orderBy("createdAt", Direction.DESCENDING)
            val page = (if (cursor == null) query else query.startAfter(cursor)).limit(PAGE_SIZE.toLong()).get().documents
            val loaded = if (cursor == null) PAGE_SIZE else (windows.value[chatId] ?: PAGE_SIZE) + page.size
            windows.value += chatId to maxOf(loaded, windows.value[chatId] ?: PAGE_SIZE)
            page.map { doc ->
                chatOfMessage[doc.id] = chatId
                val dto = doc.data(MessageDto.serializer(), ServerTimestampBehavior.ESTIMATE)
                ChatMessageModel(doc.id, chatId, dto.content, dto.createdAt?.toInstant() ?: Clock.System.now(), dto.senderId, ChatMessageDeliveryStatus.SENT)
            }
        }
    }

    /**
     * Never waits for the network: the SDK queues the write and the listener shows it as SENDING at once. A write the
     * server refuses becomes FAILED and can be retried.
     */
    override suspend fun sendMessage(message: OutgoingNewMessageModel): EmptyResult<DataError> {
        val uid = auth.currentUser?.uid ?: return Result.Failure(DataError.Remote.UNAUTHORIZED)
        failed.value -= message.messageId
        appScope.launch {
            val result = safeFirebaseCall {
                messages(message.chatId).document(message.messageId)
                    .set(NewMessageDto.serializer(), NewMessageDto(uid, message.content.trim(), Timestamp.ServerTimestamp))
            }
            if (result is Result.Failure) failed.value += message.messageId to message
        }
        return Result.Success(Unit)
    }

    override suspend fun retryMessage(messageId: String): EmptyResult<DataError> {
        val message = failed.value[messageId] ?: return Result.Failure(DataError.Local.NOT_FOUND)
        return sendMessage(message)
    }

    override suspend fun deleteMessage(messageId: String): EmptyResult<DataError.Remote> {
        if (failed.value.containsKey(messageId)) {
            failed.value -= messageId
            return Result.Success(Unit)
        }
        val chatId = chatOfMessage[messageId] ?: return Result.Failure(DataError.Remote.NOT_FOUND)
        // The rules let only the author delete it (backend ChatMessageService)
        return safeFirebaseCall { messages(chatId).document(messageId).delete() }
    }

    override suspend fun updateMessageDeliveryStatus(messageId: String, status: ChatMessageDeliveryStatus): EmptyResult<DataError.Local> =
        Result.Success(Unit)
}

/** `users/{uid}`: my participant profile and picture (Storage `users/{uid}/avatar.jpg`). */
class FirebaseChatParticipantRepository(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val images: FirebaseImageStorage
) : ChatParticipantRepository, ChatParticipantService {

    override suspend fun fetchLocalParticipant(): Result<ChatParticipantModel, DataError> = getLocalParticipant()

    override suspend fun getLocalParticipant(): Result<ChatParticipantModel, DataError.Remote> {
        val uid = auth.currentUser?.uid ?: return Result.Failure(DataError.Remote.UNAUTHORIZED)
        return safeFirebaseCall {
            val user = firestore.userDocument(uid).get().data(UserDto.serializer())
            ChatParticipantModel(uid, user.username, user.pictureUrl)
        }
    }

    override suspend fun uploadProfilePicture(imageBytes: ByteArray, mimeType: String): EmptyResult<DataError.Remote> {
        val uid = auth.currentUser?.uid ?: return Result.Failure(DataError.Remote.UNAUTHORIZED)
        val url = when (val uploaded = images.upload(FirebaseImageStorage.userAvatar(uid), imageBytes, mimeType)) {
            is Result.Success -> uploaded.data
            is Result.Failure -> return uploaded
        }
        return safeFirebaseCall {
            firestore.userDocument(uid).set(PictureUpdateDto.serializer(), PictureUpdateDto(url, Timestamp.ServerTimestamp), merge = true)
        }
    }

    override suspend fun deleteProfilePicture(): EmptyResult<DataError.Remote> {
        val uid = auth.currentUser?.uid ?: return Result.Failure(DataError.Remote.UNAUTHORIZED)
        val result = safeFirebaseCall {
            firestore.userDocument(uid).set(PictureUpdateDto.serializer(), PictureUpdateDto(null, Timestamp.ServerTimestamp), merge = true)
        }
        images.delete(FirebaseImageStorage.userAvatar(uid))
        return result
    }

    /** By username prefix, ignoring case (Firestore has no substring search; spec 019 AC-019-15). */
    override suspend fun searchParticipants(query: String): Result<List<ChatParticipantModel>, DataError.Remote> {
        val prefix = query.trim().lowercase()
        if (prefix.isEmpty()) return Result.Success(emptyList())
        val me = auth.currentUser?.uid
        return safeFirebaseCall {
            firestore.collection(FirestorePaths.USERS)
                .orderBy("usernameLower")
                .startAt(prefix).endAt(prefix + "")
                .limit(20).get().documents
                .filter { it.id != me }
                .map { doc -> doc.data(UserDto.serializer()).let { ChatParticipantModel(doc.id, it.username, it.pictureUrl) } }
        }
    }

    // The own backend's presigned uploads: the Firebase variant uploads straight to Storage (uploadProfilePicture)
    override suspend fun getProfilePictureUploadUrl(mimeType: String): Result<ProfilePictureUploadUrlsModel, DataError.Remote> =
        Result.Failure(DataError.Remote.BAD_REQUEST)

    override suspend fun uploadProfilePicture(uploadUrl: String, imageBytes: ByteArray, headers: Map<String, String>): EmptyResult<DataError.Remote> =
        Result.Failure(DataError.Remote.BAD_REQUEST)

    override suspend fun confirmProfilePictureUpload(publicUrl: String): EmptyResult<DataError.Remote> =
        Result.Failure(DataError.Remote.BAD_REQUEST)
}

/** Online state from Realtime Database's `.info/connected` (spec 019 AC-019-16); messages arrive through listeners. */
class FirebaseChatConnectionClient(
    database: FirebaseDatabase,
    appScope: CoroutineScope
) : ChatConnectionClient {

    override val chatMessages: Flow<ChatMessageModel> = emptyFlow()

    override val connectionState: StateFlow<ConnectionStateModel> =
        database.reference(".info/connected").valueEvents
            .map { snapshot -> if (snapshot.value<Boolean?>() == true) ConnectionStateModel.CONNECTED else ConnectionStateModel.DISCONNECTED }
            .catch { emit(ConnectionStateModel.ERROR_UNKNOWN) }
            .stateIn(appScope, SharingStarted.Eagerly, ConnectionStateModel.CONNECTING)
}

/** FCM tokens in `users/{uid}/devices/{token}` (spec 019 T-022), read by the Functions to send pushes. */
class FirebaseDeviceTokenService(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : DeviceTokenService {

    override suspend fun registerToken(token: String, platform: String): EmptyResult<DataError.Remote> {
        val uid = auth.currentUser?.uid ?: return Result.Success(Unit)
        return safeFirebaseCall {
            firestore.userCollection(uid, FirestorePaths.DEVICES).document(token).set(
                DeviceDto.serializer(),
                DeviceDto(
                    platform = platform.lowercase().let { if (it.startsWith("ios")) "ios" else "android" },
                    locale = currentLocaleTag(),
                    timeZone = TimeZone.currentSystemDefault().id,
                    appVersion = "",
                    updatedAt = Timestamp.ServerTimestamp
                )
            )
        }
    }

    override suspend fun unregisterToken(token: String): EmptyResult<DataError.Remote> {
        val uid = auth.currentUser?.uid ?: return Result.Success(Unit)
        return safeFirebaseCall { firestore.userCollection(uid, FirestorePaths.DEVICES).document(token).delete() }
    }
}

/** The user's language and region, e.g. "es-ES". */
internal expect fun currentLocaleTag(): String
