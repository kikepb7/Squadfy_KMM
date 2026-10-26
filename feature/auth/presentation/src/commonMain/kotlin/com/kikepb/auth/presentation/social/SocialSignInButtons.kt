package com.kikepb.auth.presentation.social

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.background
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.kikepb.core.designsystem.triangulacion.components.TriButton
import com.kikepb.core.designsystem.triangulacion.components.TriButtonVariant
import com.kikepb.core.designsystem.triangulacion.theme.Space
import com.kikepb.core.designsystem.triangulacion.theme.Triangulacion
import com.kikepb.core.domain.auth.model.SignInMethod
import org.jetbrains.compose.resources.stringResource
import squadfy_app.feature.auth.presentation.generated.resources.Res
import squadfy_app.feature.auth.presentation.generated.resources.continue_with_apple
import squadfy_app.feature.auth.presentation.generated.resources.continue_with_google
import squadfy_app.feature.auth.presentation.generated.resources.social_divider

/** "or continue with" and one button per provider the platform offers; nothing when it offers none. */
@Composable
fun SocialSignInButtons(
    state: SocialSignInState,
    onAction: (SocialSignInAction) -> Unit,
    onDarkBackground: Boolean = false,
    modifier: Modifier = Modifier
) {
    if (state.methods.isEmpty()) return
    val colors = Triangulacion.colors
    val lineColor = if (onDarkBackground) colors.onInverseMuted else colors.line
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(Space.s3)) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            Box(Modifier.weight(1f).height(1.dp).background(lineColor))
            Text(
                text = stringResource(Res.string.social_divider),
                style = Triangulacion.type.bodySm,
                color = if (onDarkBackground) colors.onInverseMuted else colors.inkMuted,
                modifier = Modifier.padding(horizontal = Space.s3)
            )
            Box(Modifier.weight(1f).height(1.dp).background(lineColor))
        }
        state.methods.forEach { method ->
            TriButton(
                text = stringResource(
                    when (method) {
                        SignInMethod.GOOGLE -> Res.string.continue_with_google
                        SignInMethod.APPLE -> Res.string.continue_with_apple
                    }
                ),
                onClick = { onAction(SocialSignInAction.OnMethodClick(method)) },
                variant = if (onDarkBackground) TriButtonVariant.Primary else TriButtonVariant.Secondary,
                enabled = state.inProgress == null,
                isLoading = state.inProgress == method,
                modifier = Modifier.fillMaxWidth()
            )
        }
        state.error?.let { error ->
            Text(
                text = error.asString(),
                style = Triangulacion.type.bodySm,
                color = if (onDarkBackground) colors.onInverse else colors.danger,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
