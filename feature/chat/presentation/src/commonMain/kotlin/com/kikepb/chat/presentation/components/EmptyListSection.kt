package com.kikepb.chat.presentation.components

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
import squadfy_app.feature.chat.presentation.generated.resources.empty_chat
import squadfy_app.feature.chat.presentation.generated.resources.Res.drawable as RDrawable

@Composable
fun EmptyListSection(
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
        Image(
            painter = painterResource(RDrawable.empty_chat),
            contentDescription = title,
            modifier = Modifier.size(
                if(configuration == MOBILE_LANDSCAPE) 125.dp else 200.dp
            )
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.extended.textPrimary
        )
        Text(
            text = description,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.extended.textSecondary
        )
    }
}