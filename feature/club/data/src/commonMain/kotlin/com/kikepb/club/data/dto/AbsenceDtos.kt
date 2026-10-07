package com.kikepb.club.data.dto

import kotlinx.serialization.Serializable

/** `MemberAbsenceDto` (BE-008). Dates are ISO `yyyy-MM-dd`. */
@Serializable
data class MemberAbsenceDTO(
    val id: String,
    val clubId: String? = null,
    val clubMemberId: String,
    val fromDate: String,
    val toDate: String,
    val reason: String? = null,
    val createdAt: String? = null
)

@Serializable
data class CreateMemberAbsenceRequestDTO(
    val fromDate: String,
    val toDate: String,
    val reason: String? = null
)
