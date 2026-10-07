package com.kikepb.club.domain.repository

import com.kikepb.club.domain.error.ClubError
import com.kikepb.club.domain.model.ClubScheduleModel
import com.kikepb.club.domain.model.ScheduleDraft
import com.kikepb.club.domain.model.ScheduleExceptionModel
import com.kikepb.club.domain.model.ScheduleExceptionType
import com.kikepb.core.domain.util.EmptyResult
import com.kikepb.core.domain.util.Result
import kotlinx.datetime.LocalDate
import kotlin.time.Instant

/** Weekly schedule and its exceptions (spec 004, BE-002 + BE-008). Network-first (ADR-0006). */
interface ScheduleRepository {
    /** `null` when the club has no schedule yet (404). */
    suspend fun getSchedule(clubId: String): Result<ClubScheduleModel?, ClubError>
    suspend fun createSchedule(clubId: String, draft: ScheduleDraft, includeDeadlines: Boolean): Result<ClubScheduleModel, ClubError>
    suspend fun updateSchedule(clubId: String, draft: ScheduleDraft, includeDeadlines: Boolean): Result<ClubScheduleModel, ClubError>

    suspend fun getExceptions(clubId: String): Result<List<ScheduleExceptionModel>, ClubError>
    suspend fun addException(
        clubId: String,
        date: LocalDate,
        type: ScheduleExceptionType,
        newScheduledAt: Instant?,
        reason: String?
    ): Result<ScheduleExceptionModel, ClubError>
    suspend fun deleteException(clubId: String, exceptionId: String): EmptyResult<ClubError>
}
