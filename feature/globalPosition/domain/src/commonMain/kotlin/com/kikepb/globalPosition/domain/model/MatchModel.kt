package com.kikepb.globalPosition.domain.model

import kotlin.time.Instant

data class MatchModel(
    val id: String,
    val clubId: String,
    val scheduledAt: Instant,
    val status: MatchStatus,
    val enrolledPlayers: List<String>,
    val teamA: List<String>,
    val teamB: List<String>,
    val teamAScore: Int,
    val teamBScore: Int,
    val goals: List<MatchEvent>,
    val assists: List<MatchEvent>,
    val yellowCards: List<MatchEvent>,
    val redCards: List<MatchEvent>
)

enum class MatchStatus { SCHEDULED, IN_PROGRESS, FINISHED }
