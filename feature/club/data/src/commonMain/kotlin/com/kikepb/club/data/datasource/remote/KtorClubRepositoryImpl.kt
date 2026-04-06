package com.kikepb.club.data.datasource.remote

import com.kikepb.club.data.dto.ClubDTO
import com.kikepb.club.data.dto.ClubLogoUploadUrlsResponseDto
import com.kikepb.club.data.dto.ClubMemberDTO
import com.kikepb.club.data.dto.CreateClubRequestDto
import com.kikepb.club.data.dto.JoinClubRequestDto
import com.kikepb.club.data.mappers.clubMemberToDomain
import com.kikepb.club.data.mappers.clubToDomain
import com.kikepb.club.domain.model.ClubMemberModel
import com.kikepb.club.domain.model.ClubModel
import com.kikepb.club.domain.repository.ClubService
import com.kikepb.core.data.networking.get
import com.kikepb.core.data.networking.post
import com.kikepb.core.data.networking.safeCall
import com.kikepb.core.domain.util.DataError
import com.kikepb.core.domain.util.Result
import com.kikepb.core.domain.util.Result.Failure
import com.kikepb.core.domain.util.Result.Success
import com.kikepb.core.domain.util.map
import io.ktor.client.HttpClient
import io.ktor.client.request.header
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.client.request.url

class KtorClubRepositoryImpl(
    private val httpClient: HttpClient
) : ClubService {

    override suspend fun getClubById(clubId: String): Result<ClubModel, DataError.Remote> =
        httpClient.get<ClubDTO>(route = "/club/$clubId").map { it.clubToDomain() }

    override suspend fun getClubMembers(clubId: String): Result<List<ClubMemberModel>, DataError.Remote> =
        httpClient.get<List<ClubMemberDTO>>(route = "/club/$clubId/members").map { members ->
            members.map { it.clubMemberToDomain() }
        }

    override suspend fun joinClub(invitationCode: String, shirtNumber: Int?, position: String?): Result<ClubModel, DataError.Remote> =
        httpClient.post<JoinClubRequestDto, ClubDTO>(
            route = "/club/join",
            body = JoinClubRequestDto(
                invitationCode = invitationCode,
                shirtNumber = shirtNumber,
                position = position
            )
        ).map { it.clubToDomain() }

    override suspend fun createClub(name: String, description: String?, clubLogoUrl: String?, maxMembers: Int?): Result<ClubModel, DataError.Remote> =
        httpClient.post<CreateClubRequestDto, ClubDTO>(
            route = "/club/create",
            body = CreateClubRequestDto(
                name = name,
                description = description,
                clubLogoUrl = clubLogoUrl,
                maxMembers = maxMembers
            )
        ).map { it.clubToDomain() }

    override suspend fun uploadClubLogo(bytes: ByteArray, mimeType: String): Result<String, DataError.Remote> {
        val urlsResult = httpClient.post<Unit, ClubLogoUploadUrlsResponseDto>(
            route = "/club/logo-upload",
            queryParams = mapOf("mimeType" to mimeType),
            body = Unit
        )

        val urls = when (urlsResult) {
            is Success -> urlsResult.data
            is Failure -> return Failure(error = urlsResult.error)
        }

        val uploadResult: Result<Unit, DataError.Remote> = safeCall {
            httpClient.put {
                url(urlString = urls.uploadUrl)
                urls.headers.forEach { (key, value) -> header(key, value) }
                setBody(body = bytes)
            }
        }

        return when (uploadResult) {
            is Success -> Success(data = urls.publicUrl)
            is Failure -> Failure(error = uploadResult.error)
        }
    }
}
