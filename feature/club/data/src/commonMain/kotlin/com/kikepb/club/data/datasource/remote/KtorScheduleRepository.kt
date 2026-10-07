package com.kikepb.club.data.datasource.remote

import com.kikepb.club.data.dto.AddScheduleExceptionRequestDTO
import com.kikepb.club.data.dto.ClubMatchScheduleDTO
import com.kikepb.club.data.dto.ScheduleExceptionDTO
import com.kikepb.club.data.dto.ScheduleRequestDTO
import com.kikepb.club.data.mappers.toDomain
import com.kikepb.club.data.mappers.toRequest
import com.kikepb.club.domain.error.ClubError
import com.kikepb.club.domain.error.toClubError
import com.kikepb.club.domain.model.ClubScheduleModel
import com.kikepb.club.domain.model.ScheduleDraft
import com.kikepb.club.domain.model.ScheduleExceptionModel
import com.kikepb.club.domain.model.ScheduleExceptionType
import com.kikepb.club.domain.repository.ScheduleRepository
import com.kikepb.core.data.networking.apiDelete
import com.kikepb.core.data.networking.apiGet
import com.kikepb.core.data.networking.apiPatch
import com.kikepb.core.data.networking.apiPost
import com.kikepb.core.domain.util.DataError
import com.kikepb.core.domain.util.EmptyResult
import com.kikepb.core.domain.util.Result
import com.kikepb.core.domain.util.map
import com.kikepb.core.domain.util.mapError
import io.ktor.client.HttpClient
import kotlinx.datetime.LocalDate
import kotlin.time.Instant

/** Network-first schedule (ADR-0006): the last loaded value per club is kept in memory for offline display. */
class KtorScheduleRepository(private val httpClient: HttpClient) : ScheduleRepository {

    private val lastKnown = mutableMapOf<String, ClubScheduleModel?>()

    override suspend fun getSchedule(clubId: String): Result<ClubScheduleModel?, ClubError> =
        when (val result = httpClient.apiGet<ClubMatchScheduleDTO>(route = scheduleRoute(clubId))) {
            is Result.Success -> Result.Success(result.data.toDomain().also { lastKnown[clubId] = it })
            is Result.Failure -> when {
                result.error.status == DataError.Remote.NOT_FOUND -> Result.Success(null).also { lastKnown[clubId] = null }
                clubId in lastKnown && result.error.status == DataError.Remote.NO_INTERNET -> Result.Success(lastKnown[clubId])
                else -> Result.Failure(result.error.toClubError())
            }
        }

    override suspend fun createSchedule(clubId: String, draft: ScheduleDraft, includeDeadlines: Boolean): Result<ClubScheduleModel, ClubError> =
        httpClient.apiPost<ScheduleRequestDTO, ClubMatchScheduleDTO>(route = scheduleRoute(clubId), body = draft.toRequest(includeDeadlines))
            .mapError { it.toClubError() }
            .map { dto -> dto.toDomain().also { lastKnown[clubId] = it } }

    override suspend fun updateSchedule(clubId: String, draft: ScheduleDraft, includeDeadlines: Boolean): Result<ClubScheduleModel, ClubError> =
        httpClient.apiPatch<ScheduleRequestDTO, ClubMatchScheduleDTO>(route = scheduleRoute(clubId), body = draft.toRequest(includeDeadlines))
            .mapError { it.toClubError() }
            .map { dto -> dto.toDomain().also { lastKnown[clubId] = it } }

    override suspend fun getExceptions(clubId: String): Result<List<ScheduleExceptionModel>, ClubError> =
        httpClient.apiGet<List<ScheduleExceptionDTO>>(route = "${scheduleRoute(clubId)}/exceptions")
            .mapError { it.toClubError() }
            .map { exceptions -> exceptions.map { it.toDomain() }.sortedBy { it.date } }

    override suspend fun addException(
        clubId: String,
        date: LocalDate,
        type: ScheduleExceptionType,
        newScheduledAt: Instant?,
        reason: String?
    ): Result<ScheduleExceptionModel, ClubError> =
        httpClient.apiPost<AddScheduleExceptionRequestDTO, ScheduleExceptionDTO>(
            route = "${scheduleRoute(clubId)}/exceptions",
            body = AddScheduleExceptionRequestDTO(date = date.toString(), type = type.name, newScheduledAt = newScheduledAt?.toString(), reason = reason)
        )
            .mapError { it.toClubError() }
            .map { it.toDomain() }

    override suspend fun deleteException(clubId: String, exceptionId: String): EmptyResult<ClubError> =
        httpClient.apiDelete<Unit>(route = "${scheduleRoute(clubId)}/exceptions/$exceptionId").mapError { it.toClubError() }

    private fun scheduleRoute(clubId: String) = "/clubs/$clubId/schedule"
}
