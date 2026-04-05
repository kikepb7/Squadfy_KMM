package com.kikepb.club.presentation.utils

import com.kikepb.club.domain.model.ClubMemberModel
import com.kikepb.club.presentation.detail.model.StandingRowUiModel

fun ClubMemberModel.toStandingRow(): StandingRowUiModel {
    val wins = minOf(matchesPlayed, goalsScored)
    val remaining = (matchesPlayed - wins).coerceAtLeast(0)
    val draws = minOf(remaining, assists)
    val losses = (matchesPlayed - wins - draws).coerceAtLeast(0)
    val rawRating = if (matchesPlayed == 0) 0.0 else
        ((goalsScored * 4.0) + (assists * 3.0) + (minutesPlayed / 45.0) -
                (yellowCards * 0.5) - (redCards * 1.0)) / matchesPlayed
    val ratingRounded = (rawRating * 10.0).toInt() / 10.0
    return StandingRowUiModel(
        memberId = id,
        shirtNumber = shirtNumber?.toString() ?: "-",
        playerName = username,
        rating = ratingRounded.toString(),
        played = matchesPlayed,
        wins = wins,
        draws = draws,
        losses = losses,
        goals = goalsScored,
        minutes = minutesPlayed,
        yellow = yellowCards,
        red = redCards,
        points = wins * 3 + draws
    )
}

fun initialsOf(name: String): String =
    name.split(" ").filter { it.isNotBlank() }.take(2)
        .joinToString("") { it.first().uppercase() }
        .ifBlank { "CL" }
