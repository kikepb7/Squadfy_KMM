package com.kikepb.chat.presentation.components

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.kikepb.core.designsystem.components.avatar.ChatParticipantModelUi
import com.kikepb.core.designsystem.components.avatar.SquadfyAvatarPhoto
import com.kikepb.core.designsystem.components.divider.SquadfyHorizontalDivider
import com.kikepb.core.designsystem.theme.extended
import com.kikepb.core.designsystem.theme.titleXSmall
import com.kikepb.core.presentation.util.DeviceConfiguration.DESKTOP
import com.kikepb.core.presentation.util.DeviceConfiguration.TABLET_LANDSCAPE
import com.kikepb.core.presentation.util.DeviceConfiguration.TABLET_PORTRAIT
import com.kikepb.core.presentation.util.currentDeviceConfiguration

@Composable
fun ColumnScope.ChatParticipantsSelectionSection(
    existingParticipants: List<ChatParticipantModelUi>,
    selectedParticipants: List<ChatParticipantModelUi>,
    modifier: Modifier = Modifier,
    searchResult: ChatParticipantModelUi? = null
) {
    val deviceConfiguration = currentDeviceConfiguration()
    val rootHeightModifier = when(deviceConfiguration) {
        TABLET_PORTRAIT,
        TABLET_LANDSCAPE,
        DESKTOP -> {
            Modifier
                .animateContentSize()
                .heightIn(min = 200.dp, max = 300.dp)
        }
        else -> Modifier.weight(1f)
    }

    Box(modifier = rootHeightModifier.then(other = modifier)) {
        LazyColumn(
            modifier = Modifier.fillMaxWidth()
        ) {
            items(
                items = existingParticipants,
                key = { "existing_${it.id}" }
            ) { participant ->
                ChatParticipantListItem(
                    participantUi = participant,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            if (existingParticipants.isNotEmpty()) item { SquadfyHorizontalDivider() }

            searchResult?.let {
                item {
                    ChatParticipantListItem(
                        participantUi = searchResult,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            if(selectedParticipants.isNotEmpty() && searchResult == null) {
                items(
                    items = selectedParticipants,
                    key = { it.id }
                ) { participant ->
                    ChatParticipantListItem(
                        participantUi = participant,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

@Composable
fun ChatParticipantListItem(
    participantUi: ChatParticipantModelUi,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(color = MaterialTheme.colorScheme.surface)
            .padding(all = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(space = 12.dp)
    ) {
        SquadfyAvatarPhoto(
            displayText = participantUi.initials,
            imageUrl = participantUi.imageUrl
        )
        Text(
            text = participantUi.username,
            style = MaterialTheme.typography.titleXSmall,
            color = MaterialTheme.colorScheme.extended.textPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}