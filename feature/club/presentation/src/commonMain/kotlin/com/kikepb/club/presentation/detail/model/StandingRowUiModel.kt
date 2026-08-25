package com.kikepb.club.presentation.detail.model

data class StandingRowUiModel(
    val memberId: String,
    val shirtNumber: String,
    val playerName: String,
    val rating: String,
    val played: Int,
    val goals: Int,
    val minutes: Int,
    val yellow: Int,
    val red: Int
)
