package com.kikepb.club.datafirebase.dto

import dev.gitlive.firebase.firestore.Timestamp
import dev.gitlive.firebase.firestore.TimestampSerializer
import kotlinx.serialization.Serializable

/** `clubs/{clubId}` as stored by the Functions (`firebase/functions/src/clubs/model.ts`). */
@Serializable
internal data class ClubDocDto(
    val name: String = "",
    val description: String? = null,
    val logoUrl: String? = null,
    val ownerId: String = "",
    val invitationCode: String = "",
    val maxMembers: Int? = null,
    val membersCount: Int = 0
)

/** What the club callables answer: the backend's ClubDto. */
@Serializable
internal data class ClubResponseDto(
    val id: String,
    val name: String = "",
    val description: String? = null,
    val clubLogoUrl: String? = null,
    val ownerId: String = "",
    val invitationCode: String = "",
    val maxMembers: Int? = null,
    val membersCount: Int = 0
)

/** `clubs/{clubId}/members/{memberId}` and the member callables' answer. */
@Serializable
internal data class MemberDocDto(
    val id: String? = null,
    val clubId: String = "",
    val userId: String = "",
    val username: String = "",
    val profilePictureUrl: String? = null,
    val clubPictureUrl: String? = null,
    val shirtNumber: Int? = null,
    val position: String? = null,
    val role: String = "PLAYER",
    val status: String = "ACTIVE",
    @Serializable(with = TimestampSerializer::class)
    val bannedAt: Timestamp? = null
)

@Serializable
internal data class InvitationCodeDto(val invitationCode: String)

@Serializable internal data class ClubIdRequest(val clubId: String)
@Serializable internal data class MemberRequest(val clubId: String, val memberId: String)
@Serializable internal data class ChangeRoleRequest(val clubId: String, val memberId: String, val role: String)
@Serializable internal data class CreateClubRequest(val name: String, val description: String?, val maxMembers: Int?)
@Serializable internal data class JoinClubRequest(val invitationCode: String, val shirtNumber: Int?, val position: String?)
@Serializable internal data class UpdateClubRequest(val clubId: String, val name: String?, val description: String?, val maxMembers: Int?)
@Serializable internal data class SetClubLogoRequest(val clubId: String, val logoUrl: String)
@Serializable internal data class UpdateMembershipRequest(val clubId: String, val shirtNumber: Int?, val position: String?)
@Serializable internal data class SetClubPictureRequest(val clubId: String, val pictureUrl: String?)
