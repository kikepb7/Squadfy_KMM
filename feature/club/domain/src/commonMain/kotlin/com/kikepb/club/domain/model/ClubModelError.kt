package com.kikepb.club.domain.model

import com.kikepb.core.domain.util.DataError
import com.kikepb.core.domain.util.Error

sealed interface CreateClubError : Error {
    data object BlankName : CreateClubError
    data object NameTooLong : CreateClubError
    data object InvalidMaxMembers : CreateClubError
    data class Remote(val dataError: DataError.Remote) : CreateClubError
}

sealed interface JoinClubError : Error {
    data object InvalidInvitationCodeFormat : JoinClubError
    data object InvalidShirtNumber : JoinClubError
    data class Remote(val dataError: DataError.Remote) : JoinClubError
}