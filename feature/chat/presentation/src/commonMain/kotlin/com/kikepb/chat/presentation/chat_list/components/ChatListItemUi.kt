package com.kikepb.chat.presentation.chat_list.components

import androidx.compose.ui.Alignment
import com.kikepb.chat.presentation.util.DateUtils
import com.kikepb.core.designsystem.components.avatar.SquadfyStackedAvatars
import com.kikepb.core.designsystem.triangulacion.components.TriAvatar
import com.kikepb.core.designsystem.triangulacion.components.TriCard
import com.kikepb.core.designsystem.triangulacion.theme.Triangulacion
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.kikepb.chat.domain.models.ChatMessageDeliveryStatus
import com.kikepb.chat.domain.models.ChatMessageModel
import com.kikepb.chat.presentation.components.ChatItemHeaderRow
import com.kikepb.chat.presentation.model.ChatModelUi
import com.kikepb.core.designsystem.components.avatar.ChatParticipantModelUi
import com.kikepb.core.designsystem.theme.SquadfyTheme
import com.kikepb.core.designsystem.theme.extended
import org.jetbrains.compose.ui.tooling.preview.Preview
import kotlin.time.Clock

@Composable
fun ChatListItemUi(
    chat: ChatModelUi,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    // Spec 018: a card per chat with the avatar, the name, the last message and its time
    val colors = Triangulacion.colors
    val isGroupChat = chat.otherParticipants.size > 1
    val other = chat.otherParticipants.firstOrNull()
    TriCard(modifier = modifier, highlighted = isSelected, contentPadding = 14.dp, onClick = onClick) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            if (isGroupChat) {
                SquadfyStackedAvatars(avatars = chat.otherParticipants)
            } else {
                TriAvatar(name = other?.username ?: chat.localParticipant.username, imageUrl = other?.imageUrl, size = 48.dp)
            }
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = if (isGroupChat) chat.otherParticipants.joinToString { it.username } else other?.username ?: chat.localParticipant.username,
                        style = Triangulacion.type.title,
                        color = colors.ink,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    chat.lastMessage?.let {
                        Text(text = DateUtils.formatMessageTime(it.createdAt).asString(), style = Triangulacion.type.bodySm, color = colors.inkMuted)
                    }
                }
                if (chat.lastMessage != null) {
                    val previewMessage = buildAnnotatedString {
                        withStyle(style = SpanStyle(fontWeight = FontWeight.SemiBold, color = colors.ink)) {
                            if (chat.lastMessageSenderUsername != null && isGroupChat) append(chat.lastMessageSenderUsername + ": ")
                        }
                        append(chat.lastMessage.content)
                    }
                    Text(text = previewMessage, style = Triangulacion.type.body, color = colors.inkMuted, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }
        }
    }
}

@Composable
@Preview
fun ChatListItemUiPreview() {
    SquadfyTheme(darkTheme = true) {
        ChatListItemUi(
            isSelected = true,
            modifier = Modifier.fillMaxWidth(),
            chat = ChatModelUi(
                id = "1",
                localParticipant = ChatParticipantModelUi(
                    id = "1",
                    username = "Enrique",
                    initials = "EN",
                ),
                otherParticipants = listOf(
                    ChatParticipantModelUi(
                        id = "2",
                        username = "Carmen",
                        initials = "CA",
                    ),
                    ChatParticipantModelUi(
                        id = "3",
                        username = "Ana",
                        initials = "An",
                    )
                ),
                lastMessage = ChatMessageModel(
                    id = "1",
                    chatId = "1",
                    content = "This is a last chat message that was sent by Enrique " +
                            "and goes over multiple lines to showcase the ellipsis",
                    createdAt = Clock.System.now(),
                    senderId = "1",
                    deliveryStatus = ChatMessageDeliveryStatus.SENT
                ),
                lastMessageSenderUsername = "Enrique"
            )
        )
    }
}