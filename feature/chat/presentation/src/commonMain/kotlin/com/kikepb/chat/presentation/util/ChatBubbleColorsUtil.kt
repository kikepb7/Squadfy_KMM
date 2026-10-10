package com.kikepb.chat.presentation.util

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.kikepb.core.designsystem.triangulacion.theme.Triangulacion

/** Spec 018: other people's messages sit on surface-200 (the per-user palette of the chat template is gone). */
@Composable
fun getChatBubbleColorForUser(@Suppress("UNUSED_PARAMETER") userId: String): Color = Triangulacion.colors.surface200
