package com.kikepb.club.data.datasource.remote

import com.kikepb.club.data.dto.ClubDTO
import com.kikepb.club.data.dto.ClubMemberDTO
import com.kikepb.club.data.dto.request.CreateClubRequestDto
import com.kikepb.club.data.dto.request.JoinClubRequestDto
import com.kikepb.club.data.mappers.clubMemberToDomain
import com.kikepb.club.data.mappers.clubToDomain
import com.kikepb.club.domain.model.ClubMemberModel
import com.kikepb.club.domain.model.ClubModel
import com.kikepb.club.domain.repository.ClubService
import com.kikepb.core.data.networking.constructRoute
import com.kikepb.core.data.networking.get
import com.kikepb.core.data.networking.post
import com.kikepb.core.data.networking.safeCall
import com.kikepb.core.domain.util.DataError
import com.kikepb.core.domain.util.Result
import com.kikepb.core.domain.util.map
import io.ktor.client.HttpClient
import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.client.request.forms.formData
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.request.url
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders

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


    // TODO --> Change by use own methods
    override suspend fun uploadClubLogo(clubId: String, bytes: ByteArray, mimeType: String): Result<ClubModel, DataError.Remote> =
        safeCall<ClubDTO> {
            httpClient.post {
                url(constructRoute("/club/$clubId/logo"))
                setBody(
                    MultiPartFormDataContent(
                        formData {
                            append(
                                key = "clubLogo",
                                value = bytes,
                                headers = Headers.build {
                                    append(HttpHeaders.ContentType, mimeType)
                                    append(HttpHeaders.ContentDisposition, "filename=\"logo\"")
                                }
                            )
                        }
                    )
                )
            }
        }.map { it.clubToDomain() }
}
