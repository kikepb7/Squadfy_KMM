package com.kikepb.core.designsystem.components.buttons

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
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
import androidx.compose.ui.unit.dp
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
    val defaultBorderStroke = BorderStroke(
        width = 1.dp,
        color = MaterialTheme.colorScheme.extended.disabledOutline
    )

    val border = when {
        style == PRIMARY && !enabled -> defaultBorderStroke
        style == SECONDARY -> defaultBorderStroke
        style == DESTRUCTIVE_PRIMARY && !enabled -> defaultBorderStroke
        style == DESTRUCTIVE_SECONDARY -> {
            val borderColor = if(enabled) {
                MaterialTheme.colorScheme.extended.destructiveSecondaryOutline
            } else {
                MaterialTheme.colorScheme.extended.disabledOutline
            }
            BorderStroke(
                width = 1.dp,
                color = borderColor
            )
        }
        else -> null
    }

    Button(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        shape = RoundedCornerShape(8.dp),
        colors = squadfyButtonColors(style = style),
        border = border
    ) {
        Box(
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(
                modifier = Modifier
                    .size(15.dp)
                    .alpha(if (isLoading) 1f else 0f),
                strokeWidth = 1.5.dp,
                color = Color.Black
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
                    style = MaterialTheme.typography.titleSmall
                )
            }
        }
    }
}

@Composable
private fun squadfyButtonColors(style: SquadfyButtonStyle) =
    when (style) {
        PRIMARY -> ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            disabledContainerColor = MaterialTheme.colorScheme.extended.disabledFill,
            disabledContentColor = MaterialTheme.colorScheme.extended.textDisabled
        )
        DESTRUCTIVE_PRIMARY -> ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.error,
            contentColor = MaterialTheme.colorScheme.onError,
            disabledContainerColor = MaterialTheme.colorScheme.extended.disabledFill,
            disabledContentColor = MaterialTheme.colorScheme.extended.textDisabled
        )
        SECONDARY -> ButtonDefaults.buttonColors(
            containerColor = Color.Transparent,
            contentColor = MaterialTheme.colorScheme.extended.textSecondary,
            disabledContainerColor = Color.Transparent,
            disabledContentColor = MaterialTheme.colorScheme.extended.textDisabled
        )
        DESTRUCTIVE_SECONDARY -> ButtonDefaults.buttonColors(
            containerColor = Color.Transparent,
            contentColor = MaterialTheme.colorScheme.error,
            disabledContainerColor = Color.Transparent,
            disabledContentColor = MaterialTheme.colorScheme.extended.textDisabled
        )
        TEXT -> ButtonDefaults.buttonColors(
            containerColor = Color.Transparent,
            contentColor = MaterialTheme.colorScheme.tertiary,
            disabledContainerColor = Color.Transparent,
            disabledContentColor = MaterialTheme.colorScheme.extended.textDisabled
        )
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
