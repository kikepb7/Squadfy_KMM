package com.kikepb.club.domain.repository

import com.kikepb.club.domain.error.ClubError
import com.kikepb.club.domain.model.MatchEventType
import com.kikepb.club.domain.model.MatchFormat
import com.kikepb.club.domain.model.MatchModel
import com.kikepb.club.domain.model.MatchStatus
import com.kikepb.club.domain.model.TeamBalanceModel
import com.kikepb.core.domain.util.Result
import kotlin.time.Instant

/** Matches, teams and result (specs 006/007, BE-003/004/008). Network-first (ADR-0006). */
interface MatchRepository {
    suspend fun getMatch(matchId: String): Result<MatchModel, ClubError>
    suspend fun getClubMatches(clubId: String, status: MatchStatus? = null): Result<List<MatchModel>, ClubError>
    suspend fun createExtraMatch(clubId: String, scheduledAt: Instant, format: MatchFormat?, durationMinutes: Int?): Result<MatchModel, ClubError>
    suspend fun cancel(matchId: String): Result<MatchModel, ClubError>
    suspend fun complete(matchId: String): Result<MatchModel, ClubError>
    suspend fun reopen(matchId: String): Result<MatchModel, ClubError>

    /** `teamA`/`teamB` hold member ids and/or guest ids; both null for an automatic draw. */
    suspend fun generateTeams(matchId: String, teamA: List<String>?, teamB: List<String>?): Result<MatchModel, ClubError>
    suspend fun getTeamBalance(matchId: String): Result<TeamBalanceModel?, ClubError>

    suspend fun addEvent(matchId: String, clubMemberId: String, type: MatchEventType, minute: Int?): Result<MatchModel, ClubError>
    suspend fun deleteEvent(matchId: String, eventId: String): Result<MatchModel, ClubError>
    suspend fun setMinutes(matchId: String, clubMemberId: String, minutes: Int): Result<MatchModel, ClubError>
    suspend fun setManualScore(matchId: String, teamAScore: Int, teamBScore: Int): Result<MatchModel, ClubError>
    suspend fun clearManualScore(matchId: String): Result<MatchModel, ClubError>
}
