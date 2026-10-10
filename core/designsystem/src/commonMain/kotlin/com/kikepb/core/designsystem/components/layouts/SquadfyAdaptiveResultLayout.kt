package com.kikepb.core.designsystem.components.layouts

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.ui.unit.dp
import com.kikepb.core.designsystem.components.brand.SquadfyBrandLockup
import com.kikepb.core.designsystem.components.systembars.StatusBarIconsEffect
import com.kikepb.core.designsystem.theme.SquadfyTheme
import com.kikepb.core.designsystem.triangulacion.theme.Space
import com.kikepb.core.designsystem.triangulacion.theme.Triangulacion
import org.jetbrains.compose.ui.tooling.preview.Preview

/** Result screens of the auth flow (account created, email verified): lockup on top, the result centred. */
@Composable
fun SquadfyAdaptiveResultLayout(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    val colors = Triangulacion.colors
    StatusBarIconsEffect(background = colors.surface100)
    Box(
        contentAlignment = Alignment.TopCenter,
        modifier = modifier.fillMaxSize().background(colors.surface100)
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 480.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .windowInsetsPadding(WindowInsets.navigationBars.only(WindowInsetsSides.Bottom))
                .padding(horizontal = Space.s5, vertical = Space.s4),
            verticalArrangement = Arrangement.spacedBy(Space.s6)
        ) {
            SquadfyBrandLockup()
            Column(
                modifier = Modifier.fillMaxWidth().padding(top = Space.s12),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(Space.s6),
                content = content
            )
        }
    }
}

@Composable
@Preview
fun SquadfyAdaptiveResultLayoutPreview() {
    SquadfyTheme {
        SquadfyAdaptiveResultLayout(modifier = Modifier.fillMaxSize()) {
            Text(text = "Cuenta creada", style = Triangulacion.type.displayLg, color = Triangulacion.colors.ink)
        }
    }
}
