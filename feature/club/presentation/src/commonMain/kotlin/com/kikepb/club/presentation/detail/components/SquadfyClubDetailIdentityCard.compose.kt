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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kikepb.club.domain.model.ClubModel
import com.kikepb.core.designsystem.theme.extended

@Composable
fun SquadfyClubDetailIdentityCard(club: ClubModel) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 1.dp,
        tonalElevation = 0.dp
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (!club.description.isNullOrBlank()) {
                Text(
                    text = club.description.orEmpty(),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.extended.textSecondary
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.extended.surfaceOutline)
            }
            SquadfyClubDetailInfoRow(label = "Código de invitación", value = club.invitationCode)
            HorizontalDivider(color = MaterialTheme.colorScheme.extended.surfaceOutline)
            SquadfyClubDetailInfoRow(
                label = "Plantilla",
                value = "${club.membersCount}${club.maxMembers?.let { " / $it" } ?: ""}"
            )
            HorizontalDivider(color = MaterialTheme.colorScheme.extended.surfaceOutline)
            SquadfyClubDetailInfoRow(label = "Temporada", value = "2026")
        }
    }
}

@Composable
private fun SquadfyClubDetailInfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.extended.textPlaceholder
        )
        Text(
            text = value,
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.extended.textPrimary
        )
    }
}