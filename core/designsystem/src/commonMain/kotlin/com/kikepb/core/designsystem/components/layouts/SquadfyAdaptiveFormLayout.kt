package com.kikepb.core.designsystem.components.layouts

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.unit.dp
import com.kikepb.core.designsystem.components.brand.SquadfyBrandLockup
import com.kikepb.core.designsystem.theme.SquadfyTheme
import com.kikepb.core.designsystem.triangulacion.components.TriIcon
import com.kikepb.core.designsystem.triangulacion.components.TriIconView
import com.kikepb.core.designsystem.triangulacion.theme.Space
import com.kikepb.core.designsystem.triangulacion.theme.Triangulacion
import com.kikepb.core.presentation.util.clearFocusOnTap
import org.jetbrains.compose.ui.tooling.preview.Preview

/**
 * Auth form (spec 018, screens 02-login and siblings): the Corte lockup, a display-lg title with an optional line
 * below, the error with its icon, and the form. One column on the screen background, centred and capped at 480 dp
 * on wide screens; it scrolls and pads the navigation bar.
 */
@Composable
fun SquadfyAdaptiveFormLayout(
    headerText: String,
    errorText: String? = null,
    logo: @Composable () -> Unit = { SquadfyBrandLockup() },
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    formContent: @Composable ColumnScope.() -> Unit
) {
    val colors = Triangulacion.colors
    Box(
        contentAlignment = Alignment.TopCenter,
        modifier = modifier
            .fillMaxSize()
            .background(colors.surface100)
            .clearFocusOnTap()
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 480.dp)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .windowInsetsPadding(WindowInsets.navigationBars.only(WindowInsetsSides.Bottom))
                .padding(horizontal = Space.s5, vertical = Space.s4)
        ) {
            logo()
            Spacer(modifier = Modifier.height(Space.s8))
            AuthHeaderSection(headerText = headerText, subtitle = subtitle, errorText = errorText)
            Spacer(modifier = Modifier.height(Space.s6))
            formContent()
            Spacer(modifier = Modifier.height(Space.s4))
        }
    }
}

@Composable
fun ColumnScope.AuthHeaderSection(
    headerText: String,
    subtitle: String? = null,
    errorText: String? = null
) {
    val colors = Triangulacion.colors
    Text(
        text = headerText,
        style = Triangulacion.type.displayLg,
        color = colors.ink,
        modifier = Modifier.fillMaxWidth().semantics { heading() }
    )
    if (subtitle != null) {
        Spacer(modifier = Modifier.height(Space.s2))
        Text(text = subtitle, style = Triangulacion.type.body, color = colors.inkMuted, modifier = Modifier.fillMaxWidth())
    }
    AnimatedVisibility(visible = errorText != null) {
        if (errorText != null) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth().padding(top = Space.s3)
            ) {
                TriIconView(icon = TriIcon.Alert, contentDescription = null, size = 18.dp, tint = colors.danger)
                Text(text = errorText, style = Triangulacion.type.bodySm, color = colors.danger)
            }
        }
    }
}

@Composable
@Preview
fun SquadfyAdaptiveFormLayoutLightPreview() {
    SquadfyTheme {
        SquadfyAdaptiveFormLayout(
            headerText = "Bienvenido de vuelta",
            subtitle = "Entra para ver tu próxima convocatoria.",
            errorText = "Email o contraseña incorrectos",
            formContent = { Text(text = "Formulario", color = Triangulacion.colors.ink) }
        )
    }
}

@Composable
@Preview
fun SquadfyAdaptiveFormLayoutDarkPreview() {
    SquadfyTheme(darkTheme = true) {
        SquadfyAdaptiveFormLayout(
            headerText = "Bienvenido de vuelta",
            formContent = { Text(text = "Formulario", color = Triangulacion.colors.ink) }
        )
    }
}
