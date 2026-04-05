package com.kikepb.globalPosition.presentation.model

data class MatchUiModel(
    val id: String,
    val homeTeamName: String,
    val homeTeamInitials: String,
    val homeTeamLogoUrl: String?,
    val awayTeamName: String,
    val awayTeamInitials: String,
    val awayTeamLogoUrl: String?,
    val homeScore: Int?,
    val awayScore: Int?,
    val date: String,
    val competition: String,
    val status: MatchStatusUi
)

enum class MatchStatusUi { SCHEDULED, LIVE, FINISHED }
