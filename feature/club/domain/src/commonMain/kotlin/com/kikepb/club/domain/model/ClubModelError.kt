package com.kikepb.club.domain.model

import com.kikepb.club.domain.error.ClubError
import com.kikepb.core.domain.util.Error

sealed interface CreateClubError : Error {
    data object BlankName : CreateClubError
    data object NameTooLong : CreateClubError
    data object DescriptionTooLong : CreateClubError
    data object InvalidMaxMembers : CreateClubError
    data class Remote(val error: ClubError) : CreateClubError
}

sealed interface JoinClubError : Error {
    data object InvalidInvitationCodeFormat : JoinClubError
    data object InvalidShirtNumber : JoinClubError
    data class Remote(val error: ClubError) : JoinClubError
}

sealed interface EditClubError : Error {
    data object BlankName : EditClubError
    data object NameTooLong : EditClubError
    data object DescriptionTooLong : EditClubError
    data object InvalidMaxMembers : EditClubError
    data class Remote(val error: ClubError) : EditClubError
}

/** Result of creating a club: the club exists even if the optional logo upload failed (AC-003-03). */
data class CreatedClub(
    val club: ClubModel,
    val logoUploadFailed: Boolean
)
