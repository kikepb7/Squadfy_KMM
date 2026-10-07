package com.kikepb.club.domain.error

import com.kikepb.core.domain.util.BackendErrorCode
import com.kikepb.core.domain.util.DataError
import com.kikepb.core.domain.util.RemoteError
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class ClubErrorTest {

    @Test
    fun `AC-003-04 join errors are typed from the backend code and status`() {
        assertEquals(
            ClubError.InvalidInvitationCode,
            RemoteError(DataError.Remote.BAD_REQUEST, BackendErrorCode.INVALID_INVITATION_CODE).toClubError(ClubOperation.JOIN)
        )
        assertEquals(
            ClubError.BannedFromClub,
            RemoteError(DataError.Remote.FORBIDDEN, BackendErrorCode.BANNED_FROM_CLUB).toClubError(ClubOperation.JOIN)
        )
        assertEquals(
            ClubError.AlreadyMemberOrClubFull,
            RemoteError(DataError.Remote.CONFLICT, BackendErrorCode.CONFLICT).toClubError(ClubOperation.JOIN)
        )
    }

    @Test
    fun `AC-003-11 a business 400 when editing the club means maxMembers below current members`() {
        assertEquals(
            ClubError.MaxMembersBelowCurrent,
            RemoteError(DataError.Remote.BAD_REQUEST, BackendErrorCode.BAD_REQUEST).toClubError(ClubOperation.EDIT_CLUB)
        )
        // The same error outside that operation is not reinterpreted
        assertIs<ClubError.Remote>(RemoteError(DataError.Remote.BAD_REQUEST, BackendErrorCode.BAD_REQUEST).toClubError())
    }

    @Test
    fun `AC-003-07 permission and membership errors`() {
        assertEquals(ClubError.Forbidden, RemoteError(DataError.Remote.FORBIDDEN, BackendErrorCode.FORBIDDEN).toClubError())
        assertEquals(ClubError.NotClubMember, RemoteError(DataError.Remote.FORBIDDEN, BackendErrorCode.NOT_CLUB_MEMBER).toClubError())
        assertEquals(ClubError.NotFound, RemoteError(DataError.Remote.NOT_FOUND, BackendErrorCode.NOT_FOUND).toClubError())
        assertIs<ClubError.Remote>(RemoteError(DataError.Remote.NO_INTERNET).toClubError())
    }
}
