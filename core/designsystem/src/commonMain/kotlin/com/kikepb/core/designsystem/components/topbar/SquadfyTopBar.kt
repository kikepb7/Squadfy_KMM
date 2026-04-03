package com.kikepb.core.designsystem.components.topbar

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight.Companion.Bold
import com.kikepb.core.designsystem.theme.SquadfyTheme
import com.kikepb.core.designsystem.theme.extended
import org.jetbrains.compose.ui.tooling.preview.Preview

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SquadfyTopBar(
    modifier: Modifier = Modifier,
    navigationIcon: @Composable () -> Unit = {},
    actions: @Composable RowScope.() -> Unit = {}
) {
    Column(modifier = modifier.fillMaxWidth()) {
        CenterAlignedTopAppBar(
            title = {
                Text(
                    text = "Squadfy",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = Bold),
                    color = MaterialTheme.colorScheme.extended.textPrimary
                )
            },
            navigationIcon = navigationIcon,
            actions = actions,
            colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        )
        HorizontalDivider(color = MaterialTheme.colorScheme.extended.surfaceOutline)
    }
}

@Preview
@Composable
private fun SquadfyTopBarPreview() {
    SquadfyTheme { SquadfyTopBar() }
}
