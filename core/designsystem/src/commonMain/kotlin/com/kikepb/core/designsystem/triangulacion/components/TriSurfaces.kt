package com.kikepb.core.designsystem.triangulacion.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.kikepb.core.designsystem.theme.SquadfyTheme
import com.kikepb.core.designsystem.triangulacion.theme.Radius
import com.kikepb.core.designsystem.triangulacion.theme.Space
import com.kikepb.core.designsystem.triangulacion.theme.Triangulacion
import org.jetbrains.compose.ui.tooling.preview.Preview

/**
 * Card on surface-200 (shadow-1 in Tiza; Noche lifts with the surface instead). [cut] gives it the Corte, for cards
 * with identity. [highlighted] marks «you» rows with success-soft.
 */
@Composable
fun TriCard(
    modifier: Modifier = Modifier,
    cut: Boolean = false,
    highlighted: Boolean = false,
    contentPadding: Dp = Space.s4,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val colors = Triangulacion.colors
    val shape: Shape = if (cut) CutMd else RoundedCornerShape(Radius.md)
    Surface(
        shape = shape,
        color = if (highlighted) colors.successSoft else colors.surface200,
        contentColor = colors.ink,
        shadowElevation = if (colors.isDark || highlighted) 0.dp else 1.dp,
        modifier = modifier.then(if (onClick != null) Modifier.clickable(role = Role.Button, onClick = onClick) else Modifier)
    ) {
        Column(modifier = Modifier.padding(contentPadding), content = content)
    }
}

enum class TriTagTone { Ok, Warn, Danger, Neutral, Inverse, Brand, Dashed }

/** Small caps tag: «OWNER», «CONVOCADO», «GANASTE», «POR CERRAR». The word carries the meaning, not the colour. */
@Composable
fun TriTag(
    text: String,
    modifier: Modifier = Modifier,
    tone: TriTagTone = TriTagTone.Ok,
    icon: TriIcon? = null
) {
    val colors = Triangulacion.colors
    val (background, content) = when (tone) {
        TriTagTone.Ok -> colors.successSoft to colors.accentText
        TriTagTone.Warn -> colors.warningSoft to colors.warning
        TriTagTone.Danger -> colors.dangerSoft to colors.danger
        TriTagTone.Neutral -> colors.surface300 to colors.ink
        TriTagTone.Inverse -> colors.surfaceInverse to colors.onInverse
        TriTagTone.Brand -> colors.brand to colors.onBrand
        TriTagTone.Dashed -> Color.Transparent to colors.inkMuted
    }
    val shape = RoundedCornerShape(Radius.xs)
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        modifier = modifier
            .heightIn(min = 22.dp)
            .background(background, shape)
            .then(
                if (tone == TriTagTone.Dashed) Modifier.drawBehind {
                    drawRoundRect(
                        color = colors.lineStrong,
                        cornerRadius = CornerRadius(Radius.xs.toPx()),
                        style = Stroke(width = 1.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 3f)))
                    )
                } else Modifier
            )
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        if (icon != null) TriIconView(icon = icon, contentDescription = null, size = 12.dp, tint = content)
        Text(text = text.uppercase(), style = Triangulacion.type.labelCaps, color = content, maxLines = 1)
    }
}

/** Filter / option chip: selected = ink fill, otherwise outlined with line-strong. */
@Composable
fun TriChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: TriIcon? = null,
    enabled: Boolean = true
) {
    val colors = Triangulacion.colors
    val shape = RoundedCornerShape(Radius.sm)
    val content = if (selected) colors.surface100 else colors.ink
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        modifier = modifier
            .heightIn(min = 40.dp)
            .background(if (selected) colors.ink else colors.surface200, shape)
            .border(BorderStroke(1.5.dp, if (selected) colors.ink else colors.lineStrong), shape)
            .clickable(enabled = enabled, role = Role.Tab, onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        if (icon != null) TriIconView(icon = icon, contentDescription = null, size = 18.dp, tint = content)
        Text(text = text, style = Triangulacion.type.label, color = if (enabled) content else colors.inkSubtle, maxLines = 1)
    }
}

/** Segmented tabs of the club screen (Partido, Equipos, Tabla, Miembros). */
@Composable
fun TriTabs(
    items: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = Triangulacion.colors
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(colors.surface300, RoundedCornerShape(Radius.md))
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        items.forEachIndexed { index, item ->
            val selected = index == selectedIndex
            Surface(
                shape = RoundedCornerShape(Radius.sm),
                color = if (selected) colors.surface200 else Color.Transparent,
                shadowElevation = if (selected && !colors.isDark) 1.dp else 0.dp,
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = 44.dp)
                    .clickable(role = Role.Tab, onClick = { onSelect(index) })
            ) {
                Row(horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = item,
                        style = Triangulacion.type.label.copy(fontSize = Triangulacion.type.body.fontSize),
                        color = if (selected) colors.ink else colors.inkMuted,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 12.dp)
                    )
                }
            }
        }
    }
}

enum class TriNoticeTone { Info, Ok, Warn, Danger }

