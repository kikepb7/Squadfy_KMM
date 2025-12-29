package com.kikepb.chat.presentation.chat_detail.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.kikepb.chat.presentation.components.EmptySection
import com.kikepb.chat.presentation.model.MessageModelUi
import com.kikepb.chat.presentation.model.MessageModelUi.LocalUserMessage
import org.jetbrains.compose.resources.stringResource
import squadfy_app.feature.chat.presentation.generated.resources.no_messages
import squadfy_app.feature.chat.presentation.generated.resources.no_messages_subtitle
import squadfy_app.feature.chat.presentation.generated.resources.Res.string as RString

@Composable
fun MessageList(
    messages: List<MessageModelUi>,
    messageWithOpenMenu: LocalUserMessage?,
    listState: LazyListState,
    onMessageLongClick: (LocalUserMessage) -> Unit,
    onMessageRetryClick: (LocalUserMessage) -> Unit,
    onDismissMessageMenu: () -> Unit,
    onDeleteMessageClick: (LocalUserMessage) -> Unit,
    modifier: Modifier = Modifier
) {
    if (messages.isEmpty()) {
        Box(
            modifier = modifier.padding(vertical = 32.dp),
            contentAlignment = Alignment.Center
        ) {
            EmptySection(
                title = stringResource(RString.no_messages),
                description = stringResource(RString.no_messages_subtitle),
            )
        }
    } else {
        LazyColumn(
            modifier = modifier,
            state = listState,
            contentPadding = PaddingValues(16.dp),
            reverseLayout = true,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(
                items = messages,
                key = { it.id }
            ) { message ->
                MessageListItem(
                    messageUi = message,
                    messageWithOpenMenu = messageWithOpenMenu,
                    onMessageLongClick = onMessageLongClick,
                    onDismissMessageMenu = onDismissMessageMenu,
                    onDeleteClick = onDeleteMessageClick,
                    onRetryClick = onMessageRetryClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .animateItem()
                )
            }
        }
    }
}