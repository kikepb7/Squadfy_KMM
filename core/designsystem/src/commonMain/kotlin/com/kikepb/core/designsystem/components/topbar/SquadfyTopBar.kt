package com.kikepb.core.designsystem.components.topbar

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.kikepb.core.designsystem.components.systembars.StatusBarIconsEffect
import com.kikepb.core.designsystem.theme.SquadfyTheme
import com.kikepb.core.designsystem.triangulacion.components.TriIcon
import com.kikepb.core.designsystem.triangulacion.components.TriRoundIconButton
import com.kikepb.core.designsystem.triangulacion.theme.Space
import com.kikepb.core.designsystem.triangulacion.theme.Triangulacion
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import squadfy_app.core.designsystem.generated.resources.squadfy_topbar_back
import squadfy_app.core.designsystem.generated.resources.squadfy_topbar_settings
import squadfy_app.core.designsystem.generated.resources.Res.string as RString

/**
 * Screen header of Triangulación (spec 018): no bar, the screen background behind the status bar, a round back
 * button and a left-aligned title (display-lg on tab roots, display-md on detail screens), actions on the right.
 */
@Composable
fun SquadfyTopBar(
    title: String = "Squadfy",
    modifier: Modifier = Modifier,
    onBackClick: (() -> Unit)? = null,
    onSettingsClick: (() -> Unit)? = null
) {
    SquadfyTopBar(
        modifier = modifier,
        title = {
            Text(
                text = title,
                style = if (onBackClick == null) Triangulacion.type.displayLg else Triangulacion.type.displayMd,
                color = Triangulacion.colors.ink,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.semantics { heading() }
            )
        },
        navigationIcon = { if (onBackClick != null) SquadfyTopBarBackButton(onClick = onBackClick) },
        actions = {
            if (onSettingsClick != null) {
                TriRoundIconButton(
                    icon = TriIcon.Sliders,
                    contentDescription = stringResource(RString.squadfy_topbar_settings),
                    onClick = onSettingsClick
                )
            }
        }
    )
}

/** Slot version (chat, club header): same insets, paddings and status bar handling. */
@Composable
fun SquadfyTopBar(
    title: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    navigationIcon: @Composable () -> Unit = {},
    actions: @Composable RowScope.() -> Unit = {}
) {
    val background = Triangulacion.colors.surface100
    StatusBarIconsEffect(background = background)
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .background(background)
            .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal))
            .heightIn(min = 72.dp)
            .padding(start = Space.s3, end = Space.s3, top = 8.dp, bottom = 8.dp)
    ) {
        navigationIcon()
        Spacer(Modifier.width(Space.s2))
        Box(modifier = Modifier.weight(1f).padding(start = 4.dp)) { title() }
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically, content = actions)
    }
}

@Composable
fun SquadfyTopBarBackButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    TriRoundIconButton(
        icon = TriIcon.ChevL,
        contentDescription = stringResource(RString.squadfy_topbar_back),
        onClick = onClick,
        filled = true,
        modifier = modifier
    )
}

/** Round header action (settings, add, edit). */
@Composable
fun SquadfyTopBarIconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: TriIcon = TriIcon.Sliders,
    contentDescription: String? = null
) {
    TriRoundIconButton(icon = icon, contentDescription = contentDescription, onClick = onClick, modifier = modifier)
}

@Preview
@Composable
private fun SquadfyTopBarHomePreview() {
    SquadfyTheme { SquadfyTopBar(title = "Chat", onSettingsClick = {}) }
}

@Preview
@Composable
private fun SquadfyTopBarDetailPreview() {
    SquadfyTheme { SquadfyTopBar(title = "Acta del partido", onBackClick = {}) }
}

@Preview
@Composable
private fun SquadfyTopBarDarkPreview() {
    SquadfyTheme(darkTheme = true) { SquadfyTopBar(title = "Horario semanal", onBackClick = {}, onSettingsClick = {}) }
}