/** Notice (Aviso): icon + title + text on a soft background. Always with an icon or a word, never colour alone. */
@Composable
fun TriNotice(
    title: String?,
    modifier: Modifier = Modifier,
    body: String? = null,
    tone: TriNoticeTone = TriNoticeTone.Info,
    icon: TriIcon? = null
) {
    val colors = Triangulacion.colors
    val (background, content, defaultIcon) = when (tone) {
        TriNoticeTone.Info -> Triple(colors.surface300, colors.ink, TriIcon.Info)
        TriNoticeTone.Ok -> Triple(colors.successSoft, colors.accentText, TriIcon.Check)
        TriNoticeTone.Warn -> Triple(colors.warningSoft, colors.warning, TriIcon.Clock)
        TriNoticeTone.Danger -> Triple(colors.dangerSoft, colors.danger, TriIcon.Alert)
    }
    Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = modifier
            .fillMaxWidth()
            .background(background, RoundedCornerShape(Radius.md))
            .padding(horizontal = Space.s4, vertical = 14.dp)
    ) {
        TriIconView(icon = icon ?: defaultIcon, contentDescription = null, size = 20.dp, tint = content)
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            if (title != null) Text(text = title, style = Triangulacion.type.label.copy(fontSize = Triangulacion.type.body.fontSize), color = content)
            if (body != null) Text(text = body, style = Triangulacion.type.body, color = if (tone == TriNoticeTone.Info) colors.inkMuted else content)
        }
    }
}

/** Section title with an optional action on the right («Tus clubs · Ver todos»). */
@Composable
fun TriSectionTitle(
    text: String,
    modifier: Modifier = Modifier,
    actionText: String? = null,
    onAction: (() -> Unit)? = null
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier.fillMaxWidth()
    ) {
        Text(text = text, style = Triangulacion.type.displayMd, color = Triangulacion.colors.ink, modifier = Modifier.weight(1f))
        if (actionText != null && onAction != null) {
            TriTextButton(text = actionText, onClick = onAction)
        }
    }
}

/** Caps label above a data block («PRÓXIMO PARTIDO», «DÍA DEL PARTIDO»). */
@Composable
fun TriCaps(text: String, modifier: Modifier = Modifier, color: Color = Triangulacion.colors.inkMuted) {
    Text(text = text.uppercase(), style = Triangulacion.type.labelCaps, color = color, modifier = modifier)
}

/** Settings / profile row: icon, text, chevron. */
@Composable
fun TriMenuRow(
    text: String,
    icon: TriIcon,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    destructive: Boolean = false,
    supportingText: String? = null,
    trailing: (@Composable () -> Unit)? = null
) {
    val colors = Triangulacion.colors
    val content = if (destructive) colors.danger else colors.ink
    TriCard(modifier = modifier.fillMaxWidth(), onClick = onClick, contentPadding = 0.dp) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.heightIn(min = 56.dp).padding(horizontal = Space.s4, vertical = 12.dp)
        ) {
            TriIconView(icon = icon, contentDescription = null, size = 22.dp, tint = content)
            Spacer(Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = text, style = Triangulacion.type.label.copy(fontSize = Triangulacion.type.body.fontSize), color = content)
                if (supportingText != null) Text(text = supportingText, style = Triangulacion.type.bodySm, color = colors.inkMuted)
            }
            if (trailing != null) trailing() else TriIconView(icon = TriIcon.ChevR, contentDescription = null, size = 20.dp, tint = colors.inkMuted)
        }
    }
}

/** Dashed divider used by the ticket perforation and the timeline axis. */
@Composable
fun TriDashedLine(modifier: Modifier = Modifier, color: Color = Triangulacion.colors.line, vertical: Boolean = false) {
    Canvas(modifier = modifier) {
        val stroke = 1.5.dp.toPx()
        val effect = PathEffect.dashPathEffect(floatArrayOf(8f, 6f))
        if (vertical) drawLine(color, start = center.copy(y = 0f), end = center.copy(y = size.height), strokeWidth = stroke, pathEffect = effect)
        else drawLine(color, start = center.copy(x = 0f), end = center.copy(x = size.width), strokeWidth = stroke, pathEffect = effect)
    }
}

@Preview
@Composable
private fun TriSurfacesPreview() {
    SquadfyTheme {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(16.dp)) {
            TriTabs(items = listOf("Partido", "Equipos", "Tabla", "Miembros"), selectedIndex = 1, onSelect = {})
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                TriTag("Owner"); TriTag("Por cerrar", tone = TriTagTone.Warn); TriTag("Prov.", tone = TriTagTone.Dashed)
            }
            TriNotice(title = "Equipos publicados", body = "Juegas en el Equipo A.", tone = TriNoticeTone.Ok)
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                TriChip("Rating", selected = true, onClick = {}, icon = TriIcon.Trophy); TriChip("Goles", selected = false, onClick = {})
            }
            TriMenuRow(text = "Cerrar sesión", icon = TriIcon.Logout, onClick = {})
        }
    }
}

@Preview
@Composable
private fun TriSurfacesDarkPreview() {
    SquadfyTheme(darkTheme = true) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.background(Triangulacion.colors.surface100).padding(16.dp)) {
            TriTabs(items = listOf("Partido", "Equipos", "Tabla"), selectedIndex = 0, onSelect = {})
            TriNotice(title = "Tienes una ausencia ese día", body = "Te desapuntamos.", tone = TriNoticeTone.Warn)
            TriCard { Text("Card", color = Triangulacion.colors.ink) }
            Spacer(Modifier.size(4.dp))
        }
    }
}
