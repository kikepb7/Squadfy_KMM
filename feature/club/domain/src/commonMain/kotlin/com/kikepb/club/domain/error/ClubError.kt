package com.kikepb.club.domain.error

import com.kikepb.core.domain.util.BackendErrorCode
import com.kikepb.core.domain.util.DataError
import com.kikepb.core.domain.util.Error
import com.kikepb.core.domain.util.RemoteError

/** Club errors the UI reacts to specifically (spec 003, APP-RN-07). Anything else stays a [Remote]. */
sealed interface ClubError : Error {
    data object InvalidInvitationCode : ClubError
    data object BannedFromClub : ClubError
    /** 409 on join: the backend uses the same `CONFLICT` code for "already a member" and "club full". */
    data object AlreadyMemberOrClubFull : ClubError
    data object NotClubMember : ClubError
    data object Forbidden : ClubError
    /** 400 on edit: `maxMembers` below the current number of members (BE-001 RN-13). */
    data object MaxMembersBelowCurrent : ClubError
    data object NotFound : ClubError
    data class Remote(val error: RemoteError) : ClubError
}

/** The same HTTP status means different things depending on the operation. */
enum class ClubOperation { JOIN, EDIT_CLUB, OTHER }

fun RemoteError.toClubError(operation: ClubOperation = ClubOperation.OTHER): ClubError = when {
    code == BackendErrorCode.INVALID_INVITATION_CODE -> ClubError.InvalidInvitationCode
    code == BackendErrorCode.BANNED_FROM_CLUB -> ClubError.BannedFromClub
    code == BackendErrorCode.NOT_CLUB_MEMBER -> ClubError.NotClubMember
    operation == ClubOperation.JOIN && status == DataError.Remote.CONFLICT -> ClubError.AlreadyMemberOrClubFull
    operation == ClubOperation.EDIT_CLUB && code == BackendErrorCode.BAD_REQUEST -> ClubError.MaxMembersBelowCurrent
    status == DataError.Remote.FORBIDDEN -> ClubError.Forbidden
    status == DataError.Remote.NOT_FOUND -> ClubError.NotFound
    else -> ClubError.Remote(this)
}
