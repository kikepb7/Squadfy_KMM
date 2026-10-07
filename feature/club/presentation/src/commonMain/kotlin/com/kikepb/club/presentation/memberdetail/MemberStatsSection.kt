package com.kikepb.club.presentation.memberdetail

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kikepb.club.presentation.components.SectionCard
import com.kikepb.club.presentation.components.SectionTitle
import com.kikepb.core.designsystem.theme.extended
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import squadfy_app.feature.club.presentation.generated.resources.Res
import squadfy_app.feature.club.presentation.generated.resources.member_rating
import squadfy_app.feature.club.presentation.generated.resources.member_recent_changes
import squadfy_app.feature.club.presentation.generated.resources.member_stats_cards
import squadfy_app.feature.club.presentation.generated.resources.member_stats_goals
import squadfy_app.feature.club.presentation.generated.resources.member_stats_matches
import squadfy_app.feature.club.presentation.generated.resources.member_stats_title

@Composable
fun MemberStatsSection(viewModel: MemberStatsViewModel = koinViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val stats = state.stats
    if (stats == null && state.rating == null) return
    SectionCard {
        SectionTitle(text = stringResource(Res.string.member_stats_title))
        if (state.rating != null && state.rank != null) {
            Text(
                text = stringResource(Res.string.member_rating, state.rating!!, state.rank!!),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }
        stats?.let {
            Line(stringResource(Res.string.member_stats_matches, it.matchesPlayed, it.wins, it.draws, it.losses))
            Line(stringResource(Res.string.member_stats_goals, it.goals, it.assists))
            Line(stringResource(Res.string.member_stats_cards, it.yellowCards, it.redCards, it.minutesPlayed))
        }
        if (state.recentChanges.isNotEmpty()) {
            // AC-008-08: "+12 · −8 · +3", newest first
            Line(stringResource(Res.string.member_recent_changes, state.recentChanges.joinToString(" · ") { if (it.change > 0) "+${it.change}" else "${it.change}" }))
        }
    }
}

@Composable
private fun Line(text: String) {
    Text(text = text, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.extended.textSecondary)
}
