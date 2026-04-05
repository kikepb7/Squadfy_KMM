package com.kikepb.globalPosition.presentation.mapper

import com.kikepb.globalPosition.domain.model.MatchModel
import com.kikepb.globalPosition.domain.model.MatchStatus
import com.kikepb.globalPosition.presentation.model.MatchStatusUi
import com.kikepb.globalPosition.presentation.model.MatchUiModel

fun MatchModel.toUiModel() = MatchUiModel(
    id = id,
    homeTeamName = homeTeamName,
    homeTeamInitials = homeTeamName.toInitials(),
    homeTeamLogoUrl = homeTeamLogoUrl,
    awayTeamName = awayTeamName,
    awayTeamInitials = awayTeamName.toInitials(),
    awayTeamLogoUrl = awayTeamLogoUrl,
    homeScore = homeScore,
    awayScore = awayScore,
    date = date,
    competition = competition,
    status = status.toUiModel()
)

fun MatchStatus.toUiModel() = when (this) {
    MatchStatus.SCHEDULED -> MatchStatusUi.SCHEDULED
    MatchStatus.LIVE -> MatchStatusUi.LIVE
    MatchStatus.FINISHED -> MatchStatusUi.FINISHED
}

private fun String.toInitials(): String = split(" ")
    .take(2)
    .mapNotNull { it.firstOrNull()?.uppercaseChar() }
    .joinToString("")
    .ifEmpty { take(2).uppercase() }
