package com.kikepb.club.domain.model

data class ClubScheduleExceptionModel(
    val id: String,
    val clubId: String,
    val date: String,
    val reason: String?
)
