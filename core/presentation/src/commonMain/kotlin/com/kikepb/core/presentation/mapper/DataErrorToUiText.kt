package com.kikepb.core.presentation.mapper

import com.kikepb.core.domain.util.DataError
import com.kikepb.core.presentation.util.UiText
import squadfy_app.core.presentation.generated.resources.error_bad_request
import squadfy_app.core.presentation.generated.resources.error_conflict
import squadfy_app.core.presentation.generated.resources.Res.string as RString
import squadfy_app.core.presentation.generated.resources.error_disk_full
import squadfy_app.core.presentation.generated.resources.error_forbidden
import squadfy_app.core.presentation.generated.resources.error_no_internet
import squadfy_app.core.presentation.generated.resources.error_not_found
import squadfy_app.core.presentation.generated.resources.error_payload_too_large
import squadfy_app.core.presentation.generated.resources.error_request_timeout
import squadfy_app.core.presentation.generated.resources.error_serialization
import squadfy_app.core.presentation.generated.resources.error_server
import squadfy_app.core.presentation.generated.resources.error_service_unavailable
import squadfy_app.core.presentation.generated.resources.error_too_many_requests
import squadfy_app.core.presentation.generated.resources.error_unauthorized
import squadfy_app.core.presentation.generated.resources.error_unknown

fun DataError.toUiText(): UiText {
    val resource = when (this) {
        DataError.Local.DISK_FULL -> RString.error_disk_full
        DataError.Local.NOT_FOUND -> RString.error_not_found
        DataError.Local.UNKNOWN -> RString.error_unknown
        DataError.Remote.BAD_REQUEST -> RString.error_bad_request
        DataError.Remote.REQUEST_TIMEOUT -> RString.error_request_timeout
        DataError.Remote.UNAUTHORIZED -> RString.error_unauthorized
        DataError.Remote.FORBIDDEN -> RString.error_forbidden
        DataError.Remote.NOT_FOUND -> RString.error_not_found
        DataError.Remote.CONFLICT -> RString.error_conflict
        DataError.Remote.TOO_MANY_REQUESTS -> RString.error_too_many_requests
        DataError.Remote.NO_INTERNET -> RString.error_no_internet
        DataError.Remote.PAYLOAD_TOO_LARGE -> RString.error_payload_too_large
        DataError.Remote.SERVER_ERROR -> RString.error_server
        DataError.Remote.SERVICE_UNAVAILABLE -> RString.error_service_unavailable
        DataError.Remote.SERIALIZATION -> RString.error_serialization
        DataError.Remote.UNKNOWN -> RString.error_unknown
    }

    return UiText.Resource(id = resource)
}