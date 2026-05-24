package com.kikepb.club.presentation.detail.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kikepb.club.domain.model.ClubMemberModel
import com.kikepb.club.presentation.detail.model.StandingRowUiModel
import com.kikepb.club.presentation.utils.initialsOf
import com.kikepb.club.presentation.utils.toStandingRow
import com.kikepb.core.designsystem.components.avatar.AvatarSize
import com.kikepb.core.designsystem.components.avatar.SquadfyAvatarPhoto
import com.kikepb.core.designsystem.theme.extended

private val RankWidth = 32.dp
private val StatWidth = 40.dp

@Composable
fun SquadfyClubDetailClassificationSection(
    members: List<ClubMemberModel>,
    onMemberClick: (String) -> Unit,
    onSeeAllClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        ClubDetailSectionHeader(
            title = "Clasificación",
            actionLabel = "Ver completa",
            onActionClick = onSeeAllClick
        )

        if (members.isEmpty()) {
            EmptyTabMessage("Aún no hay jugadores en este club.")
        } else {
            val standings = remember(members) {
                members.map { it.toStandingRow() }
                    .sortedWith(
                        compareByDescending<StandingRowUiModel> { it.points }
                            .thenByDescending { it.goals }
                    )
            }
            ClassificationTable(standings = standings, onMemberClick = onMemberClick)
        }
    }
}

@Composable
private fun ClassificationTable(
    standings: List<StandingRowUiModel>,
    onMemberClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 2.dp,
        tonalElevation = 0.dp
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.08f))
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "#",
                    modifier = Modifier.width(RankWidth),
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "JUGADOR",
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.4.sp
                    ),
                    color = MaterialTheme.colorScheme.primary
                )
                listOf("G", "A", "Pts").forEach { label ->
                    Text(
                        text = label,
                        modifier = Modifier.width(StatWidth),
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary,
                        textAlign = TextAlign.Center
                    )
                }
            }

            standings.take(5).forEachIndexed { index, row ->
                if (index > 0) HorizontalDivider(color = MaterialTheme.colorScheme.extended.surfaceOutline)
                ClassificationRow(
                    rank = index + 1,
                    row = row,
                    onClick = { onMemberClick(row.memberId) }
                )
            }
        }
    }
}

@Composable
private fun ClassificationRow(rank: Int, row: StandingRowUiModel, onClick: () -> Unit) {
    val isTopThree = rank <= 3
    val rankColor = if (isTopThree) MaterialTheme.colorScheme.primary
    else MaterialTheme.colorScheme.extended.textPlaceholder
    val ptsColor = if (isTopThree) MaterialTheme.colorScheme.primary
    else MaterialTheme.colorScheme.extended.textPrimary

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "$rank",
            modifier = Modifier.width(RankWidth),
            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
            color = rankColor,
            textAlign = TextAlign.Center
        )

        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            SquadfyAvatarPhoto(
                displayText = initialsOf(row.playerName),
                imageUrl = null,
                size = AvatarSize.SMALL
            )
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = row.playerName,
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.extended.textPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "#${row.shirtNumber}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.extended.textPlaceholder
                )
            }
        }

        Text(
            text = row.goals.toString(),
            modifier = Modifier.width(StatWidth),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.extended.textSecondary,
            textAlign = TextAlign.Center
        )
        Text(
            text = row.assists.toString(),
            modifier = Modifier.width(StatWidth),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.extended.textSecondary,
            textAlign = TextAlign.Center
        )
        Text(
            text = row.points.toString(),
            modifier = Modifier.width(StatWidth),
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
            color = ptsColor,
            textAlign = TextAlign.Center
        )
    }
}