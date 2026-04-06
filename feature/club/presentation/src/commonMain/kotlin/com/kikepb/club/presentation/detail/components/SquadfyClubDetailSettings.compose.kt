package com.kikepb.club.presentation.detail.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kikepb.club.domain.model.ClubModel
import com.kikepb.core.designsystem.theme.extended

@Composable
fun SquadfyClubDetailSettingsTab(club: ClubModel) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { SquadfyClubDetailTabSectionTitle("Ajustes del club") }

        item {
            SquadfyClubDetailSettingsGroup(title = "Administrador") {
                SquadfyClubDetailSettingsRow(label = "Editar nombre", icon = Icons.Outlined.Edit)
                HorizontalDivider(color = MaterialTheme.colorScheme.extended.surfaceOutline)
                SquadfyClubDetailSettingsRow(label = "Regenerar código de invitación", icon = Icons.Outlined.Refresh)
                HorizontalDivider(color = MaterialTheme.colorScheme.extended.surfaceOutline)
                SquadfyClubDetailSettingsRow(label = "Cambiar privacidad", icon = Icons.Outlined.Lock)
            }
        }

        item {
            SquadfyClubDetailSettingsGroup(title = "Miembro") {
                SquadfyClubDetailSettingsRow(label = "Salir del club", icon = Icons.Outlined.Close, destructive = true)
            }
        }

        item {
            SquadfyClubDetailSettingsGroup(title = "Zona de peligro") {
                SquadfyClubDetailSettingsRow(label = "Eliminar club (solo admin)", icon = Icons.Outlined.Delete, destructive = true)
            }
        }
    }
}

@Composable
private fun SquadfyClubDetailSettingsGroup(title: String, content: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            text = title.uppercase(),
            style = MaterialTheme.typography.labelSmall.copy(
                letterSpacing = 0.8.sp,
                fontWeight = FontWeight.SemiBold
            ),
            color = MaterialTheme.colorScheme.extended.textPlaceholder,
            modifier = Modifier.padding(horizontal = 4.dp)
        )
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 1.dp,
            tonalElevation = 0.dp
        ) {
            Column { content() }
        }
    }
}

@Composable
private fun SquadfyClubDetailSettingsRow(
    label: String,
    icon: ImageVector,
    destructive: Boolean = false,
    onClick: () -> Unit = {}
) {
    val contentColor = if (destructive) MaterialTheme.colorScheme.error
    else MaterialTheme.colorScheme.extended.textPrimary
    val iconColor = if (destructive) MaterialTheme.colorScheme.error
    else MaterialTheme.colorScheme.extended.textPlaceholder

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(18.dp),
            tint = iconColor
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = contentColor,
            modifier = Modifier.weight(1f)
        )
        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = MaterialTheme.colorScheme.extended.textPlaceholder
        )
    }
}