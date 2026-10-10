package com.kikepb.core.designsystem.components.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kikepb.core.designsystem.triangulacion.theme.Triangulacion

data class SquadfyBottomBarItemModel(val label: String, val icon: @Composable () -> Unit)

/**
 * BarraInferior (spec 018): three destinations; the active one sits in a Volt pill with ink icon and a bold label.
 * The bar is surface-200 (shadow-2 in Tiza) and pads the system navigation bar itself.
 */
@Composable
fun SquadfyBottomBar(
    items: List<SquadfyBottomBarItemModel>,
    selectedIndex: Int,
    onItemClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = Triangulacion.colors
    Surface(
        color = colors.surface200,
        shadowElevation = if (colors.isDark) 0.dp else 6.dp,
        modifier = modifier.fillMaxWidth()
    ) {
        Column {
            if (colors.isDark) HorizontalDivider(color = colors.line)
            Row(
                horizontalArrangement = Arrangement.SpaceEvenly,
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(top = 10.dp, bottom = 8.dp)
            ) {
                items.forEachIndexed { index, item ->
                    val selected = index == selectedIndex
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(min = 56.dp)
                            .semantics { this.selected = selected }
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                role = Role.Tab,
                                onClick = { onItemClick(index) }
                            )
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(width = 64.dp, height = 34.dp)
                                .background(if (selected) colors.volt else Color.Transparent, RoundedCornerShape(10.dp))
                        ) {
                            CompositionLocalProvider(LocalContentColor provides if (selected) colors.onVolt else colors.inkMuted) {
                                Box(Modifier.size(24.dp), contentAlignment = Alignment.Center) { item.icon() }
                            }
                        }
                        Text(
                            text = item.label,
                            style = Triangulacion.type.label.copy(fontSize = 12.sp, fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium),
                            color = if (selected) colors.ink else colors.inkMuted
                        )
                    }
                }
            }
        }
    }
}
