package com.kikepb.club.data.datasource.local

import com.kikepb.club.data.dto.ClubDTO
import com.kikepb.club.data.dto.ClubMemberDTO
import com.kikepb.club.data.dto.request.CreateClubRequestDto
import com.kikepb.club.data.dto.request.JoinClubRequestDto
import com.kikepb.club.data.mappers.toDomain
import com.kikepb.club.data.mappers.toEntity
import com.kikepb.club.database.SquadfyClubDatabase
import com.kikepb.club.domain.model.ClubMemberModel
import com.kikepb.club.domain.model.ClubModel
import com.kikepb.club.domain.repository.ClubRepository
import com.kikepb.core.data.networking.get
import com.kikepb.core.data.networking.post
import com.kikepb.core.data.networking.postMultipart
import com.kikepb.core.domain.util.DataError
import com.kikepb.core.domain.util.EmptyResult
import com.kikepb.core.domain.util.Result
import com.kikepb.core.domain.util.asEmptyResult
import com.kikepb.core.domain.util.map
import com.kikepb.core.domain.util.onSuccess
import io.ktor.client.HttpClient
import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.client.request.forms.formData
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
