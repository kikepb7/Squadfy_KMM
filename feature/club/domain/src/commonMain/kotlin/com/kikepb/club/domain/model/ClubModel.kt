package com.kikepb.club.domain.model

/** Club as exposed by the backend v1 (`ClubDto`). The weekly schedule lives in its own resource (spec 004). */
data class ClubModel(
    val id: String,
    val name: String,
    val description: String?,
    val clubLogoUrl: String?,
    val ownerId: String,
    val invitationCode: String,
    val maxMembers: Int?,
    val membersCount: Int
)

/** A club in "my clubs" with my role when the members are cached (APP-RN-04). */
data class MyClubModel(
    val club: ClubModel,
    val myRole: ClubMemberRole?
)
