package com.kikepb.core.domain.util

/**
 * Error returned by the backend API v1 (`{ code, message }` or `{ code: VALIDATION_ERROR, errors }`).
 * [code] is null when the response had no parsable body (e.g. 401 without token, HTML 5xx, network failure).
 * UI decides on [code] + [status], never on [messages] (English and only indicative, APP-RN-07).
 */
data class RemoteError(
    val status: DataError.Remote,
    val code: BackendErrorCode? = null,
    val messages: List<String> = emptyList()
) : Error

/** Error codes of `Squadfy_Backend/docs/BACKEND.md` §11. */
enum class BackendErrorCode {
    VALIDATION_ERROR,
    INVALID_REQUEST,
    BAD_REQUEST,
    INVALID_INVITATION_CODE,
    INVALID_CHAT_SIZE,
    INVALID_PROFILE_PICTURE,
    INVALID_DEVICE_TOKEN,
    INVALID_CREDENTIALS,
    INVALID_TOKEN,
    FORBIDDEN,
    NOT_CLUB_MEMBER,
    BANNED_FROM_CLUB,
    EMAIL_NOT_VERIFIED,
    NOT_FOUND,
    USER_NOT_FOUND,
    CONFLICT,
    USER_EXISTS,
    SAME_PASSWORD,
    RATE_LIMIT_EXCEEDED,
    STORAGE_ERROR,
    UNKNOWN;

    companion object {
        /** `USER_EXITS` is the backend's historical spelling of `USER_EXISTS` (BE-GAP-8). */
        fun from(raw: String?): BackendErrorCode? = when {
            raw.isNullOrBlank() -> null
            raw == "USER_EXITS" -> USER_EXISTS
            else -> entries.firstOrNull { it.name == raw } ?: UNKNOWN
        }
    }
}
