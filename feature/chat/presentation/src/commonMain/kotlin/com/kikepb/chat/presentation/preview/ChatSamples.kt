package com.kikepb.chat.presentation.preview

import androidx.compose.foundation.text.input.TextFieldState
import com.kikepb.chat.domain.models.ChatMessageDeliveryStatus
import com.kikepb.chat.domain.models.ChatMessageModel
import com.kikepb.chat.domain.models.ConnectionStateModel
import com.kikepb.chat.presentation.chat_detail.ChatDetailState
import com.kikepb.chat.presentation.chat_list.ChatListState
import com.kikepb.chat.presentation.create_chat.ManageChatState
import com.kikepb.chat.presentation.model.ChatModelUi
import com.kikepb.chat.presentation.model.MessageModelUi
import com.kikepb.chat.presentation.profile.ProfileState
import com.kikepb.core.designsystem.components.avatar.ChatParticipantModelUi
import com.kikepb.core.presentation.util.UiText
import kotlin.time.Instant

/** Sample chat of the mockup 14-chat (spec 018). Only for the previews. */
internal object ChatSamples {
    private fun person(id: String, name: String) = ChatParticipantModelUi(id = id, username = name, initials = name.split(' ').take(2).joinToString("") { it.take(1) })
    val me = person("me", "Álvaro Gil")
    private val alex = person("alex", "Álex Ruiz")
    private val dani = person("dani", "Dani Moreno")
    private val marcos = person("marcos", "Marcos Peña")

    private fun chat(id: String, other: ChatParticipantModelUi, text: String, at: String) = ChatModelUi(
        id = id, localParticipant = me, otherParticipants = listOf(other),
        lastMessage = ChatMessageModel(id = "msg-$id", chatId = id, content = text, createdAt = Instant.parse(at), senderId = other.id, deliveryStatus = ChatMessageDeliveryStatus.SENT),
        lastMessageSenderUsername = other.username
    )

    val chats = listOf(
        chat("c1", alex, "¿Alguien lleva petos el jueves?", "2026-10-13T18:41:00Z"),
        chat("c2", dani, "Me apunto, pero llego justo", "2026-10-13T17:02:00Z"),
        chat("c3", marcos, "Gracias por el pase", "2026-10-12T20:15:00Z")
    )

    val list = ChatListState(chats = chats, localParticipant = me)

    val detail = ChatDetailState(
        chatUi = chats.first(),
        messageTextFieldState = TextFieldState("Yo llevo los naranjas"),
        canSendMessage = true,
        connectionState = ConnectionStateModel.CONNECTED,
        messages = listOf(
            MessageModelUi.OtherUserMessage("1", "¿Alguien lleva petos el jueves?", UiText.DynamicString("20:41"), alex),
            MessageModelUi.LocalUserMessage("2", "Yo llevo los verdes", ChatMessageDeliveryStatus.SENT, UiText.DynamicString("20:43")),
            MessageModelUi.OtherUserMessage("3", "Perfecto, nos vemos a las 20:00", UiText.DynamicString("20:44"), alex),
            MessageModelUi.DateSeparator("4", UiText.DynamicString("Hoy"))
        ).reversed()
    )

    val profile = ProfileState(
        username = "alvarogil", userInitials = "ÁG", emailTextState = TextFieldState("alvaro@correo.es"),
        isAccountDeletionEnabled = true, isCrashReportingAvailable = true
    )

    val createChat = ManageChatState(
        queryTextState = TextFieldState("Da"),
        searchResults = listOf(dani),
        selectedChatParticipants = listOf(alex),
        canAddParticipant = true
    )
}
