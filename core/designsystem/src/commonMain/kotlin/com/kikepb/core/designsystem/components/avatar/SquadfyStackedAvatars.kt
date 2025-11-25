package com.kikepb.core.designsystem.components.avatar

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.kikepb.core.designsystem.theme.SquadfyTheme
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun SquadfyStackedAvatars(
    avatars: List<ChatParticipantModelUi>,
    modifier: Modifier = Modifier,
    size: AvatarSize = AvatarSize.SMALL,
    maxVisible: Int = 2,
    overlapPercentage: Float = 0.4f
) {
    val overlapOffset = -(size.dp * overlapPercentage)

    val visibleAvatars = avatars.take(n = maxVisible)
    val remainingCount = (avatars.size - maxVisible).coerceAtLeast(minimumValue = 0)

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(space = overlapOffset),
        verticalAlignment = Alignment.CenterVertically
    ) {
        visibleAvatars.forEach { avatarUiModel ->
            SquadfyAvatarPhoto(
                displayText = avatarUiModel.initials,
                size = size,
                imageUrl = avatarUiModel.imageUrl
            )
        }

        if (remainingCount > 0) {
            SquadfyAvatarPhoto(
                displayText = "$remainingCount+",
                size = size,
                textColor = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
@Preview
private fun SquadfyStackedAvatarsPreview() {
    SquadfyTheme {
        SquadfyStackedAvatars(
            avatars = listOf(
                ChatParticipantModelUi(
                    id = "1",
                    username = "Enrique",
                    initials = "EN",
                ),
                ChatParticipantModelUi(
                    id = "2",
                    username = "Carmen",
                    initials = "Ca",
                ),
                ChatParticipantModelUi(
                    id = "3",
                    username = "Ana",
                    initials = "AN",
                ),
                ChatParticipantModelUi(
                    id = "4",
                    username = "Julio",
                    initials = "JU",
                ),
            )
        )
    }
}