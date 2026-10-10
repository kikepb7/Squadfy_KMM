package com.kikepb.core.designsystem.components.chat

import com.kikepb.core.designsystem.triangulacion.theme.Triangulacion
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.luminance
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.kikepb.core.designsystem.components.chat.TrianglePosition.LEFT
import com.kikepb.core.designsystem.components.chat.TrianglePosition.RIGHT
import com.kikepb.core.designsystem.theme.SquadfyTheme
import com.kikepb.core.designsystem.theme.extended
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun SquadfyChatBubble(
    messageContent: String,
    sender: String,
    formattedDateTime: String,
    trianglePosition: TrianglePosition,
    modifier: Modifier = Modifier,
    // Spec 018: own messages on Volt, the others on surface-200
    color: Color = Triangulacion.colors.volt,
    messageStatus: @Composable (() -> Unit)? = null,
    triangleSize: Dp = 16.dp,
    onLongClick: (() -> Unit)? = null
) {
    val padding = 12.dp
    val onBubble = if (color.luminance() > 0.5f) Triangulacion.colors.onVolt else Triangulacion.colors.ink

    Column(
        modifier = modifier
            .then(
                other = if (onLongClick != null) {
                    Modifier.combinedClickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = ripple(
                            color = MaterialTheme.colorScheme.extended.surfaceOutline
                        ),
                        onLongClick = onLongClick,
                        onClick = {}
                    )
                } else Modifier
            )
            .clip(
                shape = ChatBubbleShape(trianglePosition = trianglePosition, triangleSize = triangleSize)
            )
            .background(color = color)
            .padding(
                start = if (trianglePosition == LEFT) padding + triangleSize else padding,
                end = if (trianglePosition == RIGHT) padding + triangleSize else padding,
                top = padding,
                bottom = padding
            )
            .width(IntrinsicSize.Max),
        verticalArrangement = Arrangement.spacedBy(space = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = sender,
                style = Triangulacion.type.label.copy(fontSize = 12.sp),
                color = onBubble.copy(alpha = 0.72f),
                modifier = Modifier.weight(weight = 1f)
            )
            Spacer(modifier = Modifier.width(width = 20.dp))
            Text(
                text = formattedDateTime,
                style = Triangulacion.type.label.copy(fontSize = 12.sp),
                color = onBubble.copy(alpha = 0.72f),
            )
        }
        Text(
            text = messageContent,
            style = Triangulacion.type.body.copy(fontSize = 16.sp, lineHeight = 22.sp),
            color = onBubble,
            modifier = Modifier.fillMaxWidth()
        )
        messageStatus?.invoke()
    }
}

@Preview
@Composable
private fun SquadfyChatBubbleLeftPreview() {
    SquadfyTheme {
        SquadfyChatBubble(
            messageContent = "Hello world, this is a longer message",
            sender = "Enrique",
            formattedDateTime = "Monday 2:40pm",
            trianglePosition = LEFT,
            color = MaterialTheme.colorScheme.surface
        )
    }
}

@Preview
@Composable
private fun SquadfyChatBubbleRightPreview() {
    SquadfyTheme {
        SquadfyChatBubble(
            messageContent = "Hello world, this is a longer message",
            sender = "Enrique",
            formattedDateTime = "Monday 2:40pm",
            trianglePosition = RIGHT,
        )
    }
}