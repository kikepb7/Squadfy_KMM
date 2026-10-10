package com.kikepb.core.designsystem.components.buttons

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.heightIn
import com.kikepb.core.designsystem.triangulacion.components.CutSm
import com.kikepb.core.designsystem.triangulacion.theme.Radius
import com.kikepb.core.designsystem.triangulacion.theme.Triangulacion
import com.kikepb.core.designsystem.components.buttons.SquadfyButtonStyle.DESTRUCTIVE_PRIMARY
import com.kikepb.core.designsystem.components.buttons.SquadfyButtonStyle.DESTRUCTIVE_SECONDARY
import com.kikepb.core.designsystem.components.buttons.SquadfyButtonStyle.PRIMARY
import com.kikepb.core.designsystem.components.buttons.SquadfyButtonStyle.SECONDARY
import com.kikepb.core.designsystem.components.buttons.SquadfyButtonStyle.TEXT
import com.kikepb.core.designsystem.theme.SquadfyTheme
import com.kikepb.core.designsystem.theme.extended
import org.jetbrains.compose.ui.tooling.preview.Preview

enum class SquadfyButtonStyle {
    PRIMARY,
    DESTRUCTIVE_PRIMARY,
    SECONDARY,
    DESTRUCTIVE_SECONDARY,
    TEXT
}

/**
 * The app's button on Triangulación (spec 018): PRIMARY = brand with the Corte, SECONDARY = outlined, DESTRUCTIVE_* =
 * danger, TEXT = accent text. Minimum height 48 dp. For the Volt call to action use [com.kikepb.core.designsystem.triangulacion.components.TriButton].
 */
@Composable
fun SquadfyButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    style: SquadfyButtonStyle = PRIMARY,
    enabled: Boolean = true,
    isLoading: Boolean = false,
    leadingIcon: @Composable (() -> Unit)? = null,
) {
    val colors = Triangulacion.colors
    val (container, content) = when {
        !enabled && style != TEXT -> colors.surface300 to colors.inkSubtle
        !enabled -> Color.Transparent to colors.inkSubtle
        style == PRIMARY -> colors.brand to colors.onBrand
        style == DESTRUCTIVE_PRIMARY -> colors.danger to colors.surface100
        style == SECONDARY -> colors.surface200 to colors.ink
        style == DESTRUCTIVE_SECONDARY -> colors.surface200 to colors.danger
        else -> Color.Transparent to colors.accentText
    }
    val border = when {
        !enabled -> null
        style == SECONDARY -> BorderStroke(width = 1.5.dp, color = colors.lineStrong)
        style == DESTRUCTIVE_SECONDARY -> BorderStroke(width = 1.5.dp, color = colors.danger)
        else -> null
    }
    val shape = if (style == PRIMARY) CutSm else RoundedCornerShape(Radius.md)

    Button(
        onClick = onClick,
        modifier = modifier.heightIn(min = 48.dp),
        enabled = enabled && !isLoading,
        shape = shape,
        colors = ButtonDefaults.buttonColors(
            containerColor = container,
            contentColor = content,
            disabledContainerColor = if (isLoading) container else container,
            disabledContentColor = content
        ),
        border = border,
        elevation = null
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.padding(vertical = 4.dp)
        ) {
            CircularProgressIndicator(
                modifier = Modifier
                    .size(18.dp)
                    .alpha(if (isLoading) 1f else 0f),
                strokeWidth = 2.dp,
                color = content
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(
                    8.dp,
                    Alignment.CenterHorizontally
                ),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.alpha(
                    if (isLoading) 0f else 1f
                )
            ) {
                leadingIcon?.invoke()
                Text(
                    text = text,
                    style = Triangulacion.type.label.copy(fontSize = 16.sp),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Preview
@Composable
fun SquadfyPrimaryButtonPreview() {
    SquadfyTheme {
        SquadfyButton(
            text = "Squadfy",
            onClick = {},
            style = PRIMARY
        )
    }
}

@Preview
@Composable
fun SquadfySecondaryButtonPreview() {
    SquadfyTheme {
        SquadfyButton(
            text = "Squadfy",
            onClick = {},
            style = SECONDARY
        )
    }
}

@Preview
@Composable
fun SquadfyDestructivePrimaryButtonPreview() {
    SquadfyTheme {
        SquadfyButton(
            text = "Squadfy",
            onClick = {},
            style = DESTRUCTIVE_PRIMARY
        )
    }
}

@Preview
@Composable
fun SquadfyDestructiveSecondaryButtonPreview() {
    SquadfyTheme {
        SquadfyButton(
            text = "Squadfy",
            onClick = {},
            style = DESTRUCTIVE_SECONDARY
        )
    }
}

@Preview
@Composable
fun SquadfyTextButtonPreview() {
    SquadfyTheme {
        SquadfyButton(
            text = "Squadfy",
            onClick = {},
            style = TEXT
        )
    }
}
