package com.kikepb.core.presentation.share

import androidx.compose.runtime.Composable

/** Returns a function that opens the platform share sheet with [text] (e.g. the club invitation code). */
@Composable
expect fun rememberShareTextLauncher(): (text: String) -> Unit
