package com.kikepb.core.designsystem.components.topbar

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight.Companion.Bold
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kikepb.core.designsystem.components.brand.SquadfyBrandLogo
import com.kikepb.core.designsystem.components.systembars.StatusBarIconsEffect
import com.kikepb.core.designsystem.theme.SquadfyTheme
import com.kikepb.core.designsystem.theme.extended
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import squadfy_app.core.designsystem.generated.resources.squadfy_topbar_back
import squadfy_app.core.designsystem.generated.resources.squadfy_topbar_settings
import squadfy_app.core.designsystem.generated.resources.Res.string as RString

/**
 * The app's top bar: brand crest (or back), centered title, settings (or an empty slot of the same size so the
 * title stays centered). Every screen uses it (spec 017).
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
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = Bold, letterSpacing = (-0.4).sp),
                color = MaterialTheme.colorScheme.extended.textPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        navigationIcon = {
            if (onBackClick != null) {
                SquadfyTopBarBackButton(onClick = onBackClick)
            } else {
                Box(
                    modifier = Modifier.size(size = TopBarButtonSize),
                    contentAlignment = Alignment.Center
                ) {
                    SquadfyBrandLogo(modifier = Modifier.size(size = 22.dp))
                }
            }
        },
        actions = {
            if (onSettingsClick != null) {
                SquadfyTopBarIconButton(onClick = onSettingsClick) {
                    Icon(
                        imageVector = Icons.Outlined.Settings,
                        contentDescription = stringResource(RString.squadfy_topbar_settings),
                        modifier = Modifier.size(size = 18.dp)
                    )
                }
            } else {
                Box(modifier = Modifier.size(size = TopBarButtonSize))
            }
        }
    )
}

/**
 * Slot version for headers with richer content (chat). Same surface, height, divider and status bar handling:
 * the bar paints behind the status bar and pads its content below it (spec 017, edge to edge).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SquadfyTopBar(
    title: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    navigationIcon: @Composable () -> Unit = {},
    actions: @Composable RowScope.() -> Unit = {}
) {
    val containerColor = MaterialTheme.colorScheme.surface
    StatusBarIconsEffect(background = containerColor)
    Column(modifier = modifier.fillMaxWidth()) {
        CenterAlignedTopAppBar(
            title = title,
            navigationIcon = navigationIcon,
            actions = actions,
            colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = containerColor),
            windowInsets = WindowInsets.safeDrawing.only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal)
        )
        HorizontalDivider(color = MaterialTheme.colorScheme.extended.surfaceOutline)
    }
}

@Composable
fun SquadfyTopBarBackButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    SquadfyTopBarIconButton(onClick = onClick, modifier = modifier) {
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = stringResource(RString.squadfy_topbar_back),
            modifier = Modifier.size(size = 18.dp)
        )
    }
}

@Composable
fun SquadfyTopBarIconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    OutlinedIconButton(
        onClick = onClick,
        // AC-011-11: 38dp visual, 48dp touch target
        modifier = modifier.minimumInteractiveComponentSize().size(size = TopBarButtonSize),
        shape = RoundedCornerShape(size = 10.dp),
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.extended.surfaceOutline
        ),
        colors = IconButtonDefaults.outlinedIconButtonColors(
            containerColor = MaterialTheme.colorScheme.extended.surfaceLower,
            contentColor = MaterialTheme.colorScheme.extended.textSecondary
        )
    ) {
        content()
    }
}

private val TopBarButtonSize = 38.dp

@Preview
@Composable
private fun SquadfyTopBarHomePreview() {
    SquadfyTheme { SquadfyTopBar(title = "Inicio", onSettingsClick = {}) }
}

@Preview
@Composable
private fun SquadfyTopBarDetailPreview() {
    SquadfyTheme { SquadfyTopBar(title = "Mi Perfil", onBackClick = {}, onSettingsClick = {}) }
}

@Preview
@Composable
private fun SquadfyTopBarDarkPreview() {
    SquadfyTheme(darkTheme = true) { SquadfyTopBar(title = "Últimos Partidos", onBackClick = {}, onSettingsClick = {}) }
}
