package com.kikepb.club.domain.repository

import com.kikepb.club.domain.model.ClubMatchModel
import com.kikepb.club.domain.model.ClubMemberModel
import com.kikepb.club.domain.model.ClubModel
import com.kikepb.club.domain.model.ClubScheduleExceptionModel
import com.kikepb.club.domain.model.MatchSignupModel
import com.kikepb.core.domain.util.DataError
import com.kikepb.core.domain.util.EmptyResult
import com.kikepb.core.domain.util.Result

interface ClubService {
    suspend fun getClubById(clubId: String): Result<ClubModel, DataError.Remote>
    suspend fun getClubMembers(clubId: String): Result<List<ClubMemberModel>, DataError.Remote>
    suspend fun joinClub(invitationCode: String, shirtNumber: Int?, position: String?): Result<ClubModel, DataError.Remote>
    suspend fun createClub(name: String, description: String?, clubLogoUrl: String?, maxMembers: Int?): Result<ClubModel, DataError.Remote>
    suspend fun uploadClubLogo(clubId: String, bytes: ByteArray, mimeType: String): Result<ClubModel, DataError.Remote>

    suspend fun updateMember(clubId: String, memberId: String, shirtNumber: Int?, position: String?, rating: Int?): Result<ClubMemberModel, DataError.Remote>

    suspend fun updateSchedule(
        clubId: String,
        matchDayOfWeek: String?,
        matchStartTime: String?,
        matchEndTime: String?,
        seasonStartMonth: Int?,
        seasonStartDay: Int?,
        drawTime: String?
    ): Result<ClubModel, DataError.Remote>

    suspend fun listScheduleExceptions(clubId: String): Result<List<ClubScheduleExceptionModel>, DataError.Remote>
    suspend fun addScheduleException(clubId: String, date: String, reason: String?): Result<ClubScheduleExceptionModel, DataError.Remote>
    suspend fun removeScheduleException(clubId: String, exceptionId: String): EmptyResult<DataError.Remote>

    suspend fun getMatchesForClub(clubId: String): Result<List<ClubMatchModel>, DataError.Remote>
    suspend fun getMatch(matchId: String): Result<ClubMatchModel, DataError.Remote>
    suspend fun createMatch(clubId: String, scheduledAt: String?, signupOpensAt: String?, signupClosesAt: String?): Result<ClubMatchModel, DataError.Remote>
    suspend fun cancelMatch(matchId: String): Result<ClubMatchModel, DataError.Remote>

    suspend fun listSignups(matchId: String): Result<List<MatchSignupModel>, DataError.Remote>
    suspend fun signUpForMatch(matchId: String): Result<MatchSignupModel, DataError.Remote>
    suspend fun cancelSignup(matchId: String): EmptyResult<DataError.Remote>
    suspend fun addGuest(matchId: String, guestName: String, position: String?, rating: Int?): Result<MatchSignupModel, DataError.Remote>
    suspend fun removeSignup(matchId: String, signupId: String): EmptyResult<DataError.Remote>

    suspend fun generateTeams(matchId: String, mode: String, manualTeamA: List<String>?, manualTeamB: List<String>?): Result<ClubMatchModel, DataError.Remote>

    suspend fun recordMatchResult(
        matchId: String,
        teamAScore: Int,
        teamBScore: Int,
        playerStats: List<PlayerStatInput>
    ): Result<ClubMatchModel, DataError.Remote>
}

data class PlayerStatInput(
    val clubMemberId: String,
    val goals: Int = 0,
    val assists: Int = 0,
    val yellowCards: Int = 0,
    val redCards: Int = 0,
    val minutesPlayed: Int = 0
)
