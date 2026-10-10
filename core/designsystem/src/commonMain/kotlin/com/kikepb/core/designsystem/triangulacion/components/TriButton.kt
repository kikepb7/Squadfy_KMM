package com.kikepb.core.designsystem.triangulacion.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kikepb.core.designsystem.theme.SquadfyTheme
import com.kikepb.core.designsystem.triangulacion.theme.Radius
import com.kikepb.core.designsystem.triangulacion.theme.Triangulacion
import org.jetbrains.compose.ui.tooling.preview.Preview

enum class TriButtonVariant { Primary, Volt, Secondary, Danger }

/**
 * Button with the Corte (spec 018). Primary = brand, Volt = the one standout action of the screen (sign up),
 * Secondary = outlined, Danger = destructive. Minimum height 48 dp (56 dp when [large]). [glow] adds the Volt
 * shadow, only for the active sign-up button.
 */
@Composable
fun TriButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    variant: TriButtonVariant = TriButtonVariant.Primary,
    icon: TriIcon? = null,
    enabled: Boolean = true,
    isLoading: Boolean = false,
    large: Boolean = false,
    glow: Boolean = false
) {
    val colors = Triangulacion.colors
    val active = enabled && !isLoading
    val (background, content) = when {
        !enabled -> colors.surface300 to colors.inkSubtle
        variant == TriButtonVariant.Primary -> colors.brand to colors.onBrand
        variant == TriButtonVariant.Volt -> colors.volt to colors.onVolt
        variant == TriButtonVariant.Danger -> colors.dangerSoft to colors.danger
        else -> colors.surface200 to colors.ink
    }
    val shape = when (variant) {
        TriButtonVariant.Secondary, TriButtonVariant.Danger -> RoundedCornerShape(Radius.md)
        else -> if (large) CutMd else CutSm
    }
    val glowModifier = if (glow && enabled && !colors.isDark) {
        Modifier.shadow(elevation = 10.dp, shape = shape, ambientColor = colors.volt, spotColor = colors.volt)
    } else Modifier
    Surface(
        color = background,
        contentColor = content,
        shape = shape,
        border = if (variant == TriButtonVariant.Secondary && enabled) BorderStroke(1.5.dp, colors.lineStrong) else null,
        modifier = modifier
            .then(glowModifier)
            .heightIn(min = if (large) 56.dp else 48.dp)
            .clickable(enabled = active, role = Role.Button, onClick = onClick)
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.alpha(if (isLoading) 0f else 1f)
            ) {
                if (icon != null) TriIconView(icon = icon, contentDescription = null, size = 20.dp, tint = content)
                Text(
                    text = text,
                    style = Triangulacion.type.label.copy(fontSize = if (large) 17.sp else 15.sp),
                    color = content,
                    textAlign = TextAlign.Center
                )
            }
            if (isLoading) CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp, color = content)
        }
    }
}

/** Text action: «Desapuntarme», «Ya tengo cuenta», «Ver todos». Keeps the 48 dp touch target. */
@Composable
fun TriTextButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    color: Color = Triangulacion.colors.accentText,
    underline: Boolean = false,
    enabled: Boolean = true
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .heightIn(min = 48.dp)
            .clickable(enabled = enabled, role = Role.Button, onClick = onClick)
            .padding(horizontal = 12.dp)
    ) {
        Text(
            text = text,
            style = Triangulacion.type.label.copy(
                fontSize = 15.sp,
                textDecoration = if (underline) TextDecoration.Underline else null
            ),
            color = if (enabled) color else Triangulacion.colors.inkSubtle
        )
    }
}

/** Round icon action of the headers (back, settings, add): 44 dp visual, 48 dp touch target. */
@Composable
fun TriRoundIconButton(
    icon: TriIcon,
    contentDescription: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    filled: Boolean = false
) {
    val colors = Triangulacion.colors
    Surface(
        shape = RoundedCornerShape(50),
        color = if (filled) colors.surface300 else colors.surface200,
        shadowElevation = if (filled || colors.isDark) 0.dp else 1.dp,
        border = if (colors.isDark && !filled) BorderStroke(1.dp, colors.line) else null,
        modifier = modifier
            .padding(2.dp)
            .size(44.dp)
            .clickable(role = Role.Button, onClick = onClick)
    ) {
        Box(contentAlignment = Alignment.Center) {
            TriIconView(icon = icon, contentDescription = contentDescription, size = 22.dp, tint = colors.ink)
        }
    }
}

@Preview
@Composable
private fun TriButtonPreview() {
    SquadfyTheme {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            TriButton(text = "Apuntarme", onClick = {}, variant = TriButtonVariant.Volt, icon = TriIcon.Plus, glow = true)
            TriButton(text = "Iniciar sesión", onClick = {})
            TriButton(text = "Cambiar foto", onClick = {}, variant = TriButtonVariant.Secondary)
        }
    }
}

@Preview
@Composable
private fun TriButtonDarkPreview() {
    SquadfyTheme(darkTheme = true) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.border(0.dp, Color.Transparent)) {
            TriButton(text = "Apuntarme", onClick = {}, variant = TriButtonVariant.Volt, icon = TriIcon.Plus, large = true)
            TriButton(text = "Guardar", onClick = {}, isLoading = true)
        }
    }
}
