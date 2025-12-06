package com.kikepb.chat.presentation.chat_detail.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.kikepb.chat.presentation.model.MessageModelUi
import com.kikepb.core.designsystem.components.avatar.SquadfyAvatarPhoto
import com.kikepb.core.designsystem.components.chat.SquadfyChatBubble
import com.kikepb.core.designsystem.components.chat.TrianglePosition

@Composable
fun OtherUserMessage(
    message: MessageModelUi.OtherUserMessage,
    color: Color,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        SquadfyAvatarPhoto(
            displayText = message.sender.initials,
            imageUrl = message.sender.imageUrl
        )
        SquadfyChatBubble(
            messageContent = message.content,
            sender = message.sender.username,
            trianglePosition = TrianglePosition.LEFT,
            color = color,
            formattedDateTime = message.formattedSentTime.asString()
        )
    }
}