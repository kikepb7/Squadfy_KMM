package com.kikepb.globalPosition.presentation.model

data class MatchUiModel(
    val id: String,
    val clubId: String,
    val clubName: String? = null,
    val clubLogoUrl: String? = null,
    val firstTeamName: String,
    val firstTeamCode: String,
    val secondTeamName: String,
    val secondTeamCode: String,
    val firstTeamScore: Int,
    val secondTeamScore: Int,
    val date: String,
    val status: MatchStatusUi,
    val enrolledCount: Int,
    val matchday: String? = null
)

enum class MatchStatusUi { SCHEDULED, IN_PROGRESS, FINISHED }
