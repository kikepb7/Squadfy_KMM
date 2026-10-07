package com.kikepb.club.domain.usecase

import com.kikepb.club.domain.error.ClubError
import com.kikepb.club.domain.model.MemberAbsenceModel
import com.kikepb.club.domain.repository.AbsenceRepository
import com.kikepb.core.domain.util.EmptyResult
import com.kikepb.core.domain.util.Error
import com.kikepb.core.domain.util.Result
import com.kikepb.core.domain.util.mapError
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.plus

class GetAbsencesUseCase(private val repository: AbsenceRepository) {
    /** AC-014-01: current and future absences, by start date. */
    suspend operator fun invoke(clubId: String, from: LocalDate): Result<List<MemberAbsenceModel>, ClubError> =
        when (val result = repository.getAbsences(clubId, from)) {
            is Result.Success -> Result.Success(result.data.sortedBy { it.fromDate })
            is Result.Failure -> result
        }
}

enum class AbsenceValidationError { END_BEFORE_START, ENDS_IN_PAST, TOO_LONG, REASON_TOO_LONG }

sealed interface AddAbsenceError : Error {
    data class Invalid(val reason: AbsenceValidationError) : AddAbsenceError
    data class Remote(val error: ClubError) : AddAbsenceError
}

class AddMyAbsenceUseCase(private val repository: AbsenceRepository) {
    suspend operator fun invoke(clubId: String, fromDate: LocalDate, toDate: LocalDate, reason: String?, today: LocalDate): Result<MemberAbsenceModel, AddAbsenceError> {
        val cleanReason = reason?.trim()?.takeIf { it.isNotEmpty() }
        validate(fromDate, toDate, cleanReason, today)?.let { return Result.Failure(AddAbsenceError.Invalid(it)) }
        return repository.addMyAbsence(clubId, fromDate, toDate, cleanReason).mapError { AddAbsenceError.Remote(it) }
    }

    companion object {
        const val MAX_REASON_LENGTH = 200

        /** AC-014-02: from ≤ to, to ≥ today and at most one year long. */
        fun validate(fromDate: LocalDate, toDate: LocalDate, reason: String?, today: LocalDate): AbsenceValidationError? = when {
            toDate < fromDate -> AbsenceValidationError.END_BEFORE_START
            toDate < today -> AbsenceValidationError.ENDS_IN_PAST
            toDate > fromDate.plus(DatePeriod(years = 1)) -> AbsenceValidationError.TOO_LONG
            (reason?.length ?: 0) > MAX_REASON_LENGTH -> AbsenceValidationError.REASON_TOO_LONG
            else -> null
        }
    }
}

class DeleteMyAbsenceUseCase(private val repository: AbsenceRepository) {
    suspend operator fun invoke(clubId: String, absenceId: String): EmptyResult<ClubError> = repository.deleteMyAbsence(clubId, absenceId)
}

/** AC-005-15 / AC-014-05: one of my absences covers the match day (it never blocks signing up). */
fun List<MemberAbsenceModel>.coversMe(myMemberId: String?, date: LocalDate): Boolean =
    myMemberId != null && any { it.clubMemberId == myMemberId && it.covers(date) }
