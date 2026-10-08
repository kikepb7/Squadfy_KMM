package com.kikepb.core.designsystem.components.systembars

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance

/**
 * Spec 017: the status bar is drawn by the screen (edge to edge), so its icons must contrast with whatever the
 * screen paints behind it. Called by the components that own the top edge: [com.kikepb.core.designsystem.components.topbar.SquadfyTopBar]
 * and the auth layouts. The last screen composed wins, which is the one on top.
 */
@Composable
expect fun StatusBarIconsEffect(lightIcons: Boolean)

/** Light icons on dark backgrounds, dark icons on light ones. */
@Composable
fun StatusBarIconsEffect(background: Color) = StatusBarIconsEffect(lightIcons = background.luminance() < 0.5f)
