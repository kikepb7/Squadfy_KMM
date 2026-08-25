package com.kikepb.club.presentation.utils

import com.kikepb.club.domain.model.ClubMemberModel
import com.kikepb.club.presentation.detail.model.StandingRowUiModel

/**
 * A per-player performance index (goal contributions and discipline, normalized by matches
 * played). This intentionally does NOT attempt to derive team wins/draws/losses from a single
 * player's own stats - that would conflate personal output with match outcomes. Real W/D/L per
 * player requires aggregating completed [com.kikepb.club.domain.model.ClubMatchModel] results
 * (teamAScore/teamBScore) across the matches they played in, which isn't wired into this screen
 * yet.
 */
fun ClubMemberModel.toStandingRow(): StandingRowUiModel {
    val rawRating = if (matchesPlayed == 0) 0.0 else
        ((goalsScored * 4.0) + (assists * 3.0) + (minutesPlayed / 90.0) -
                (yellowCards * 0.5) - (redCards * 1.0)) / matchesPlayed
    val ratingRounded = (rawRating * 10.0).toInt() / 10.0

    return StandingRowUiModel(
        memberId = id,
        shirtNumber = shirtNumber?.toString() ?: "-",
        playerName = username,
        rating = ratingRounded.toString(),
        played = matchesPlayed,
        goals = goalsScored,
        minutes = minutesPlayed,
        yellow = yellowCards,
        red = redCards
    )
}

fun initialsOf(name: String): String =
    name.split(" ").filter { it.isNotBlank() }.take(2)
        .joinToString("") { it.first().uppercase() }
        .ifBlank { "CL" }
