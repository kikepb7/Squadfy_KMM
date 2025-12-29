package com.kikepb.chat.presentation.chat_detail.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.kikepb.chat.domain.models.ChatMessageDeliveryStatus
import com.kikepb.chat.presentation.model.MessageModelUi.LocalUserMessage
import com.kikepb.core.designsystem.components.chat.SquadfyChatBubble
import com.kikepb.core.designsystem.components.chat.TrianglePosition
import com.kikepb.core.designsystem.components.dropdown.SquadfyDropDownItemModel
import com.kikepb.core.designsystem.components.dropdown.SquadfyDropDownMenu
import com.kikepb.core.designsystem.theme.extended
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.resources.vectorResource
import squadfy_app.feature.chat.presentation.generated.resources.Res.string as RString
import squadfy_app.feature.chat.presentation.generated.resources.Res.drawable as RDrawable
import squadfy_app.feature.chat.presentation.generated.resources.delete_for_everyone
import squadfy_app.feature.chat.presentation.generated.resources.reload_icon
import squadfy_app.feature.chat.presentation.generated.resources.retry
import squadfy_app.feature.chat.presentation.generated.resources.you

@Composable
fun LocalUserMessage(
    message: LocalUserMessage,
    messageWithOpenMenu: LocalUserMessage?,
    onMessageLongClick: () -> Unit,
    onDismissMessageMenu: () -> Unit,
    onDeleteClick: () -> Unit,
    onRetryClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End)
    ) {
        Box {
            SquadfyChatBubble(
                messageContent = message.content,
                sender = stringResource(RString.you),
                formattedDateTime = message.formattedSentTime.asString(),
                trianglePosition = TrianglePosition.RIGHT,
                messageStatus = {
                    MessageStatus(
                        status = message.deliveryStatus
                    )
                },
                onLongClick = {
                    onMessageLongClick()
                }
            )

            SquadfyDropDownMenu(
                isOpen = messageWithOpenMenu?.id == message.id,
                onDismiss = onDismissMessageMenu,
                items = listOf(
                    SquadfyDropDownItemModel(
                        title = stringResource(RString.delete_for_everyone),
                        icon = Icons.Default.Delete,
                        contentColor = MaterialTheme.colorScheme.extended.destructiveHover,
                        onClick = onDeleteClick
                    ),
                )
            )
        }

        if(message.deliveryStatus == ChatMessageDeliveryStatus.FAILED) {
            IconButton(
                onClick = onRetryClick
            ) {
                Icon(
                    imageVector = vectorResource(RDrawable.reload_icon),
                    contentDescription = stringResource(RString.retry),
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}