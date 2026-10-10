package com.kikepb.chat.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.CircleShape
import com.kikepb.core.designsystem.triangulacion.components.TriIcon
import com.kikepb.core.designsystem.triangulacion.components.TriIconView
import com.kikepb.core.designsystem.triangulacion.theme.Triangulacion
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.kikepb.core.designsystem.theme.extended
import com.kikepb.core.presentation.util.DeviceConfiguration.MOBILE_LANDSCAPE
import com.kikepb.core.presentation.util.currentDeviceConfiguration
import org.jetbrains.compose.resources.painterResource
import squadfy_app.feature.chat.presentation.generated.resources.Res.drawable as RDrawable

@Composable
fun EmptySection(
    title: String,
    description: String,
    modifier: Modifier = Modifier
) {

    val configuration = currentDeviceConfiguration()

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Spec 018: the chat glyph in a sunken circle instead of the chat template illustration
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(if (configuration == MOBILE_LANDSCAPE) 88.dp else 120.dp)
                .background(Triangulacion.colors.surface300, CircleShape)
        ) {
            TriIconView(icon = TriIcon.Chat, contentDescription = null, size = 48.dp, tint = Triangulacion.colors.accentText)
        }
        Spacer(modifier = Modifier.height(12.dp))
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = title,
            style = Triangulacion.type.displayMd,
            color = Triangulacion.colors.ink
        )
        Text(
            text = description,
            style = Triangulacion.type.body,
            color = Triangulacion.colors.inkMuted
        )
    }
}