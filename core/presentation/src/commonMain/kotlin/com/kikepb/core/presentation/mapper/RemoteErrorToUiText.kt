package com.kikepb.core.presentation.mapper

import com.kikepb.core.domain.util.BackendErrorCode
import com.kikepb.core.domain.util.RemoteError
import com.kikepb.core.presentation.util.UiText
import squadfy_app.core.presentation.generated.resources.error_banned_from_club
import squadfy_app.core.presentation.generated.resources.error_email_not_verified_v1
import squadfy_app.core.presentation.generated.resources.error_invalid_credentials_v1
import squadfy_app.core.presentation.generated.resources.error_invalid_invitation_code
import squadfy_app.core.presentation.generated.resources.error_invalid_profile_picture
import squadfy_app.core.presentation.generated.resources.error_not_club_member
import squadfy_app.core.presentation.generated.resources.error_rate_limit_v1
import squadfy_app.core.presentation.generated.resources.error_same_password_v1
import squadfy_app.core.presentation.generated.resources.error_session_expired
import squadfy_app.core.presentation.generated.resources.error_storage
import squadfy_app.core.presentation.generated.resources.error_user_exists
import squadfy_app.core.presentation.generated.resources.error_validation
import squadfy_app.core.presentation.generated.resources.Res.string as RString

/**
 * APP-RN-07: the text depends on the backend `code` (never on its English `message`);
 * codes without a specific text fall back to the generic text of the HTTP status.
 */
fun RemoteError.toUiText(): UiText {
    val resource = when (code) {
        BackendErrorCode.VALIDATION_ERROR, BackendErrorCode.INVALID_REQUEST -> RString.error_validation
        BackendErrorCode.INVALID_CREDENTIALS -> RString.error_invalid_credentials_v1
        BackendErrorCode.EMAIL_NOT_VERIFIED -> RString.error_email_not_verified_v1
        BackendErrorCode.INVALID_TOKEN -> RString.error_session_expired
        BackendErrorCode.RATE_LIMIT_EXCEEDED -> RString.error_rate_limit_v1
        BackendErrorCode.USER_EXISTS -> RString.error_user_exists
        BackendErrorCode.SAME_PASSWORD -> RString.error_same_password_v1
        BackendErrorCode.NOT_CLUB_MEMBER -> RString.error_not_club_member
        BackendErrorCode.BANNED_FROM_CLUB -> RString.error_banned_from_club
        BackendErrorCode.INVALID_INVITATION_CODE -> RString.error_invalid_invitation_code
        BackendErrorCode.INVALID_PROFILE_PICTURE -> RString.error_invalid_profile_picture
        BackendErrorCode.STORAGE_ERROR -> RString.error_storage
        else -> return status.toUiText()
    }
    return UiText.Resource(id = resource)
}
