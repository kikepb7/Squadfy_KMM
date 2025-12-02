package com.kikepb.chat.presentation.chat_detail.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kikepb.chat.domain.models.ChatMessageDeliveryStatus
import com.kikepb.chat.presentation.model.MessageModelUi
import com.kikepb.core.designsystem.components.chat.SquadfyChatBubble
import com.kikepb.core.designsystem.components.chat.TrianglePosition
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
    message: MessageModelUi.LocalUserMessage,
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
        Box(
            modifier = Modifier.weight(1f)
        ) {
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

            DropdownMenu(
                expanded = message.isMenuOpen,
                onDismissRequest = onDismissMessageMenu,
                containerColor = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.extended.surfaceOutline
                )
            ) {
                DropdownMenuItem(
                    text = {
                        Text(
                            text = stringResource(RString.delete_for_everyone),
                            color = MaterialTheme.colorScheme.extended.destructiveHover,
                            fontWeight = FontWeight.Medium
                        )
                    },
                    onClick = {
                        onDismissMessageMenu()
                        onDeleteClick()
                    }
                )
            }
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