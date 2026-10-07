package com.kikepb.club.data.mappers

import com.kikepb.club.data.dto.MemberAbsenceDTO
import com.kikepb.club.domain.model.MemberAbsenceModel
import kotlinx.datetime.LocalDate

fun MemberAbsenceDTO.toDomain() = MemberAbsenceModel(
    id = id,
    clubMemberId = clubMemberId,
    fromDate = LocalDate.parse(fromDate),
    toDate = LocalDate.parse(toDate),
    reason = reason
)
