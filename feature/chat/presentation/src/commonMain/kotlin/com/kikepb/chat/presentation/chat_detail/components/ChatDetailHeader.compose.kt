package com.kikepb.chat.presentation.chat_detail.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.kikepb.chat.domain.models.ChatMessageDeliveryStatus
import com.kikepb.chat.domain.models.ChatMessageModel
import com.kikepb.chat.presentation.components.ChatHeader
import com.kikepb.chat.presentation.components.ChatItemHeaderRow
import com.kikepb.chat.presentation.model.ChatModelUi
import com.kikepb.core.designsystem.components.avatar.ChatParticipantModelUi
import com.kikepb.core.designsystem.components.buttons.SquadfyIconButton
import com.kikepb.core.designsystem.components.dropdown.SquadfyDropDownItemModel
import com.kikepb.core.designsystem.components.dropdown.SquadfyDropDownMenu
import com.kikepb.core.designsystem.theme.SquadfyTheme
import com.kikepb.core.designsystem.theme.extended
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.resources.vectorResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import squadfy_app.core.designsystem.generated.resources.arrow_left_icon
import squadfy_app.core.designsystem.generated.resources.dots_icon
import squadfy_app.core.designsystem.generated.resources.log_out_icon
import squadfy_app.feature.chat.presentation.generated.resources.chat_members
import squadfy_app.feature.chat.presentation.generated.resources.go_back
import squadfy_app.feature.chat.presentation.generated.resources.leave_chat
import squadfy_app.feature.chat.presentation.generated.resources.open_chat_options_menu
import squadfy_app.feature.chat.presentation.generated.resources.user_icon
import squadfy_app.feature.chat.presentation.generated.resources.Res.string as RString
import squadfy_app.feature.chat.presentation.generated.resources.Res.drawable as RDrawable
import squadfy_app.core.designsystem.generated.resources.Res.drawable as DesignSystemDrawable
import kotlin.time.Clock

@Composable
fun ChatDetailHeader(
    chatUi: ChatModelUi?,
    isDetailPresent: Boolean,
    isChatOptionsDropDownOpen: Boolean,
    onChatOptionsClick: () -> Unit,
    onDismissChatOptions: ()  -> Unit,
    onManageChatClick: () -> Unit,
    onLeaveChatClick: () -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(color = MaterialTheme.colorScheme.surface),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (!isDetailPresent) {
            SquadfyIconButton(
                onClick = onBackClick
            ) {
                Icon(
                    imageVector = vectorResource(DesignSystemDrawable.arrow_left_icon),
                    contentDescription = stringResource(RString.go_back),
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.extended.textSecondary
                )
            }
        }

        chatUi?.let {
            val isGroupChat = chatUi.otherParticipants.size > 1

            ChatItemHeaderRow(
                chat = chatUi,
                isGroupChat = isGroupChat,
                modifier = Modifier
                    .weight(1f)
                    .clickable {
                        onManageChatClick()
                    }
            )
        } ?: Spacer(modifier = Modifier.weight(1f))

        Box {
            SquadfyIconButton(
                onClick = onChatOptionsClick
            ) {
                Icon(
                    imageVector = vectorResource(DesignSystemDrawable.dots_icon),
                    contentDescription = stringResource(RString.open_chat_options_menu),
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.extended.textSecondary
                )
            }

            SquadfyDropDownMenu(
                isOpen = isChatOptionsDropDownOpen,
                onDismiss = onDismissChatOptions,
                items = listOf(
                    SquadfyDropDownItemModel(
                        title = stringResource(RString.chat_members),
                        icon = vectorResource(RDrawable.user_icon),
                        contentColor = MaterialTheme.colorScheme.extended.textSecondary,
                        onClick = onManageChatClick
                    ),
                    SquadfyDropDownItemModel(
                        title = stringResource(RString.leave_chat),
                        icon = vectorResource(DesignSystemDrawable.log_out_icon),
                        contentColor = MaterialTheme.colorScheme.extended.destructiveHover,
                        onClick = onLeaveChatClick
                    ),
                )
            )
        }
    }
}

@Composable
@Preview
fun ChatDetailHeaderPreview() {
    SquadfyTheme {
        Column(modifier = Modifier.fillMaxSize()) {
            ChatHeader {
                ChatDetailHeader(
                    isDetailPresent = false,
                    isChatOptionsDropDownOpen = true,
                    chatUi = ChatModelUi(
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
                                initials = "AN",
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
                    ),
                    onChatOptionsClick = {},
                    onManageChatClick = {},
                    onLeaveChatClick = {},
                    onDismissChatOptions = {},
                    onBackClick = {},
                )
            }
        }
    }
}