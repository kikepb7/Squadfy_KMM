package com.kikepb.core.designsystem.components.textfields

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.Alignment
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.width
import com.kikepb.core.designsystem.triangulacion.components.TriIcon
import com.kikepb.core.designsystem.triangulacion.components.TriIconView
import com.kikepb.core.designsystem.triangulacion.theme.Radius
import com.kikepb.core.designsystem.triangulacion.theme.Triangulacion
import com.kikepb.core.designsystem.theme.extended

@Composable
fun SquadfyTextFieldLayout(
    title: String? = null,
    isError: Boolean = false,
    supportingText: String? = null,
    supportingConditions: List<Pair<String, Boolean>>? = null,
    enabled: Boolean = true,
    onFocusChanged: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    textField: @Composable (Modifier, MutableInteractionSource) -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()
    val colors = Triangulacion.colors

    LaunchedEffect(isFocused) { onFocusChanged(isFocused) }

    // Spec 018 (Campo): radius-sm, line-strong border, 2 dp focus ring, error with icon and word (never colour alone)
    val shape = RoundedCornerShape(Radius.sm)
    val textFieldStyleModifier = Modifier
        .fillMaxWidth()
        .heightIn(min = 52.dp)
        .background(color = if (enabled) colors.surface200 else colors.surface300, shape = shape)
        .border(
            width = if (isFocused || isError) 2.dp else 1.5.dp,
            color = when {
                isError -> colors.danger
                isFocused -> colors.focusRing
                else -> colors.lineStrong
            },
            shape = shape
        )
        .padding(horizontal = 14.dp, vertical = 14.dp)

    Column(modifier = modifier) {
        if (title != null) {
            Text(
                text = title.uppercase(),
                style = Triangulacion.type.labelCaps,
                color = colors.inkMuted
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        textField(textFieldStyleModifier, interactionSource)

        if (!supportingConditions.isNullOrEmpty()) {
            Spacer(modifier = Modifier.height(6.dp))
            supportingConditions.forEach { (label, ok) ->
                Row(modifier = Modifier.padding(top = 2.dp), verticalAlignment = Alignment.CenterVertically) {
                    TriIconView(
                        icon = if (ok) TriIcon.Check else TriIcon.X,
                        contentDescription = null,
                        size = 16.dp,
                        tint = if (ok) colors.accentText else colors.danger
                    )
                    Text(
                        text = label,
                        color = if (ok) colors.inkMuted else colors.danger,
                        style = Triangulacion.type.bodySm,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }
            return@Column
        }

        if (!supportingText.isNullOrEmpty()) {
            Spacer(modifier = Modifier.height(6.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (isError) {
                    TriIconView(icon = TriIcon.Alert, contentDescription = null, size = 16.dp, tint = colors.danger)
                    Spacer(Modifier.width(6.dp))
                }
                Text(
                    text = supportingText,
                    color = if (isError) colors.danger else colors.inkMuted,
                    style = Triangulacion.type.bodySm
                )
            }
        }
    }
}

