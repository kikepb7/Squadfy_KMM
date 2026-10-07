package com.kikepb.club.domain.repository

import com.kikepb.club.domain.error.ClubError
import com.kikepb.club.domain.model.MemberAbsenceModel
import com.kikepb.core.domain.util.EmptyResult
import com.kikepb.core.domain.util.Result
import kotlinx.datetime.LocalDate

/** Member absences (spec 014, BE-008 RN-C). Network-first (ADR-0006). */
interface AbsenceRepository {
    suspend fun getAbsences(clubId: String, from: LocalDate?, to: LocalDate? = null): Result<List<MemberAbsenceModel>, ClubError>
    suspend fun addMyAbsence(clubId: String, fromDate: LocalDate, toDate: LocalDate, reason: String?): Result<MemberAbsenceModel, ClubError>
    suspend fun deleteMyAbsence(clubId: String, absenceId: String): EmptyResult<ClubError>
}
