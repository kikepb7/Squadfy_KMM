package com.kikepb.club.presentation.mapper

import androidx.compose.runtime.Composable
import com.kikepb.club.domain.model.MatchEventType
import com.kikepb.club.domain.model.PlayerPosition
import com.kikepb.club.domain.model.Team
import com.kikepb.core.designsystem.triangulacion.components.TimelineEventKind
import com.kikepb.core.designsystem.triangulacion.components.TriPosition
import com.kikepb.core.designsystem.triangulacion.components.TriTeam
import org.jetbrains.compose.resources.stringResource
import squadfy_app.feature.club.presentation.generated.resources.Res
import squadfy_app.feature.club.presentation.generated.resources.club_format_players
import squadfy_app.feature.club.presentation.generated.resources.club_format_spots

/** Domain → design system (spec 018): positions get their glyph, teams their colour and letter. */
fun PlayerPosition?.toTri(): TriPosition? = when (this) {
    PlayerPosition.GOALKEEPER -> TriPosition.GK
    PlayerPosition.DEFENDER -> TriPosition.DEF
    PlayerPosition.MIDFIELDER -> TriPosition.MID
    PlayerPosition.FORWARD -> TriPosition.FWD
    null -> null
}

fun Team.toTri(): TriTeam = if (this == Team.A) TriTeam.A else TriTeam.B

fun MatchEventType.toTimelineKind(): TimelineEventKind = when (this) {
    MatchEventType.GOAL -> TimelineEventKind.GOAL
    MatchEventType.ASSIST -> TimelineEventKind.ASSIST
    MatchEventType.YELLOW_CARD -> TimelineEventKind.YELLOW_CARD
    MatchEventType.RED_CARD -> TimelineEventKind.RED_CARD
}

/** «Fútbol 7» for the usual formats (10/14/22 places), otherwise «n plazas». */
@Composable
fun matchFormatLabel(maxPlayers: Int): String =
    if (maxPlayers in setOf(10, 14, 22)) stringResource(Res.string.club_format_players, maxPlayers / 2)
    else stringResource(Res.string.club_format_spots, maxPlayers)
