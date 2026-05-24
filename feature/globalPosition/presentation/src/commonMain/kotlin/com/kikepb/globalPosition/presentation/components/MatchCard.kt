package com.kikepb.globalPosition.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kikepb.core.designsystem.components.avatar.AvatarSize
import com.kikepb.core.designsystem.components.avatar.SquadfyAvatarPhoto
import com.kikepb.core.designsystem.theme.SquadfyBrand500
import com.kikepb.core.designsystem.theme.SquadfyRed500
import com.kikepb.core.designsystem.theme.extended
import com.kikepb.globalPosition.presentation.model.MatchStatusUi
import com.kikepb.globalPosition.presentation.model.MatchUiModel

@Composable
fun MatchCard(
    match: MatchUiModel,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 2.dp,
        tonalElevation = 0.dp
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (match.matchday != null) {
                        Text(
                            text = match.matchday,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                            color = MaterialTheme.colorScheme.extended.textPlaceholder
                        )
                        Text(
                            text = "·",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.extended.textPlaceholder.copy(alpha = 0.5f)
                        )
                    }
                    Text(
                        text = match.date,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.extended.textPlaceholder
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (match.clubName != null) {
                        Text(
                            text = match.clubName,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.extended.textSecondary,
                            maxLines = 1
                        )
                    }
                    StatusBadge(status = match.status)
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.extended.surfaceOutline)

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RecentMatchTeamColumn(
                    code = match.firstTeamCode,
                    name = match.firstTeamName,
                    logoUrl = null,
                    modifier = Modifier.weight(1f)
                )

                RecentScoreDisplay(
                    firstScore = match.firstTeamScore,
                    secondScore = match.secondTeamScore,
                    modifier = Modifier.padding(horizontal = 10.dp)
                )

                RecentMatchTeamColumn(
                    code = match.secondTeamCode,
                    name = match.secondTeamName,
                    logoUrl = null,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun RecentMatchTeamColumn(
    code: String,
    name: String,
    logoUrl: String?,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        SquadfyAvatarPhoto(
            displayText = code,
            imageUrl = logoUrl,
            size = AvatarSize.SMALL
        )
        Text(
            text = name,
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
            color = MaterialTheme.colorScheme.extended.textSecondary,
            textAlign = TextAlign.Center,
            maxLines = 2
        )
    }
}

@Composable
private fun RecentScoreDisplay(
    firstScore: Int,
    secondScore: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        ScoreBox(score = firstScore)
        Text(
            text = "–",
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Light),
            color = MaterialTheme.colorScheme.extended.textPlaceholder
        )
        ScoreBox(score = secondScore)
    }
}

@Composable
private fun ScoreBox(score: Int, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(34.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(SquadfyBrand500.copy(alpha = 0.12f)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "$score",
            style = MaterialTheme.typography.titleSmall.copy(
                fontWeight = FontWeight.ExtraBold,
                fontSize = 16.sp
            ),
            color = MaterialTheme.colorScheme.extended.textPrimary
        )
    }
}

@Composable
private fun StatusBadge(status: MatchStatusUi, modifier: Modifier = Modifier) {
    when (status) {
        MatchStatusUi.FINISHED -> {
            Box(
                modifier = modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(MaterialTheme.colorScheme.extended.surfaceLower)
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = "FT",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.4.sp
                    ),
                    color = MaterialTheme.colorScheme.extended.textPlaceholder
                )
            }
        }
        MatchStatusUi.IN_PROGRESS -> {
            Box(
                modifier = modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(SquadfyRed500.copy(alpha = 0.15f))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = "EN VIVO",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.4.sp,
                        color = SquadfyRed500
                    )
                )
            }
        }
        MatchStatusUi.SCHEDULED -> Unit
    }
}
