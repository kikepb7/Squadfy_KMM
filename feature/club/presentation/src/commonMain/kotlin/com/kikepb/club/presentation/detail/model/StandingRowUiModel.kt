package com.kikepb.club.presentation.detail.model

data class StandingRowUiModel(
    val memberId: String,
    val shirtNumber: String,
    val playerName: String,
    val rating: String,
    val played: Int,
    val wins: Int,
    val draws: Int,
    val losses: Int,
    val goals: Int,
    val minutes: Int,
    val yellow: Int,
    val red: Int,
    val points: Int
)
