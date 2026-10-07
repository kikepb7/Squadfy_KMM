package com.kikepb.club.domain.repository

import com.kikepb.club.domain.error.ClubError
import com.kikepb.club.domain.model.ClubBanModel
import com.kikepb.club.domain.model.ClubMatchModel
import com.kikepb.club.domain.model.ClubMemberModel
import com.kikepb.club.domain.model.ClubMemberRole
import com.kikepb.club.domain.model.ClubModel
import com.kikepb.club.domain.model.MatchSignupModel
import com.kikepb.club.domain.model.PlayerPosition
import com.kikepb.core.domain.util.DataError
import com.kikepb.core.domain.util.EmptyResult
import com.kikepb.core.domain.util.Result
import kotlinx.coroutines.flow.Flow

/** Clubs and memberships (spec 003, backend BE-001). Reads come from Room, writes go to the API v1. */
interface ClubRepository {

    fun observeMyClubs(): Flow<List<ClubModel>>
    fun getClubById(clubId: String): Flow<ClubModel?>
    fun getClubMembers(clubId: String): Flow<List<ClubMemberModel>>
    fun getClubMemberById(memberId: String): Flow<ClubMemberModel?>
    fun observeMembershipsOfUser(userId: String): Flow<List<ClubMemberModel>>

    suspend fun fetchMyClubs(): EmptyResult<ClubError>
    suspend fun fetchClubById(clubId: String): EmptyResult<ClubError>
    suspend fun fetchClubMembers(clubId: String): EmptyResult<ClubError>

    suspend fun createClub(name: String, description: String?, maxMembers: Int?): Result<ClubModel, ClubError>
    suspend fun joinClub(invitationCode: String, shirtNumber: Int?, position: PlayerPosition?): Result<ClubModel, ClubError>
    suspend fun uploadClubLogo(clubId: String, bytes: ByteArray, mimeType: String): Result<ClubModel, ClubError>
    suspend fun editClub(clubId: String, name: String?, description: String?, maxMembers: Int?): Result<ClubModel, ClubError>
    suspend fun regenerateInvitationCode(clubId: String): Result<String, ClubError>

    suspend fun updateMyMembership(clubId: String, shirtNumber: Int?, position: PlayerPosition?): Result<ClubMemberModel, ClubError>
    suspend fun leaveClub(clubId: String): EmptyResult<ClubError>
    suspend fun removeMember(clubId: String, memberId: String): EmptyResult<ClubError>
    suspend fun changeMemberRole(clubId: String, memberId: String, role: ClubMemberRole): Result<ClubMemberModel, ClubError>
    suspend fun transferOwnership(clubId: String, memberId: String): Result<ClubModel, ClubError>

    suspend fun getBans(clubId: String): Result<List<ClubBanModel>, ClubError>
    suspend fun banMember(clubId: String, memberId: String): EmptyResult<ClubError>
    suspend fun unbanMember(clubId: String, memberId: String): EmptyResult<ClubError>

    // region Legacy match flow (pre-v1 routes). Replaced by the announcement and match repositories in specs 005-007.
    suspend fun getMatchesForClub(clubId: String): Result<List<ClubMatchModel>, DataError.Remote>
    suspend fun createMatch(clubId: String, scheduledAt: String?, signupOpensAt: String?, signupClosesAt: String?): Result<ClubMatchModel, DataError.Remote>
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
    // endregion
}

data class PlayerStatInput(
    val clubMemberId: String,
    val goals: Int,
    val assists: Int,
    val yellowCards: Int,
    val redCards: Int,
    val minutesPlayed: Int
)
