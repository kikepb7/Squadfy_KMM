package com.kikepb.club.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.kikepb.core.designsystem.triangulacion.components.TriCard
import com.kikepb.core.designsystem.triangulacion.theme.Triangulacion
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kikepb.core.designsystem.theme.extended

/** Card of the club screens on Triangulación (spec 018): surface-200, radius-md, shadow-1 in Tiza. */
@Composable
internal fun SectionCard(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    TriCard(modifier = modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) { content() }
    }
}

@Composable
internal fun SectionTitle(text: String, modifier: Modifier = Modifier) {
    Text(text = text, style = Triangulacion.type.title, color = Triangulacion.colors.ink, modifier = modifier)
}

@Composable
internal fun HintText(text: String, modifier: Modifier = Modifier) {
    Text(text = text, style = Triangulacion.type.bodySm, color = Triangulacion.colors.inkMuted, modifier = modifier)
}
