package com.kikepb.core.designsystem.components.layouts

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.kikepb.core.designsystem.components.buttons.SquadfyButton
import com.kikepb.core.designsystem.components.buttons.SquadfyButtonStyle
import com.kikepb.core.designsystem.components.icons.SquadfySuccessIcon
import com.kikepb.core.designsystem.theme.SquadfyTheme
import com.kikepb.core.designsystem.theme.extended
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun SquadfySimpleSuccessLayout(
    title: String,
    description: String,
    secondaryError: String? = null,
    icon: @Composable () -> Unit,
    primaryButton: @Composable () -> Unit,
    secondaryButton: @Composable (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        icon()
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .offset(y = -(25).dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.extended.textPrimary,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.extended.textSecondary,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(24.dp))

            primaryButton()

            if(secondaryButton != null) {
                Spacer(modifier = Modifier.height(8.dp))
                secondaryButton()
                if (secondaryError != null) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = secondaryError,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
@Preview
fun SquadfySimpleSuccessLayoutPreview() {
    SquadfyTheme(darkTheme = true) {
        SquadfySimpleSuccessLayout(
            title = "Hello world!",
            description = "Test description",
            icon = {
                SquadfySuccessIcon()
            },
            primaryButton = {
                SquadfyButton(
                    text = "Log In",
                    onClick = {},
                    modifier = Modifier.fillMaxWidth()
                )
            },
            secondaryButton = {
                SquadfyButton(
                    text = "Resend verification email",
                    onClick = {},
                    style = SquadfyButtonStyle.SECONDARY,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        )
    }
}