package com.kikepb.club.data.datasource.local

import com.kikepb.club.data.dto.ClubDTO
import com.kikepb.club.data.dto.ClubLogoUploadUrlsResponseDto
import com.kikepb.club.data.dto.ClubMemberDTO
import com.kikepb.club.data.dto.CreateClubRequestDto
import com.kikepb.club.data.dto.JoinClubRequestDto
import com.kikepb.club.data.mappers.toDomain
import com.kikepb.club.data.mappers.toEntity
import com.kikepb.club.database.SquadfyClubDatabase
import com.kikepb.club.domain.model.ClubMemberModel
import com.kikepb.club.domain.model.ClubModel
import com.kikepb.club.domain.repository.ClubRepository
import com.kikepb.core.data.networking.get
import com.kikepb.core.data.networking.post
import com.kikepb.core.data.networking.safeCall
import com.kikepb.core.domain.util.DataError
import com.kikepb.core.domain.util.EmptyResult
import com.kikepb.core.domain.util.Result
import com.kikepb.core.domain.util.asEmptyResult
import com.kikepb.core.domain.util.map
import com.kikepb.core.domain.util.onSuccess
import io.ktor.client.HttpClient
import io.ktor.client.request.header
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.client.request.url
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

    override suspend fun fetchClubById(clubId: String): EmptyResult<DataError.Remote> =
        httpClient.get<ClubDTO>(route = "/club/$clubId")
            .onSuccess { dto ->
                db.clubDao.upsertClub(club = dto.toEntity())
            }
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

    override suspend fun uploadClubLogo(bytes: ByteArray, mimeType: String): Result<String, DataError.Remote> {
        val urlsResult = httpClient.post<Unit, ClubLogoUploadUrlsResponseDto>(
            route = "/club/logo-upload",
            queryParams = mapOf("mimeType" to mimeType),
            body = Unit
        )

        val urls = when (urlsResult) {
            is Result.Success -> urlsResult.data
            is Result.Failure -> return Result.Failure(error = urlsResult.error)
        }

        val uploadResult: Result<Unit, DataError.Remote> = safeCall {
            httpClient.put {
                url(urlString = urls.uploadUrl)
                urls.headers.forEach { (key, value) -> header(key, value) }
                setBody(body = bytes)
            }
        }

        return when (uploadResult) {
            is Result.Success -> Result.Success(data = urls.publicUrl)
            is Result.Failure -> Result.Failure(error = uploadResult.error)
        }
    }
}
