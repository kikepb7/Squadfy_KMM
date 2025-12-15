package com.kikepb.chat.presentation.chat_detail.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.kikepb.chat.domain.models.ConnectionStateModel
import com.kikepb.chat.presentation.mappers.toUiText
import com.kikepb.core.designsystem.components.buttons.SquadfyButton
import com.kikepb.core.designsystem.components.textfields.SquadfyMultiLineTextField
import com.kikepb.core.designsystem.theme.SquadfyTheme
import com.kikepb.core.designsystem.theme.extended
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.resources.vectorResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import squadfy_app.feature.chat.presentation.generated.resources.Res.string as RString
import squadfy_app.core.designsystem.generated.resources.Res.drawable as RDrawable
import squadfy_app.core.designsystem.generated.resources.cloud_off_icon
import squadfy_app.feature.chat.presentation.generated.resources.send
import squadfy_app.feature.chat.presentation.generated.resources.send_a_message

@Composable
fun MessageBox(
    messageTextFieldState: TextFieldState,
    isTextInputEnabled: Boolean,
    connectionState: ConnectionStateModel,
    onSendClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isConnected = connectionState == ConnectionStateModel.CONNECTED

    SquadfyMultiLineTextField(
        state = messageTextFieldState,
        modifier = modifier,
        placeholder = stringResource(RString.send_a_message),
        enabled = isTextInputEnabled,
        keyboardOptions = KeyboardOptions(
            imeAction = ImeAction.Send
        ),
        onKeyboardAction = onSendClick,
        bottomContent = {
            Spacer(modifier = Modifier.weight(1f))
            if (!isConnected) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = vectorResource(RDrawable.cloud_off_icon),
                        contentDescription = connectionState.toUiText().asString(),
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.extended.textDisabled
                    )
                    Text(
                        text = connectionState.toUiText().asString(),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.extended.textDisabled
                    )
                }
            }
            SquadfyButton(
                text = stringResource(RString.send),
                onClick = onSendClick,
                enabled = isConnected && isTextInputEnabled
            )
        }
    )
}

@Composable
@Preview
fun MessageBoxPreview() {
    SquadfyTheme {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp),
            contentAlignment = Alignment.BottomCenter
        ) {
            MessageBox(
                messageTextFieldState = rememberTextFieldState(),
                isTextInputEnabled = true,
                connectionState = ConnectionStateModel.CONNECTED,
                onSendClick = {},
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}