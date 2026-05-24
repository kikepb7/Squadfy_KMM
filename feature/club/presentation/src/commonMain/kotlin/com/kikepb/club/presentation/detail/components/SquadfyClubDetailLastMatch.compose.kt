package com.kikepb.club.presentation.detail.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
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
import com.kikepb.club.presentation.detail.model.ClubDetailMatchModel
import com.kikepb.core.designsystem.theme.SquadfyBrand500
import com.kikepb.core.designsystem.theme.SquadfyRed500
import com.kikepb.core.designsystem.theme.extended

@Composable
fun SquadfyClubDetailLastMatchSection(
    lastMatch: ClubDetailMatchModel?,
    onSeeAllClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        ClubDetailSectionHeader(
            title = "Último Partido",
            actionLabel = "Ver todos",
            onActionClick = onSeeAllClick
        )

        if (lastMatch == null) {
            EmptyTabMessage("No hay partidos registrados todavía.")
        } else {
            LastMatchCard(match = lastMatch)
        }
    }
}

@Composable
private fun LastMatchCard(match: ClubDetailMatchModel, modifier: Modifier = Modifier) {
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
                if (match.isFinished) {
                    Box(
                        modifier = Modifier
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
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.extended.surfaceOutline)

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = match.firstTeamName,
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.extended.textPrimary,
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                    TeamCodeBadge(code = match.firstTeamCode)
                }

                Row(
                    modifier = Modifier
                        .wrapContentWidth()
                        .padding(horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    MatchScoreBox(score = match.firstTeamScore)
                    Text(
                        text = "–",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Light),
                        color = MaterialTheme.colorScheme.extended.textPlaceholder
                    )
                    MatchScoreBox(score = match.secondTeamScore)
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    TeamCodeBadge(code = match.secondTeamCode)
                    Text(
                        text = match.secondTeamName,
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.extended.textPrimary,
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                        textAlign = TextAlign.End,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            if (match.venue != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .padding(bottom = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.LocationOn,
                        contentDescription = null,
                        modifier = Modifier.size(12.dp),
                        tint = SquadfyRed500
                    )
                    Text(
                        text = match.venue,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.extended.textPlaceholder
                    )
                }
            }
        }
    }
}

@Composable
private fun TeamCodeBadge(code: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(SquadfyBrand500.copy(alpha = 0.15f))
            .padding(horizontal = 6.dp, vertical = 3.dp)
    ) {
        Text(
            text = code,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.3.sp
            ),
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
private fun MatchScoreBox(score: Int, modifier: Modifier = Modifier) {
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
