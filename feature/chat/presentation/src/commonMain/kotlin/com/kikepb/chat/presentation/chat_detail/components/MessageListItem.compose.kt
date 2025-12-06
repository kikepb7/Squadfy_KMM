package com.kikepb.chat.presentation.chat_detail.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.kikepb.chat.presentation.model.MessageModelUi
import com.kikepb.chat.presentation.provider.MessageListItemProvider
import com.kikepb.chat.presentation.util.getChatBubbleColorForUser
import com.kikepb.core.designsystem.theme.SquadfyTheme
import com.kikepb.core.designsystem.theme.extended
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.jetbrains.compose.ui.tooling.preview.PreviewParameter

@Composable
fun MessageListItem(
    messageUi: MessageModelUi,
    onMessageLongClick: (MessageModelUi.LocalUserMessage) -> Unit,
    onDismissMessageMenu: () -> Unit,
    onDeleteClick: (MessageModelUi.LocalUserMessage) -> Unit,
    onRetryClick: (MessageModelUi.LocalUserMessage) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier) {
        when(messageUi) {
            is MessageModelUi.DateSeparator -> {
                DateSeparatorUi(
                    date = messageUi.date.asString(),
                    modifier = Modifier.fillMaxWidth()
                )
            }
            is MessageModelUi.LocalUserMessage -> {
                LocalUserMessage(
                    message = messageUi,
                    onMessageLongClick = { onMessageLongClick(messageUi) },
                    onDismissMessageMenu = onDismissMessageMenu,
                    onDeleteClick = { onDeleteClick(messageUi) },
                    onRetryClick = { onRetryClick(messageUi) }
                )
            }
            is MessageModelUi.OtherUserMessage -> {
                OtherUserMessage(
                    message = messageUi,
                    color = getChatBubbleColorForUser(userId = messageUi.sender.id)
                )
            }
        }
    }
}

@Composable
private fun DateSeparatorUi(
    date: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        HorizontalDivider(modifier = Modifier.weight(1f))
        Text(
            text = date,
            modifier = Modifier.padding(horizontal = 40.dp),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.extended.textPlaceholder
        )
        HorizontalDivider(modifier = Modifier.weight(1f))
    }
}

@Composable
@Preview
fun MessageListItemCombinedPreview(@PreviewParameter(MessageListItemProvider::class) messageUi: MessageModelUi) {
    SquadfyTheme {
        MessageListItem(
            messageUi = messageUi,
            onRetryClick = {},
            onMessageLongClick = {},
            onDismissMessageMenu = {},
            onDeleteClick = {},
            modifier = when(messageUi) {
                is MessageModelUi.LocalUserMessage -> Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                else -> Modifier.fillMaxWidth()
            }
        )
    }
}