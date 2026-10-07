package com.kikepb.club.presentation.fake

import com.kikepb.club.domain.error.ClubError
import com.kikepb.club.domain.model.ClubBanModel
import com.kikepb.club.domain.model.ClubMatchModel
import com.kikepb.club.domain.model.ClubMemberModel
import com.kikepb.club.domain.model.ClubMemberRole
import com.kikepb.club.domain.model.ClubModel
import com.kikepb.club.domain.model.MatchSignupModel
import com.kikepb.club.domain.model.PlayerPosition
import com.kikepb.club.domain.repository.ClubRepository
import com.kikepb.club.domain.repository.PlayerStatInput
import com.kikepb.core.domain.util.DataError
import com.kikepb.core.domain.util.EmptyResult
import com.kikepb.core.domain.util.Result
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

fun club(id: String = "club-1", name: String = "Squadfy FC") = ClubModel(
    id = id, name = name, description = null, clubLogoUrl = null, ownerId = "owner",
    invitationCode = "AB12CD34", maxMembers = null, membersCount = 1
)

fun member(id: String, userId: String, clubId: String = "club-1", role: ClubMemberRole = ClubMemberRole.PLAYER) = ClubMemberModel(
    id = id, clubId = clubId, userId = userId, username = userId, profilePictureUrl = null,
    shirtNumber = null, position = null, role = role
)

class FakeClubRepository : ClubRepository {
    val clubs = MutableStateFlow<List<ClubModel>>(emptyList())
    val members = MutableStateFlow<List<ClubMemberModel>>(emptyList())

    var createResult: Result<ClubModel, ClubError> = Result.Success(club())
    var joinResult: Result<ClubModel, ClubError> = Result.Success(club())
    var logoResult: Result<ClubModel, ClubError> = Result.Success(club())
    var editResult: Result<ClubModel, ClubError> = Result.Success(club())

    data class JoinCall(val code: String, val shirtNumber: Int?, val position: PlayerPosition?)
    val joinCalls = mutableListOf<JoinCall>()
    val editCalls = mutableListOf<Triple<String?, String?, Int?>>()
    var logoUploads = 0

    override fun observeMyClubs(): Flow<List<ClubModel>> = clubs
    override fun getClubById(clubId: String): Flow<ClubModel?> = clubs.map { list -> list.firstOrNull { it.id == clubId } }
    override fun getClubMembers(clubId: String): Flow<List<ClubMemberModel>> = members.map { list -> list.filter { it.clubId == clubId } }
    override fun getClubMemberById(memberId: String): Flow<ClubMemberModel?> = members.map { list -> list.firstOrNull { it.id == memberId } }
    override fun observeMembershipsOfUser(userId: String): Flow<List<ClubMemberModel>> = members.map { list -> list.filter { it.userId == userId } }

    override suspend fun fetchMyClubs(): EmptyResult<ClubError> = Result.Success(Unit)
    override suspend fun fetchClubById(clubId: String): EmptyResult<ClubError> = Result.Success(Unit)
    override suspend fun fetchClubMembers(clubId: String): EmptyResult<ClubError> = Result.Success(Unit)

    override suspend fun createClub(name: String, description: String?, maxMembers: Int?) = createResult
    override suspend fun joinClub(invitationCode: String, shirtNumber: Int?, position: PlayerPosition?): Result<ClubModel, ClubError> {
        joinCalls += JoinCall(invitationCode, shirtNumber, position)
        return joinResult
    }
    override suspend fun uploadClubLogo(clubId: String, bytes: ByteArray, mimeType: String): Result<ClubModel, ClubError> {
        logoUploads++
        return logoResult
    }
    override suspend fun editClub(clubId: String, name: String?, description: String?, maxMembers: Int?): Result<ClubModel, ClubError> {
        editCalls += Triple(name, description, maxMembers)
        return editResult
    }
    override suspend fun regenerateInvitationCode(clubId: String): Result<String, ClubError> = Result.Success("NEWCODE1")
    override suspend fun updateMyMembership(clubId: String, shirtNumber: Int?, position: PlayerPosition?) = TODO("not used")
    override suspend fun leaveClub(clubId: String): EmptyResult<ClubError> = Result.Success(Unit)
    override suspend fun removeMember(clubId: String, memberId: String): EmptyResult<ClubError> = Result.Success(Unit)
    override suspend fun changeMemberRole(clubId: String, memberId: String, role: ClubMemberRole) = TODO("not used")
    override suspend fun transferOwnership(clubId: String, memberId: String) = TODO("not used")
    override suspend fun getBans(clubId: String): Result<List<ClubBanModel>, ClubError> = Result.Success(emptyList())
    override suspend fun banMember(clubId: String, memberId: String): EmptyResult<ClubError> = Result.Success(Unit)
    override suspend fun unbanMember(clubId: String, memberId: String): EmptyResult<ClubError> = Result.Success(Unit)

    override suspend fun getMatchesForClub(clubId: String): Result<List<ClubMatchModel>, DataError.Remote> = TODO("legacy")
    override suspend fun createMatch(clubId: String, scheduledAt: String?, signupOpensAt: String?, signupClosesAt: String?): Result<ClubMatchModel, DataError.Remote> = TODO("legacy")
    override suspend fun listSignups(matchId: String): Result<List<MatchSignupModel>, DataError.Remote> = TODO("legacy")
    override suspend fun signUpForMatch(matchId: String): Result<MatchSignupModel, DataError.Remote> = TODO("legacy")
    override suspend fun cancelSignup(matchId: String): EmptyResult<DataError.Remote> = TODO("legacy")
    override suspend fun addGuest(matchId: String, guestName: String, position: String?, rating: Int?): Result<MatchSignupModel, DataError.Remote> = TODO("legacy")
    override suspend fun removeSignup(matchId: String, signupId: String): EmptyResult<DataError.Remote> = TODO("legacy")
    override suspend fun generateTeams(matchId: String, mode: String, manualTeamA: List<String>?, manualTeamB: List<String>?): Result<ClubMatchModel, DataError.Remote> = TODO("legacy")
    override suspend fun recordMatchResult(matchId: String, teamAScore: Int, teamBScore: Int, playerStats: List<PlayerStatInput>): Result<ClubMatchModel, DataError.Remote> = TODO("legacy")
}
