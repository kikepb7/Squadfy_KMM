package com.kikepb.club.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Alignment
import androidx.compose.ui.semantics.Role
import com.kikepb.club.presentation.mapper.toTri
import com.kikepb.core.designsystem.triangulacion.components.PositionGlyph
import com.kikepb.core.designsystem.triangulacion.theme.Radius
import com.kikepb.core.designsystem.triangulacion.theme.Triangulacion
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.kikepb.club.domain.model.PlayerPosition
import com.kikepb.club.presentation.mapper.label
import org.jetbrains.compose.resources.stringResource

/** Single optional selection of a [PlayerPosition]; tapping the selected chip clears it. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PositionChips(
    selected: PlayerPosition?,
    onSelected: (PlayerPosition?) -> Unit,
    modifier: Modifier = Modifier
) {
    FlowRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Spec 018: each position with its glyph (■ ◆ ● ▲), the meaning never depends on colour
        PlayerPosition.entries.forEach { position ->
            val isSelected = selected == position
            val tri = position.toTri() ?: return@forEach
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .heightIn(min = 44.dp)
                    .background(if (isSelected) Triangulacion.colors.ink else Triangulacion.colors.surface200, RoundedCornerShape(Radius.sm))
                    .border(1.5.dp, if (isSelected) Triangulacion.colors.ink else Triangulacion.colors.lineStrong, RoundedCornerShape(Radius.sm))
                    .clickable(role = Role.RadioButton) { onSelected(if (isSelected) null else position) }
                    .padding(horizontal = 14.dp, vertical = 8.dp)
            ) {
                val content = if (isSelected) Triangulacion.colors.surface100 else Triangulacion.colors.ink
                PositionGlyph(position = tri, color = content, size = 10.dp)
                Text(text = stringResource(position.label), style = Triangulacion.type.label, color = content)
            }
        }
    }
}
