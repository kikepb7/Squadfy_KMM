package com.kikepb.club.presentation.detail.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kikepb.club.domain.model.ClubMemberModel
import com.kikepb.club.presentation.utils.initialsOf
import com.kikepb.core.designsystem.components.avatar.AvatarSize
import com.kikepb.core.designsystem.components.avatar.SquadfyAvatarPhoto
import com.kikepb.core.designsystem.theme.extended

@Composable
fun SquadfyClubDetailTopScorersSection(
    members: List<ClubMemberModel>,
    onRankingClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        ClubDetailSectionHeader(
            title = "Máximos Goleadores",
            actionLabel = "Ranking completo",
            onActionClick = onRankingClick
        )

        val topScorers = remember(members) {
            members.filter { it.goalsScored > 0 }
                .sortedByDescending { it.goalsScored }
                .take(5)
        }

        if (topScorers.isEmpty()) {
            EmptyTabMessage("Aún no hay goles registrados.")
        } else {
            TopScorersCard(topScorers = topScorers)
        }
    }
}

@Composable
private fun TopScorersCard(topScorers: List<ClubMemberModel>, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 2.dp,
        tonalElevation = 0.dp
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            topScorers.forEachIndexed { index, member ->
                if (index > 0) HorizontalDivider(color = MaterialTheme.colorScheme.extended.surfaceOutline)
                TopScorerRow(rank = index + 1, member = member)
            }
        }
    }
}

@Composable
private fun TopScorerRow(rank: Int, member: ClubMemberModel, modifier: Modifier = Modifier) {
    val isTopThree = rank <= 3
    val rankColor = if (isTopThree) MaterialTheme.colorScheme.primary
    else MaterialTheme.colorScheme.extended.textPlaceholder

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "$rank",
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.ExtraBold,
                fontSize = 18.sp
            ),
            color = rankColor,
            modifier = Modifier.padding(end = 4.dp)
        )

        SquadfyAvatarPhoto(
            displayText = initialsOf(member.username),
            imageUrl = member.profilePictureUrl,
            size = AvatarSize.SMALL
        )

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = member.username,
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.extended.textPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (!member.position.isNullOrBlank()) {
                Text(
                    text = member.position.orEmpty(),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.extended.textPlaceholder,
                    maxLines = 1
                )
            }
        }

        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = "${member.goalsScored}",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 20.sp
                ),
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "goles",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.extended.textPlaceholder
            )
        }
    }
}
