package com.kikepb.club.domain.usecase

import com.kikepb.club.domain.error.ClubError
import com.kikepb.club.domain.model.ClubScheduleModel
import com.kikepb.club.domain.model.ScheduleDraft
import com.kikepb.club.domain.model.ScheduleExceptionModel
import com.kikepb.club.domain.model.ScheduleExceptionType
import com.kikepb.club.domain.policy.ScheduleValidation
import com.kikepb.club.domain.policy.ScheduleValidationError
import com.kikepb.club.domain.repository.ScheduleRepository
import com.kikepb.core.domain.util.EmptyResult
import com.kikepb.core.domain.util.Error
import com.kikepb.core.domain.util.Result
import com.kikepb.core.domain.util.mapError
import kotlinx.datetime.LocalDate
import kotlin.time.Instant

sealed interface SaveScheduleError : Error {
    data class Invalid(val reason: ScheduleValidationError) : SaveScheduleError
    data class Remote(val error: ClubError) : SaveScheduleError
}

class GetScheduleUseCase(private val scheduleRepository: ScheduleRepository) {
    suspend operator fun invoke(clubId: String): Result<ClubScheduleModel?, ClubError> = scheduleRepository.getSchedule(clubId)
}

/**
 * Creates the schedule the first time (the backend plans the first match at once, BE-002 RN-3) or updates it.
 * Close and draw times are only sent when the [customDeadlines] feature is on (flag `CUSTOM_DRAW_TIME`).
 */
class SaveScheduleUseCase(private val scheduleRepository: ScheduleRepository) {
    suspend operator fun invoke(
        clubId: String,
        draft: ScheduleDraft,
        exists: Boolean,
        customDeadlines: Boolean
    ): Result<ClubScheduleModel, SaveScheduleError> {
        ScheduleValidation.validate(draft, customDeadlines)?.let { return Result.Failure(SaveScheduleError.Invalid(it)) }
        val result = if (exists) {
            scheduleRepository.updateSchedule(clubId, draft, includeDeadlines = customDeadlines)
        } else {
            scheduleRepository.createSchedule(clubId, draft, includeDeadlines = customDeadlines)
        }
        return result.mapError { SaveScheduleError.Remote(it) }
    }
}

class GetScheduleExceptionsUseCase(private val scheduleRepository: ScheduleRepository) {
    suspend operator fun invoke(clubId: String): Result<List<ScheduleExceptionModel>, ClubError> =
        scheduleRepository.getExceptions(clubId)
}

class AddScheduleExceptionUseCase(private val scheduleRepository: ScheduleRepository) {
    suspend operator fun invoke(
        clubId: String,
        date: LocalDate,
        type: ScheduleExceptionType,
        newScheduledAt: Instant?,
        reason: String?
    ): Result<ScheduleExceptionModel, ClubError> = scheduleRepository.addException(
        clubId = clubId,
        date = date,
        type = type,
        newScheduledAt = newScheduledAt.takeIf { type == ScheduleExceptionType.RESCHEDULED },
        reason = reason?.trim()?.ifBlank { null }
    )
}

class DeleteScheduleExceptionUseCase(private val scheduleRepository: ScheduleRepository) {
    suspend operator fun invoke(clubId: String, exceptionId: String): EmptyResult<ClubError> =
        scheduleRepository.deleteException(clubId, exceptionId)
}
