package com.kikepb.globalPosition.domain.model

data class MatchModel(
    val id: String,
    val homeTeamName: String,
    val homeTeamLogoUrl: String?,
    val awayTeamName: String,
    val awayTeamLogoUrl: String?,
    val homeScore: Int?,
    val awayScore: Int?,
    val date: String,
    val competition: String,
    val status: MatchStatus
)

enum class MatchStatus { SCHEDULED, LIVE, FINISHED }
