package com.kikepb.club.presentation.fake

import com.kikepb.club.domain.error.ClubError
import com.kikepb.club.domain.model.ClubScheduleModel
import com.kikepb.club.domain.model.ScheduleDraft
import com.kikepb.club.domain.model.ScheduleExceptionModel
import com.kikepb.club.domain.model.ScheduleExceptionType
import com.kikepb.club.domain.repository.ScheduleRepository
import com.kikepb.core.domain.util.EmptyResult
import com.kikepb.core.domain.util.Result
import kotlinx.datetime.LocalDate
import kotlin.time.Clock
import kotlin.time.Instant

/** Read-only schedule source for screens that only need the club time zone. */
class StubScheduleRepository(private val schedule: ClubScheduleModel? = null) : ScheduleRepository {
    override suspend fun getSchedule(clubId: String): Result<ClubScheduleModel?, ClubError> = Result.Success(schedule)
    override suspend fun createSchedule(clubId: String, draft: ScheduleDraft, includeDeadlines: Boolean) = TODO("not used")
    override suspend fun updateSchedule(clubId: String, draft: ScheduleDraft, includeDeadlines: Boolean) = TODO("not used")
    override suspend fun getExceptions(clubId: String): Result<List<ScheduleExceptionModel>, ClubError> = Result.Success(emptyList())
    override suspend fun addException(clubId: String, date: LocalDate, type: ScheduleExceptionType, newScheduledAt: Instant?, reason: String?) =
        TODO("not used")
    override suspend fun deleteException(clubId: String, exceptionId: String): EmptyResult<ClubError> = Result.Success(Unit)
}

class FixedTestClock(private val instant: Instant) : Clock {
    override fun now(): Instant = instant
}
