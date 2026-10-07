package com.kikepb.club.domain.model

import kotlinx.datetime.LocalDate

/** A member's absence period (`MemberAbsenceDto`, BE-008 RN-C). Dates are local to the club. */
data class MemberAbsenceModel(
    val id: String,
    val clubMemberId: String,
    val fromDate: LocalDate,
    val toDate: LocalDate,
    val reason: String?
) {
    fun covers(date: LocalDate): Boolean = date in fromDate..toDate
}
