package com.kikepb.club.data.datasource.local

import com.kikepb.club.data.dto.ClubBanDTO
import com.kikepb.club.data.dto.ClubDTO
import com.kikepb.club.data.dto.ClubMatchDTO
import com.kikepb.club.data.dto.ClubMemberDTO
import com.kikepb.club.data.dto.InvitationCodeDTO
import com.kikepb.club.data.dto.MatchSignupDTO
import com.kikepb.club.data.dto.request.AddGuestRequestDto
import com.kikepb.club.data.dto.request.ChangeRoleRequestDto
import com.kikepb.club.data.dto.request.CreateClubRequestDto
import com.kikepb.club.data.dto.request.CreateMatchRequestDto
import com.kikepb.club.data.dto.request.EditClubRequestDto
import com.kikepb.club.data.dto.request.GenerateTeamsRequestDto
import com.kikepb.club.data.dto.request.JoinClubRequestDto
import com.kikepb.club.data.dto.request.PlayerStatRequestDto
import com.kikepb.club.data.dto.request.RecordMatchResultRequestDto
import com.kikepb.club.data.dto.request.TransferOwnershipRequestDto
import com.kikepb.club.data.dto.request.UpdateMyMembershipRequestDto
import com.kikepb.club.data.mappers.toDomain
import com.kikepb.club.data.mappers.toEntity
import com.kikepb.club.database.SquadfyClubDatabase
import com.kikepb.club.domain.error.ClubError
import com.kikepb.club.domain.error.ClubOperation
import com.kikepb.club.domain.error.toClubError
import com.kikepb.club.domain.model.ClubBanModel
import com.kikepb.club.domain.model.ClubMatchModel
import com.kikepb.club.domain.model.ClubMemberModel
import com.kikepb.club.domain.model.ClubMemberRole
import com.kikepb.club.domain.model.ClubModel
import com.kikepb.club.domain.model.MatchSignupModel
import com.kikepb.club.domain.model.PlayerPosition
import com.kikepb.club.domain.repository.ClubRepository
import com.kikepb.club.domain.repository.PlayerStatInput
import com.kikepb.core.data.networking.apiDelete
import com.kikepb.core.data.networking.apiGet
import com.kikepb.core.data.networking.apiPatch
import com.kikepb.core.data.networking.apiPost
import com.kikepb.core.data.networking.apiPutMultipart
import com.kikepb.core.data.networking.constructRoute
import com.kikepb.core.data.networking.delete
import com.kikepb.core.data.networking.get
import com.kikepb.core.data.networking.post
import com.kikepb.core.data.networking.safeCall
import com.kikepb.core.domain.util.DataError
import com.kikepb.core.domain.util.EmptyResult
import com.kikepb.core.domain.util.RemoteError
import com.kikepb.core.domain.util.Result
import com.kikepb.core.domain.util.asEmptyResult
import com.kikepb.core.domain.util.map
import com.kikepb.core.domain.util.mapError
import com.kikepb.core.domain.util.onSuccess
import io.ktor.client.HttpClient
import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.client.request.forms.formData
import io.ktor.client.request.post
import io.ktor.client.request.url
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class OfflineFirstClubRepositoryImpl(
    private val httpClient: HttpClient,
    private val db: SquadfyClubDatabase
) : ClubRepository {

    // region Reads (Room)

    override fun observeMyClubs(): Flow<List<ClubModel>> =
        db.clubDao.observeAllClubs().map { entities -> entities.map { it.toDomain() } }

    override fun getClubById(clubId: String): Flow<ClubModel?> =
        db.clubDao.observeClubById(clubId = clubId).map { entity -> entity?.toDomain() }

    override fun getClubMembers(clubId: String): Flow<List<ClubMemberModel>> =
        db.clubMemberDao.observeMembersByClub(clubId = clubId).map { entities -> entities.map { it.toDomain() } }

    override fun getClubMemberById(memberId: String): Flow<ClubMemberModel?> =
        db.clubMemberDao.observeMemberById(memberId = memberId).map { entity -> entity?.toDomain() }

    override fun observeMembershipsOfUser(userId: String): Flow<List<ClubMemberModel>> =
        db.clubMemberDao.observeMembershipsOfUser(userId = userId).map { entities -> entities.map { it.toDomain() } }

    // endregion

    // region Sync

    override suspend fun fetchMyClubs(): EmptyResult<ClubError> =
        httpClient.apiGet<List<ClubDTO>>(route = CLUBS)
            .onSuccess { clubs -> db.clubDao.syncClubs(clubs = clubs.map { it.toEntity() }) }
            .asClubResult()
            .asEmptyResult()

    override suspend fun fetchClubById(clubId: String): EmptyResult<ClubError> =
        httpClient.apiGet<ClubDTO>(route = "$CLUBS/$clubId")
            .onSuccess { dto -> db.clubDao.upsertClub(club = dto.toEntity()) }
            .asClubResult()
            .asEmptyResult()

    override suspend fun fetchClubMembers(clubId: String): EmptyResult<ClubError> =
        httpClient.apiGet<List<ClubMemberDTO>>(route = "$CLUBS/$clubId/members")
            .onSuccess { members -> db.clubMemberDao.syncMembers(clubId = clubId, members = members.map { it.toEntity() }) }
            .asClubResult()
            .asEmptyResult()

    // endregion

    // region Clubs

    override suspend fun createClub(name: String, description: String?, maxMembers: Int?): Result<ClubModel, ClubError> =
        httpClient.apiPost<CreateClubRequestDto, ClubDTO>(
            route = CLUBS,
            body = CreateClubRequestDto(name = name, description = description, maxMembers = maxMembers)
        ).cacheClub()

    override suspend fun joinClub(invitationCode: String, shirtNumber: Int?, position: PlayerPosition?): Result<ClubModel, ClubError> =
        httpClient.apiPost<JoinClubRequestDto, ClubDTO>(
            route = "$CLUBS/join",
            body = JoinClubRequestDto(invitationCode = invitationCode, shirtNumber = shirtNumber, position = position?.name)
        ).cacheClub(operation = ClubOperation.JOIN)

    override suspend fun uploadClubLogo(clubId: String, bytes: ByteArray, mimeType: String): Result<ClubModel, ClubError> =
        httpClient.apiPutMultipart<ClubDTO>(
            route = "$CLUBS/$clubId/logo",
            content = buildMultipartImage(key = "clubLogo", bytes = bytes, mimeType = mimeType, filename = "logo")
        ).cacheClub()

    override suspend fun editClub(clubId: String, name: String?, description: String?, maxMembers: Int?): Result<ClubModel, ClubError> =
        httpClient.apiPatch<EditClubRequestDto, ClubDTO>(
            route = "$CLUBS/$clubId",
            body = EditClubRequestDto(name = name, description = description, maxMembers = maxMembers)
        ).cacheClub(operation = ClubOperation.EDIT_CLUB)

    override suspend fun regenerateInvitationCode(clubId: String): Result<String, ClubError> =
        httpClient.apiPost<Unit, InvitationCodeDTO>(route = "$CLUBS/$clubId/invitation-code", body = Unit)
            .asClubResult()
            .map { it.invitationCode }
            .also { result ->
                // The code is part of the cached club: refresh it so every screen shows the new one
                if (result is Result.Success) fetchClubById(clubId)
            }

    override suspend fun transferOwnership(clubId: String, memberId: String): Result<ClubModel, ClubError> =
        httpClient.apiPost<TransferOwnershipRequestDto, ClubDTO>(
            route = "$CLUBS/$clubId/transfer-ownership",
            body = TransferOwnershipRequestDto(memberId = memberId)
        ).cacheClub().also { result ->
            // Both members change role (new OWNER, former owner becomes ADMIN)
            if (result is Result.Success) fetchClubMembers(clubId)
        }

    // endregion

    // region Members

    override suspend fun updateMyMembership(clubId: String, shirtNumber: Int?, position: PlayerPosition?): Result<ClubMemberModel, ClubError> =
        httpClient.apiPatch<UpdateMyMembershipRequestDto, ClubMemberDTO>(
            route = "$CLUBS/$clubId/members/me",
            body = UpdateMyMembershipRequestDto(shirtNumber = shirtNumber, position = position?.name)
        ).cacheMember()

    override suspend fun changeMemberRole(clubId: String, memberId: String, role: ClubMemberRole): Result<ClubMemberModel, ClubError> =
        httpClient.apiPatch<ChangeRoleRequestDto, ClubMemberDTO>(
            route = "$CLUBS/$clubId/members/$memberId/role",
            body = ChangeRoleRequestDto(role = role.name)
        ).cacheMember()

    override suspend fun leaveClub(clubId: String): EmptyResult<ClubError> =
        httpClient.apiDelete<Unit>(route = "$CLUBS/$clubId/members/me")
            .onSuccess { db.clubDao.deleteClubById(clubId = clubId) }
            .asClubResult()

    override suspend fun removeMember(clubId: String, memberId: String): EmptyResult<ClubError> =
        httpClient.apiDelete<Unit>(route = "$CLUBS/$clubId/members/$memberId")
            .onSuccess { forgetMember(clubId = clubId, memberId = memberId) }
            .asClubResult()

    override suspend fun getBans(clubId: String): Result<List<ClubBanModel>, ClubError> =
        httpClient.apiGet<List<ClubBanDTO>>(route = "$CLUBS/$clubId/bans")
            .asClubResult()
            .map { bans ->
                bans.map { ClubBanModel(clubMemberId = it.clubMemberId, userId = it.userId, username = it.username, bannedAt = it.bannedAt) }
            }

    override suspend fun banMember(clubId: String, memberId: String): EmptyResult<ClubError> =
        httpClient.apiPost<Unit, Unit>(route = "$CLUBS/$clubId/members/$memberId/ban", body = Unit)
            .onSuccess { forgetMember(clubId = clubId, memberId = memberId) }
            .asClubResult()

    override suspend fun unbanMember(clubId: String, memberId: String): EmptyResult<ClubError> =
        httpClient.apiDelete<Unit>(route = "$CLUBS/$clubId/members/$memberId/ban")
            .asClubResult()

    /** A removed or banned member disappears from the club and its member count (BE-001 RN-10). */
    private suspend fun forgetMember(clubId: String, memberId: String) {
        db.clubMemberDao.deleteMemberById(memberId = memberId)
        fetchClubById(clubId)
    }

    // endregion

    private suspend fun Result<ClubDTO, RemoteError>.cacheClub(operation: ClubOperation = ClubOperation.OTHER): Result<ClubModel, ClubError> =
        onSuccess { dto -> db.clubDao.upsertClub(club = dto.toEntity()) }
            .asClubResult(operation)
            .map { it.toDomain() }

    private suspend fun Result<ClubMemberDTO, RemoteError>.cacheMember(): Result<ClubMemberModel, ClubError> =
        onSuccess { dto -> db.clubMemberDao.upsertMembers(members = listOf(dto.toEntity())) }
            .asClubResult()
            .map { it.toDomain() }

    private fun <T> Result<T, RemoteError>.asClubResult(operation: ClubOperation = ClubOperation.OTHER): Result<T, ClubError> =
        mapError { it.toClubError(operation) }

    // region Legacy match flow (pre-v1 routes), replaced in specs 005-007

    override suspend fun getMatchesForClub(clubId: String): Result<List<ClubMatchModel>, DataError.Remote> =
        httpClient.get<List<ClubMatchDTO>>(route = "/club/$clubId/matches").map { matches ->
            matches.map { it.toDomain() }
        }

    override suspend fun createMatch(clubId: String, scheduledAt: String?, signupOpensAt: String?, signupClosesAt: String?): Result<ClubMatchModel, DataError.Remote> =
        httpClient.post<CreateMatchRequestDto, ClubMatchDTO>(
            route = "/club/$clubId/matches",
            body = CreateMatchRequestDto(scheduledAt = scheduledAt, signupOpensAt = signupOpensAt, signupClosesAt = signupClosesAt)
        ).map { it.toDomain() }

    override suspend fun listSignups(matchId: String): Result<List<MatchSignupModel>, DataError.Remote> =
        httpClient.get<List<MatchSignupDTO>>(route = "/club/matches/$matchId/signups").map { signups ->
            signups.map { it.toDomain() }
        }

    override suspend fun signUpForMatch(matchId: String): Result<MatchSignupModel, DataError.Remote> =
        safeCall<MatchSignupDTO> {
            httpClient.post {
                url(constructRoute("/club/matches/$matchId/signups"))
            }
        }.map { it.toDomain() }

    override suspend fun cancelSignup(matchId: String): EmptyResult<DataError.Remote> =
        httpClient.delete<Unit>(route = "/club/matches/$matchId/signups/me").asEmptyResult()

    override suspend fun addGuest(matchId: String, guestName: String, position: String?, rating: Int?): Result<MatchSignupModel, DataError.Remote> =
        httpClient.post<AddGuestRequestDto, MatchSignupDTO>(
            route = "/club/matches/$matchId/guests",
            body = AddGuestRequestDto(guestName = guestName, position = position, rating = rating)
        ).map { it.toDomain() }

    override suspend fun removeSignup(matchId: String, signupId: String): EmptyResult<DataError.Remote> =
        httpClient.delete<Unit>(route = "/club/matches/$matchId/signups/$signupId").asEmptyResult()

    override suspend fun generateTeams(matchId: String, mode: String, manualTeamA: List<String>?, manualTeamB: List<String>?): Result<ClubMatchModel, DataError.Remote> =
        httpClient.post<GenerateTeamsRequestDto, ClubMatchDTO>(
            route = "/club/matches/$matchId/generate-teams",
            body = GenerateTeamsRequestDto(mode = mode, manualTeamA = manualTeamA, manualTeamB = manualTeamB)
        ).map { it.toDomain() }

    override suspend fun recordMatchResult(
        matchId: String,
        teamAScore: Int,
        teamBScore: Int,
        playerStats: List<PlayerStatInput>
    ): Result<ClubMatchModel, DataError.Remote> =
        httpClient.post<RecordMatchResultRequestDto, ClubMatchDTO>(
            route = "/club/matches/$matchId/result",
            body = RecordMatchResultRequestDto(
                teamAScore = teamAScore,
                teamBScore = teamBScore,
                playerStats = playerStats.map {
                    PlayerStatRequestDto(
                        clubMemberId = it.clubMemberId,
                        goals = it.goals,
                        assists = it.assists,
                        yellowCards = it.yellowCards,
                        redCards = it.redCards,
                        minutesPlayed = it.minutesPlayed
                    )
                }
            )
        ).map { it.toDomain() }

    // endregion

    private fun buildMultipartImage(key: String, bytes: ByteArray, mimeType: String, filename: String): MultiPartFormDataContent =
        MultiPartFormDataContent(
            formData {
                append(
                    key = key,
                    value = bytes,
                    headers = Headers.build {
                        append(HttpHeaders.ContentType, mimeType)
                        append(HttpHeaders.ContentDisposition, "filename=\"$filename\"")
                    }
                )
            }
        )

    private companion object {
        const val CLUBS = "/clubs"
    }
}
