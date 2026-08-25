package com.kikepb.club.data.datasource.local

import com.kikepb.club.data.dto.ClubDTO
import com.kikepb.club.data.dto.ClubMatchDTO
import com.kikepb.club.data.dto.ClubMemberDTO
import com.kikepb.club.data.dto.ClubScheduleExceptionDTO
import com.kikepb.club.data.dto.MatchSignupDTO
import com.kikepb.club.data.dto.request.AddGuestRequestDto
import com.kikepb.club.data.dto.request.AddScheduleExceptionRequestDto
import com.kikepb.club.data.dto.request.CreateClubRequestDto
import com.kikepb.club.data.dto.request.CreateMatchRequestDto
import com.kikepb.club.data.dto.request.GenerateTeamsRequestDto
import com.kikepb.club.data.dto.request.JoinClubRequestDto
import com.kikepb.club.data.dto.request.PlayerStatRequestDto
import com.kikepb.club.data.dto.request.RecordMatchResultRequestDto
import com.kikepb.club.data.dto.request.UpdateScheduleRequestDto
import com.kikepb.club.data.mappers.toDomain
import com.kikepb.club.data.mappers.toEntity
import com.kikepb.club.database.SquadfyClubDatabase
import com.kikepb.club.domain.model.ClubMatchModel
import com.kikepb.club.domain.model.ClubMemberModel
import com.kikepb.club.domain.model.ClubModel
import com.kikepb.club.domain.model.ClubScheduleExceptionModel
import com.kikepb.club.domain.model.MatchSignupModel
import com.kikepb.club.domain.repository.ClubRepository
import com.kikepb.club.domain.repository.PlayerStatInput
import com.kikepb.core.data.networking.constructRoute
import com.kikepb.core.data.networking.delete
import com.kikepb.core.data.networking.get
import com.kikepb.core.data.networking.patch
import com.kikepb.core.data.networking.post
import com.kikepb.core.data.networking.postMultipart
import com.kikepb.core.data.networking.safeCall
import com.kikepb.core.domain.util.DataError
import com.kikepb.core.domain.util.EmptyResult
import com.kikepb.core.domain.util.Result
import com.kikepb.core.domain.util.asEmptyResult
import com.kikepb.core.domain.util.map
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

    override fun getClubById(clubId: String): Flow<ClubModel?> =
        db.clubDao.observeClubById(clubId = clubId)
            .map { entity -> entity?.toDomain() }

    override fun getClubMembers(clubId: String): Flow<List<ClubMemberModel>> =
        db.clubMemberDao.observeMembersByClub(clubId = clubId)
            .map { entities -> entities.map { it.toDomain() } }

    override fun getClubMemberById(memberId: String): Flow<ClubMemberModel?> =
        db.clubMemberDao.observeMemberById(memberId = memberId)
            .map { entity -> entity?.toDomain() }

    override suspend fun fetchClubById(clubId: String): EmptyResult<DataError.Remote> =
        httpClient.get<ClubDTO>(route = "/club/$clubId")
            .onSuccess { dto -> db.clubDao.upsertClub(club = dto.toEntity()) }
            .asEmptyResult()

    override suspend fun fetchClubMembers(clubId: String): EmptyResult<DataError.Remote> =
        httpClient.get<List<ClubMemberDTO>>(route = "/club/$clubId/members")
            .onSuccess { members ->
                db.clubMemberDao.syncMembers(
                    clubId = clubId,
                    members = members.map { it.toEntity() }
                )
            }
            .asEmptyResult()

    override suspend fun joinClub(invitationCode: String, shirtNumber: Int?, position: String?): Result<ClubModel, DataError.Remote> =
        httpClient.post<JoinClubRequestDto, ClubDTO>(
            route = "/club/join",
            body = JoinClubRequestDto(invitationCode = invitationCode, shirtNumber = shirtNumber, position = position)
        )
            .onSuccess { dto -> db.clubDao.upsertClub(club = dto.toEntity()) }
            .map { it.toEntity().toDomain() }

    override suspend fun createClub(name: String, description: String?, clubLogoUrl: String?, maxMembers: Int?): Result<ClubModel, DataError.Remote> =
        httpClient.post<CreateClubRequestDto, ClubDTO>(
            route = "/club/create",
            body = CreateClubRequestDto(name = name, description = description, clubLogoUrl = clubLogoUrl, maxMembers = maxMembers)
        )
            .onSuccess { dto -> db.clubDao.upsertClub(club = dto.toEntity()) }
            .map { it.toEntity().toDomain() }

    override suspend fun uploadClubLogo(clubId: String, bytes: ByteArray, mimeType: String): Result<ClubModel, DataError.Remote> =
        httpClient.postMultipart<ClubDTO>(
            route = "/club/$clubId/logo",
            content = buildMultipartImage(key = "clubLogo", bytes = bytes, mimeType = mimeType, filename = "logo")
        )
            .onSuccess { dto -> db.clubDao.upsertClub(club = dto.toEntity()) }
            .map { it.toEntity().toDomain() }

    override suspend fun uploadMemberPhoto(clubId: String, memberId: String, bytes: ByteArray, mimeType: String): Result<ClubMemberModel, DataError.Remote> =
        httpClient.postMultipart<ClubMemberDTO>(
            route = "/club/$clubId/members/$memberId/photo",
            content = buildMultipartImage(key = "memberPhoto", bytes = bytes, mimeType = mimeType, filename = "photo")
        )
            .onSuccess { dto -> db.clubMemberDao.upsertMembers(listOf(dto.toEntity())) }
            .map { it.toEntity().toDomain() }

    override suspend fun updateSchedule(
        clubId: String,
        matchDayOfWeek: String?,
        matchStartTime: String?,
        matchEndTime: String?,
        seasonStartMonth: Int?,
        seasonStartDay: Int?,
        drawTime: String?
    ): Result<ClubModel, DataError.Remote> =
        httpClient.patch<UpdateScheduleRequestDto, ClubDTO>(
            route = "/club/$clubId/schedule",
            body = UpdateScheduleRequestDto(
                matchDayOfWeek = matchDayOfWeek,
                matchStartTime = matchStartTime,
                matchEndTime = matchEndTime,
                seasonStartMonth = seasonStartMonth,
                seasonStartDay = seasonStartDay,
                drawTime = drawTime
            )
        )
            .onSuccess { dto -> db.clubDao.upsertClub(club = dto.toEntity()) }
            .map { it.toEntity().toDomain() }

    override suspend fun listScheduleExceptions(clubId: String): Result<List<ClubScheduleExceptionModel>, DataError.Remote> =
        httpClient.get<List<ClubScheduleExceptionDTO>>(route = "/club/$clubId/schedule/exceptions").map { exceptions ->
            exceptions.map { it.toDomain() }
        }

    override suspend fun addScheduleException(clubId: String, date: String, reason: String?): Result<ClubScheduleExceptionModel, DataError.Remote> =
        httpClient.post<AddScheduleExceptionRequestDto, ClubScheduleExceptionDTO>(
            route = "/club/$clubId/schedule/exceptions",
            body = AddScheduleExceptionRequestDto(date = date, reason = reason)
        ).map { it.toDomain() }

    override suspend fun removeScheduleException(clubId: String, exceptionId: String): EmptyResult<DataError.Remote> =
        httpClient.delete<Unit>(route = "/club/$clubId/schedule/exceptions/$exceptionId").asEmptyResult()

    override suspend fun getMatchesForClub(clubId: String): Result<List<ClubMatchModel>, DataError.Remote> =
        httpClient.get<List<ClubMatchDTO>>(route = "/club/$clubId/matches").map { matches ->
            matches.map { it.toDomain() }
        }

    override suspend fun getMatch(matchId: String): Result<ClubMatchModel, DataError.Remote> =
        httpClient.get<ClubMatchDTO>(route = "/club/matches/$matchId").map { it.toDomain() }

    override suspend fun createMatch(clubId: String, scheduledAt: String?, signupOpensAt: String?, signupClosesAt: String?): Result<ClubMatchModel, DataError.Remote> =
        httpClient.post<CreateMatchRequestDto, ClubMatchDTO>(
            route = "/club/$clubId/matches",
            body = CreateMatchRequestDto(scheduledAt = scheduledAt, signupOpensAt = signupOpensAt, signupClosesAt = signupClosesAt)
        ).map { it.toDomain() }

    override suspend fun cancelMatch(matchId: String): Result<ClubMatchModel, DataError.Remote> =
        safeCall<ClubMatchDTO> {
            httpClient.post {
                url(constructRoute("/club/matches/$matchId/cancel"))
            }
        }.map { it.toDomain() }

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
}
