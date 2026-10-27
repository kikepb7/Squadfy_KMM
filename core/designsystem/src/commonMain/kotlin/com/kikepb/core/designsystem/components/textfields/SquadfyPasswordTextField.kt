package com.kikepb.core.designsystem.components.textfields

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicSecureTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.TextObfuscationMode
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.semantics.Role
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kikepb.core.designsystem.triangulacion.components.TriIcon
import com.kikepb.core.designsystem.triangulacion.components.TriIconView
import com.kikepb.core.designsystem.triangulacion.theme.Triangulacion
import com.kikepb.core.designsystem.theme.SquadfyTheme
import com.kikepb.core.designsystem.theme.extended
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.resources.vectorResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import squadfy_app.core.designsystem.generated.resources.Res
import squadfy_app.core.designsystem.generated.resources.eye_icon
import squadfy_app.core.designsystem.generated.resources.eye_off_icon
import squadfy_app.core.designsystem.generated.resources.hide_password
import squadfy_app.core.designsystem.generated.resources.min_length
import squadfy_app.core.designsystem.generated.resources.show_password
import squadfy_app.core.designsystem.generated.resources.special_character
import squadfy_app.core.designsystem.generated.resources.upper_case

@Composable
fun SquadfyPasswordTextField(
    state: TextFieldState,
    isPasswordVisible: Boolean,
    onToggleVisibilityClick: () -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String? = null,
    title: String? = null,
    supportingText: String? = null,
    isError: Boolean = false,
    enabled: Boolean = true,
    onFocusChanged: (Boolean) -> Unit = {},
    leadingIcon: TriIcon? = null
) {
    SquadfyTextFieldLayout(
        title = title,
        isError = isError,
        supportingText = supportingText,
        enabled = enabled,
        onFocusChanged = onFocusChanged,
        modifier = modifier
    ) { styleModifier, interactionSource ->
        BasicSecureTextField(
            state = state,
            modifier = styleModifier,
            enabled = enabled,
            textObfuscationMode = if(isPasswordVisible) {
                TextObfuscationMode.Visible
            } else TextObfuscationMode.Hidden,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password
            ),
            textStyle = Triangulacion.type.body.copy(fontSize = 16.sp).copy(
                color = if(enabled) {
                    MaterialTheme.colorScheme.onSurface
                } else {
                    MaterialTheme.colorScheme.extended.textPlaceholder
                }
            ),
            interactionSource = interactionSource,
            cursorBrush = SolidColor(MaterialTheme.colorScheme.onSurface),
            decorator = { innerBox ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (leadingIcon != null) {
                        TriIconView(icon = leadingIcon, contentDescription = null, size = 20.dp, tint = Triangulacion.colors.inkMuted)
                        Spacer(Modifier.width(10.dp))
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        if(state.text.isEmpty() && placeholder != null) {
                            Text(
                                text = placeholder,
                                color = Triangulacion.colors.inkSubtle,
                                style = Triangulacion.type.body.copy(fontSize = 16.sp)
                            )
                        }
                        innerBox()
                    }

                    Icon(
                        imageVector = if (isPasswordVisible) vectorResource(Res.drawable.eye_off_icon) else vectorResource(Res.drawable.eye_icon),
                        contentDescription = if (isPasswordVisible) stringResource(Res.string.hide_password) else stringResource(Res.string.show_password),
                        tint = Triangulacion.colors.inkMuted,
                        // 48 dp touch target around the 24 dp eye, announced as a button (accessibility audit 2026-10)
                        modifier = Modifier
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = ripple(
                                    bounded = false,
                                    radius = 24.dp
                                ),
                                role = Role.Button,
                                onClick = onToggleVisibilityClick
                            )
                            .padding(12.dp)
                            .size(24.dp)
                    )
                }
            }
        )
    }
}

@Composable
@Preview(showBackground = true)
fun SquadfyPasswordTextFieldEmptyPreview() {
    SquadfyTheme {
        SquadfyPasswordTextField(
            state = rememberTextFieldState(),
            isPasswordVisible = true,
            onToggleVisibilityClick = {},
            modifier = Modifier
                .width(300.dp),
            placeholder = "Password",
            title = "Password",
        )
    }
}

@Composable
@Preview(showBackground = true)
fun SquadfyPasswordTextFieldFilledPreview() {
    SquadfyTheme {
        SquadfyPasswordTextField(
            state = rememberTextFieldState("password123!A"),
            isPasswordVisible = false,
            onToggleVisibilityClick = {},
            modifier = Modifier
                .width(300.dp),
            placeholder = "Password",
            title = "Password",
        )
    }
}

@Composable
@Preview(showBackground = true)
fun SquadfyPasswordTextFieldErrorPreview() {
    SquadfyTheme {
        SquadfyPasswordTextField(
            state = rememberTextFieldState("short"),
            isPasswordVisible = true,
            onToggleVisibilityClick = {},
            modifier = Modifier
                .width(300.dp),
            placeholder = "Password",
            title = "Password",
            isError = true,
        )
    }
}