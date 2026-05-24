package com.kikepb.club.presentation.memberdetail.components

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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kikepb.club.presentation.memberdetail.MemberMatchParticipation
import com.kikepb.core.designsystem.theme.SquadfyBrand500
import com.kikepb.core.designsystem.theme.extended

@Composable
fun MemberParticipationSection(
    participation: List<MemberMatchParticipation>,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = "Participación",
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                letterSpacing = (-0.2).sp
            ),
            color = MaterialTheme.colorScheme.extended.textPrimary
        )

        if (participation.isEmpty()) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 2.dp,
                tonalElevation = 0.dp
            ) {
                Text(
                    text = "Sin partidos registrados",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.extended.textPlaceholder,
                    modifier = Modifier.padding(24.dp)
                )
            }
        } else {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 2.dp,
                tonalElevation = 0.dp
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    participation.forEachIndexed { index, match ->
                        if (index > 0) {
                            HorizontalDivider(
                                color = MaterialTheme.colorScheme.extended.surfaceOutline
                            )
                        }
                        ParticipationMatchRow(match = match)
                    }
                }
            }
        }
    }
}

@Composable
private fun ParticipationMatchRow(match: MemberMatchParticipation) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = match.matchday,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = MaterialTheme.colorScheme.extended.textPlaceholder
                )
                Text(
                    text = "·",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.extended.textPlaceholder
                )
                Text(
                    text = match.date,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.extended.textPlaceholder
                )
            }
            Text(
                text = "${match.homeScore}–${match.awayScore}",
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.ExtraBold
                ),
                color = MaterialTheme.colorScheme.extended.textPrimary
            )
        }

        Text(
            text = "${match.homeTeam} vs ${match.awayTeam}",
            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.extended.textPrimary
        )

        if (match.goalMinutes.isNotEmpty() || match.assistMinutes.isNotEmpty()) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                match.goalMinutes.forEach { minute ->
                    EventChip(emoji = "⚽", minute = minute, color = SquadfyBrand500)
                }
                match.assistMinutes.forEach { minute ->
                    EventChip(emoji = "🅰", minute = minute, color = MaterialTheme.colorScheme.extended.textPlaceholder)
                }
            }
        }
    }
}

@Composable
private fun EventChip(emoji: String, minute: Int, color: androidx.compose.ui.graphics.Color) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = emoji,
            style = MaterialTheme.typography.labelSmall,
        )
        Text(
            text = "$minute'",
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
            color = color
        )
    }
}
