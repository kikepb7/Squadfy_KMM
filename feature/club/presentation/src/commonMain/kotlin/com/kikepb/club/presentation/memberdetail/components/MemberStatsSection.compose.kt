package com.kikepb.club.presentation.memberdetail.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
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
import com.kikepb.core.designsystem.theme.SquadfyBase0
import com.kikepb.core.designsystem.theme.SquadfyRed200
import com.kikepb.core.designsystem.theme.extended

private val GoalsBg = Color(0xFF0A2418)
private val GoalsText = Color(0xFF4DDEAB)

private val AssistsBg = Color(0xFF0D1E35)
private val AssistsText = Color(0xFF6BAAFF)

private val PointsBg = Color(0xFF252100)
private val PointsText = Color(0xFFD4C040)

private val MinutesBg = Color(0xFF2F3F4F)
private val MinutesText = SquadfyBase0

private val YellowBg = Color(0xFF1E1500)
private val YellowText = Color(0xFFFEE55A)

private val RedBg = Color(0xFF200810)
private val RedText = SquadfyRed200

@Composable
fun MemberStatsSection(member: ClubMemberModel, modifier: Modifier = Modifier) {
    val points = member.goalsScored * 3 + member.assists

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = "Estadísticas",
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                letterSpacing = (-0.2).sp
            ),
            color = MaterialTheme.colorScheme.extended.textPrimary
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            StatTile(
                value = member.goalsScored.toString(),
                label = "Goles",
                bgColor = GoalsBg,
                textColor = GoalsText,
                modifier = Modifier.weight(1f)
            )
            StatTile(
                value = member.assists.toString(),
                label = "Asistencias",
                bgColor = AssistsBg,
                textColor = AssistsText,
                modifier = Modifier.weight(1f)
            )
            StatTile(
                value = points.toString(),
                label = "Puntos",
                bgColor = PointsBg,
                textColor = PointsText,
                modifier = Modifier.weight(1f)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            StatTile(
                value = member.minutesPlayed.toString(),
                label = "Minutos",
                bgColor = MinutesBg,
                textColor = MinutesText,
                modifier = Modifier.weight(1f)
            )
            StatTile(
                value = member.yellowCards.toString(),
                label = "Amarillas",
                bgColor = YellowBg,
                textColor = YellowText,
                modifier = Modifier.weight(1f)
            )
            StatTile(
                value = member.redCards.toString(),
                label = "Rojas",
                bgColor = RedBg,
                textColor = RedText,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun StatTile(
    value: String,
    label: String,
    bgColor: Color,
    textColor: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(bgColor)
            .padding(horizontal = 12.dp, vertical = 14.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 28.sp
                ),
                color = textColor
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = textColor.copy(alpha = 0.65f)
            )
        }
    }
}
