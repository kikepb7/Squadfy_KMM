package com.kikepb.club.presentation.detail.model

data class ClubDetailMatchModel(
    val id: String,
    val matchday: String?,
    val date: String,
    val firstTeamName: String,
    val firstTeamCode: String,
    val firstTeamScore: Int,
    val secondTeamName: String,
    val secondTeamCode: String,
    val secondTeamScore: Int,
    val venue: String?,
    val isFinished: Boolean
)
