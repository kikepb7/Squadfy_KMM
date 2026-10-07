package com.kikepb.core.presentation.mapper

import com.kikepb.core.domain.util.BackendErrorCode
import com.kikepb.core.domain.util.DataError
import com.kikepb.core.domain.util.RemoteError
import com.kikepb.core.presentation.util.UiText
import squadfy_app.core.presentation.generated.resources.error_banned_from_club
import squadfy_app.core.presentation.generated.resources.error_conflict
import squadfy_app.core.presentation.generated.resources.error_email_not_verified_v1
import squadfy_app.core.presentation.generated.resources.error_no_internet
import squadfy_app.core.presentation.generated.resources.error_rate_limit_v1
import squadfy_app.core.presentation.generated.resources.error_user_exists
import squadfy_app.core.presentation.generated.resources.Res.string as RString
import kotlin.test.Test
import kotlin.test.assertEquals

class RemoteErrorToUiTextTest {

    private fun RemoteError.resourceId() = (toUiText() as UiText.Resource).id

    @Test
    fun `AC-002-06 known codes get their specific text`() {
        assertEquals(RString.error_email_not_verified_v1, RemoteError(DataError.Remote.FORBIDDEN, BackendErrorCode.EMAIL_NOT_VERIFIED).resourceId())
        assertEquals(RString.error_banned_from_club, RemoteError(DataError.Remote.FORBIDDEN, BackendErrorCode.BANNED_FROM_CLUB).resourceId())
        assertEquals(RString.error_rate_limit_v1, RemoteError(DataError.Remote.TOO_MANY_REQUESTS, BackendErrorCode.RATE_LIMIT_EXCEEDED).resourceId())
        assertEquals(RString.error_user_exists, RemoteError(DataError.Remote.CONFLICT, BackendErrorCode.USER_EXISTS).resourceId())
    }

    @Test
    fun `AC-002-06 generic or missing codes fall back to the HTTP status text`() {
        assertEquals(RString.error_conflict, RemoteError(DataError.Remote.CONFLICT, BackendErrorCode.CONFLICT).resourceId())
        assertEquals(RString.error_conflict, RemoteError(DataError.Remote.CONFLICT, BackendErrorCode.UNKNOWN).resourceId())
        assertEquals(RString.error_no_internet, RemoteError(DataError.Remote.NO_INTERNET).resourceId())
    }
}
