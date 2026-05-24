package com.kikepb.globalPosition.data.dto

import kotlinx.serialization.Serializable
import kotlin.time.Instant

typealias MatchId = String
typealias ClubMemberId = String

@Serializable
data class MatchDto(
    val id: MatchId,
    val clubId: ClubId,
    val scheduledAt: Instant,
    val status: MatchStatusDto,
    val enrolledPlayers: List<ClubMemberId>,
    val teamA: List<ClubMemberId>,
    val teamB: List<ClubMemberId>,
    val teamAScore: Int,
    val teamBScore: Int,
    val goals: List<MatchEventDto>,
    val assists: List<MatchEventDto>,
    val yellowCards: List<MatchEventDto>,
    val redCards: List<MatchEventDto>,
    val createdAt: Instant,
    val updatedAt: Instant
)
