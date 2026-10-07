package com.kikepb.club.data.datasource.remote

import com.kikepb.club.data.dto.CreateMemberAbsenceRequestDTO
import com.kikepb.club.data.dto.MemberAbsenceDTO
import com.kikepb.club.data.mappers.toDomain
import com.kikepb.club.domain.error.ClubError
import com.kikepb.club.domain.error.toClubError
import com.kikepb.club.domain.model.MemberAbsenceModel
import com.kikepb.club.domain.repository.AbsenceRepository
import com.kikepb.core.data.networking.apiDelete
import com.kikepb.core.data.networking.apiGet
import com.kikepb.core.data.networking.apiPost
import com.kikepb.core.domain.util.EmptyResult
import com.kikepb.core.domain.util.Result
import com.kikepb.core.domain.util.map
import com.kikepb.core.domain.util.mapError
import io.ktor.client.HttpClient
import kotlinx.datetime.LocalDate

class KtorAbsenceRepository(private val httpClient: HttpClient) : AbsenceRepository {

    override suspend fun getAbsences(clubId: String, from: LocalDate?, to: LocalDate?): Result<List<MemberAbsenceModel>, ClubError> =
        httpClient.apiGet<List<MemberAbsenceDTO>>(
            route = "/clubs/$clubId/absences",
            queryParams = buildMap {
                from?.let { put("from", it.toString()) }
                to?.let { put("to", it.toString()) }
            }
        ).mapError { it.toClubError() }.map { list -> list.map { it.toDomain() } }

    override suspend fun addMyAbsence(clubId: String, fromDate: LocalDate, toDate: LocalDate, reason: String?): Result<MemberAbsenceModel, ClubError> =
        httpClient.apiPost<CreateMemberAbsenceRequestDTO, MemberAbsenceDTO>(
            route = "/clubs/$clubId/members/me/absences",
            body = CreateMemberAbsenceRequestDTO(fromDate = fromDate.toString(), toDate = toDate.toString(), reason = reason)
        ).mapError { it.toClubError() }.map { it.toDomain() }

    override suspend fun deleteMyAbsence(clubId: String, absenceId: String): EmptyResult<ClubError> =
        httpClient.apiDelete<Unit>(route = "/clubs/$clubId/members/me/absences/$absenceId").mapError { it.toClubError() }
}
