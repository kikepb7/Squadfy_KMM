package com.kikepb.club.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.kikepb.club.domain.model.PlayerPosition
import com.kikepb.club.presentation.mapper.label
import org.jetbrains.compose.resources.stringResource

/** Single optional selection of a [PlayerPosition]; tapping the selected chip clears it. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PositionChips(
    selected: PlayerPosition?,
    onSelected: (PlayerPosition?) -> Unit,
    modifier: Modifier = Modifier
) {
    FlowRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        PlayerPosition.entries.forEach { position ->
            FilterChip(
                selected = selected == position,
                onClick = { onSelected(if (selected == position) null else position) },
                label = { Text(text = stringResource(position.label)) }
            )
        }
    }
}
