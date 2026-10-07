package com.kikepb.club.presentation.match

import com.kikepb.club.domain.model.MatchStatus
import org.jetbrains.compose.resources.StringResource
import squadfy_app.feature.club.presentation.generated.resources.Res
import squadfy_app.feature.club.presentation.generated.resources.match_status_cancelled
import squadfy_app.feature.club.presentation.generated.resources.match_status_completed
import squadfy_app.feature.club.presentation.generated.resources.match_status_in_progress
import squadfy_app.feature.club.presentation.generated.resources.match_status_scheduled
import squadfy_app.feature.club.presentation.generated.resources.match_status_unknown

val MatchStatus.label: StringResource
    get() = when (this) {
        MatchStatus.SCHEDULED -> Res.string.match_status_scheduled
        MatchStatus.IN_PROGRESS -> Res.string.match_status_in_progress
        MatchStatus.COMPLETED -> Res.string.match_status_completed
        MatchStatus.CANCELLED -> Res.string.match_status_cancelled
        MatchStatus.UNKNOWN -> Res.string.match_status_unknown
    }
