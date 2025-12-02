package com.kikepb.chat.presentation.chat_detail.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.kikepb.chat.domain.models.ChatMessageDeliveryStatus
import com.kikepb.core.designsystem.theme.extended
import com.kikepb.core.designsystem.theme.labelXSmall
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.resources.vectorResource
import squadfy_app.feature.chat.presentation.generated.resources.Res.string as RString
import squadfy_app.feature.chat.presentation.generated.resources.Res.drawable as RDrawable
import squadfy_app.feature.chat.presentation.generated.resources.check_icon
import squadfy_app.feature.chat.presentation.generated.resources.failed
import squadfy_app.feature.chat.presentation.generated.resources.loading_icon
import squadfy_app.feature.chat.presentation.generated.resources.sending
import squadfy_app.feature.chat.presentation.generated.resources.sent

@Composable
fun MessageStatus(
    status: ChatMessageDeliveryStatus,
    modifier: Modifier = Modifier
) {
    val (text, icon, color) = when (status) {
        ChatMessageDeliveryStatus.SENDING -> Triple(
            stringResource(RString.sending),
            vectorResource(RDrawable.loading_icon),
            MaterialTheme.colorScheme.extended.textDisabled
        )
        ChatMessageDeliveryStatus.SENT -> Triple(
            stringResource(RString.sent),
            vectorResource(RDrawable.check_icon),
            MaterialTheme.colorScheme.extended.textTertiary
        )
        ChatMessageDeliveryStatus.FAILED -> Triple(
            stringResource(RString.failed),
            Icons.Default.Close,
            MaterialTheme.colorScheme.error
        )
    }
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = text,
            tint = color,
            modifier = Modifier.size(14.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = text,
            color = color,
            style = MaterialTheme.typography.labelXSmall
        )
    }
}