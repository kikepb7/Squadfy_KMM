package com.kikepb.club.presentation.memberdetail.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kikepb.club.domain.model.ClubMemberModel
import com.kikepb.core.designsystem.theme.SquadfyBrand500
import com.kikepb.core.designsystem.theme.extended

private val BlueAccent = Color(0xFF6BAAFF)
private val YellowAccent = Color(0xFFFEE55A)

@Composable
fun MemberPerformanceSection(member: ClubMemberModel, modifier: Modifier = Modifier) {
    val matchesPlayed = member.matchesPlayed.takeIf { it > 0 } ?: 1

    val goalsPerMatch = member.goalsScored.toFloat() / matchesPlayed
    val assistsPerMatch = member.assists.toFloat() / matchesPlayed
    val minutesAvg = member.minutesPlayed.toFloat() / matchesPlayed

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 2.dp,
        tonalElevation = 0.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = "RENDIMIENTO POR PARTIDO",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.8.sp
                ),
                color = MaterialTheme.colorScheme.extended.textPlaceholder
            )

            PerformanceRow(
                label = "Goles por partido",
                value = goalsPerMatch,
                displayValue = formatRate(goalsPerMatch),
                fraction = (goalsPerMatch / 2f).coerceIn(0f, 1f),
                barColor = SquadfyBrand500
            )
            PerformanceRow(
                label = "Asistencias por partido",
                value = assistsPerMatch,
                displayValue = formatRate(assistsPerMatch),
                fraction = (assistsPerMatch / 2f).coerceIn(0f, 1f),
                barColor = BlueAccent
            )
            PerformanceRow(
                label = "Minutos de media",
                value = minutesAvg,
                displayValue = minutesAvg.toInt().toString(),
                fraction = (minutesAvg / 90f).coerceIn(0f, 1f),
                barColor = YellowAccent
            )
        }
    }
}

@Composable
private fun PerformanceRow(
    label: String,
    value: Float,
    displayValue: String,
    fraction: Float,
    barColor: Color,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.extended.textSecondary
            )
            Text(
                text = displayValue,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.ExtraBold
                ),
                color = barColor
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(barColor.copy(alpha = 0.15f))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(fraction = fraction)
                    .fillMaxHeight()
                    .background(barColor)
            )
        }
    }
}

private fun formatRate(value: Float): String {
    val intPart = value.toInt()
    val decPart = ((value - intPart) * 10).toInt()
    return if (decPart == 0) "$intPart" else "$intPart.$decPart"
}
