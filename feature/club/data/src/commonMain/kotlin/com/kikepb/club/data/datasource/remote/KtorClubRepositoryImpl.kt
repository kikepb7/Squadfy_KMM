package com.kikepb.club.data.datasource.remote

import com.kikepb.club.data.dto.ClubDTO
import com.kikepb.club.data.dto.ClubMemberDTO
import com.kikepb.club.data.mappers.clubToDomain
import com.kikepb.club.data.mappers.clubMemberToDomain
import com.kikepb.club.domain.model.ClubMemberModel
import com.kikepb.club.domain.model.ClubModel
import com.kikepb.club.domain.repository.ClubRepository
import com.kikepb.core.data.networking.get
import com.kikepb.core.domain.util.DataError
import com.kikepb.core.domain.util.Result
import com.kikepb.core.domain.util.map
import io.ktor.client.HttpClient

class KtorClubRepositoryImpl(
    private val httpClient: HttpClient
) : ClubRepository {

    override suspend fun getClubById(clubId: String): Result<ClubModel, DataError.Remote> =
        httpClient.get<ClubDTO>(route = "/club/$clubId").map { club ->
            club.clubToDomain()
        }

    override suspend fun getClubMembers(clubId: String): Result<List<ClubMemberModel>, DataError.Remote> =
        httpClient.get<List<ClubMemberDTO>>(route = "/club/$clubId/members").map { members ->
            members.map { it.clubMemberToDomain() }
        }
}