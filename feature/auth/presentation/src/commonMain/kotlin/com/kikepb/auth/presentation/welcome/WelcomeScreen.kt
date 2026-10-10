package com.kikepb.auth.presentation.welcome

import com.kikepb.core.designsystem.triangulacion.preview.ScreenPreview
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.kikepb.core.designsystem.components.brand.SquadfyBrandLockup
import com.kikepb.core.designsystem.components.systembars.StatusBarIconsEffect
import com.kikepb.core.designsystem.theme.SquadfyTheme
import com.kikepb.core.designsystem.triangulacion.components.TriButton
import com.kikepb.core.designsystem.triangulacion.components.TriButtonVariant
import com.kikepb.core.designsystem.triangulacion.components.TriTextButton
import com.kikepb.core.designsystem.triangulacion.components.TriangulationArt
import com.kikepb.core.designsystem.triangulacion.theme.Space
import com.kikepb.core.designsystem.triangulacion.theme.Triangulacion
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import squadfy_app.feature.auth.presentation.generated.resources.Res
import squadfy_app.feature.auth.presentation.generated.resources.already_have_account
import squadfy_app.feature.auth.presentation.generated.resources.create_account
import squadfy_app.feature.auth.presentation.generated.resources.welcome_body
import squadfy_app.feature.auth.presentation.generated.resources.welcome_headline
import squadfy_app.feature.auth.presentation.generated.resources.welcome_headline_accent

/**
 * Welcome (spec 018, screen 01-bienvenida): the inverse surface in both themes, the lockup, a pass triangle and the
 * promise of the product. «Crear cuenta» is the Volt action; «Ya tengo cuenta» goes to the login.
 */
@Composable
fun WelcomeScreen(
    onCreateAccountClick: () -> Unit,
    onLoginClick: () -> Unit
) {
    val colors = Triangulacion.colors
    StatusBarIconsEffect(lightIcons = true)
    Box(
        contentAlignment = Alignment.TopCenter,
        modifier = Modifier.fillMaxSize().background(colors.surfaceInverse)
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 520.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .padding(horizontal = Space.s5, vertical = Space.s4)
        ) {
            SquadfyBrandLockup(onDarkBackground = true, markSize = 48.dp)
            TriangulationArt(modifier = Modifier.fillMaxWidth().height(260.dp).padding(vertical = Space.s6))
            Spacer(Modifier.weight(1f))
            Text(
                text = buildAnnotatedString {
                    append(stringResource(Res.string.welcome_headline))
                    append(" ")
                    withStyle(SpanStyle(color = colors.volt)) { append(stringResource(Res.string.welcome_headline_accent)) }
                },
                style = Triangulacion.type.displayXl,
                color = colors.onInverse,
                modifier = Modifier.semantics { heading() }
            )
            Spacer(Modifier.height(Space.s4))
            Text(
                text = stringResource(Res.string.welcome_body),
                style = Triangulacion.type.bodyLg,
                color = colors.onInverseMuted
            )
            Spacer(Modifier.height(Space.s8))
            TriButton(
                text = stringResource(Res.string.create_account),
                onClick = onCreateAccountClick,
                variant = TriButtonVariant.Volt,
                large = true,
                modifier = Modifier.fillMaxWidth()
            )
            TriTextButton(
                text = stringResource(Res.string.already_have_account),
                onClick = onLoginClick,
                color = colors.onInverse,
                modifier = Modifier.fillMaxWidth().padding(top = Space.s2)
            )
        }
    }
}

// Previews (spec 018): Tiza and Noche with the default state, no sample data

@Preview(name = "01 Bienvenida · Tiza", widthDp = 393, heightDp = 852)
@Composable
private fun WelcomeTizaPreview() = ScreenPreview { WelcomeScreen(onCreateAccountClick = {}, onLoginClick = {}) }

@Preview(name = "01 Bienvenida · Noche", widthDp = 393, heightDp = 852)
@Composable
private fun WelcomeNochePreview() = ScreenPreview(darkTheme = true) { WelcomeScreen(onCreateAccountClick = {}, onLoginClick = {}) }
