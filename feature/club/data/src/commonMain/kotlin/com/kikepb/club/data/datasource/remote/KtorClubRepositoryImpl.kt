package com.kikepb.club.data.datasource.remote

import com.kikepb.club.data.dto.ClubDTO
import com.kikepb.club.data.dto.ClubMemberDTO
import com.kikepb.club.data.dto.request.CreateClubRequestDTO
import com.kikepb.club.data.dto.request.JoinClubRequestDTO
import com.kikepb.club.data.mappers.toDomain
import com.kikepb.club.domain.model.ClubMemberModel
import com.kikepb.club.domain.model.ClubModel
import com.kikepb.club.domain.repository.ClubRepository
import com.kikepb.core.data.networking.get
import com.kikepb.core.data.networking.post
import com.kikepb.core.domain.util.DataError
import com.kikepb.core.domain.util.Result
import com.kikepb.core.domain.util.map
import io.ktor.client.HttpClient

class KtorClubRepositoryImpl(
    private val httpClient: HttpClient
) : ClubRepository {

    override suspend fun getUserClubs(): Result<List<ClubModel>, DataError.Remote> =
        httpClient.get<List<ClubDTO>>(route = "/club").map { clubs ->
            clubs.map { it.toDomain() }
        }

    override suspend fun getClubById(clubId: String): Result<ClubModel, DataError.Remote> =
        httpClient.get<ClubDTO>(route = "/club/$clubId").map { club ->
            club.toDomain()
        }

    override suspend fun getClubMembers(clubId: String): Result<List<ClubMemberModel>, DataError.Remote> =
        httpClient.get<List<ClubMemberDTO>>(route = "/club/$clubId/members").map { members ->
            members.map { it.toDomain() }
        }

    override suspend fun createClub(
        name: String,
        description: String?,
        clubLogoUrl: String?,
        maxMembers: Int?
    ): Result<ClubModel, DataError.Remote> =
        httpClient.post<CreateClubRequestDTO, ClubDTO>(
            route = "/club/create",
            body = CreateClubRequestDTO(
                name = name,
                description = description,
                clubLogoUrl = clubLogoUrl,
                maxMembers = maxMembers
            )
        ).map { it.toDomain() }

    override suspend fun joinClub(
        invitationCode: String,
        shirtNumber: Int?,
        position: String?
    ): Result<ClubModel, DataError.Remote> =
        httpClient.post<JoinClubRequestDTO, ClubDTO>(
            route = "/club/join",
            body = JoinClubRequestDTO(
                invitationCode = invitationCode,
                shirtNumber = shirtNumber,
                position = position
            )
        ).map { it.toDomain() }
}
