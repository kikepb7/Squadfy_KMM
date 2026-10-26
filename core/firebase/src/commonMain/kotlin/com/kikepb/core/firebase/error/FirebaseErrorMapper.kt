package com.kikepb.core.firebase.error

import com.kikepb.core.domain.util.BackendErrorCode
import com.kikepb.core.domain.util.DataError
import com.kikepb.core.domain.util.DataError.Remote.BAD_REQUEST
import com.kikepb.core.domain.util.DataError.Remote.CONFLICT
import com.kikepb.core.domain.util.DataError.Remote.FORBIDDEN
import com.kikepb.core.domain.util.DataError.Remote.NO_INTERNET
import com.kikepb.core.domain.util.DataError.Remote.REQUEST_TIMEOUT
import com.kikepb.core.domain.util.DataError.Remote.SERIALIZATION
import com.kikepb.core.domain.util.DataError.Remote.SERVER_ERROR
import com.kikepb.core.domain.util.DataError.Remote.SERVICE_UNAVAILABLE
import com.kikepb.core.domain.util.DataError.Remote.TOO_MANY_REQUESTS
import com.kikepb.core.domain.util.DataError.Remote.UNAUTHORIZED
import com.kikepb.core.domain.util.RemoteError
import dev.gitlive.firebase.FirebaseNetworkException
import dev.gitlive.firebase.FirebaseTooManyRequestsException
import dev.gitlive.firebase.auth.FirebaseAuthException
import dev.gitlive.firebase.firestore.FirebaseFirestoreException
import dev.gitlive.firebase.firestore.FirestoreExceptionCode
import dev.gitlive.firebase.firestore.code
import dev.gitlive.firebase.functions.FirebaseFunctionsException
import dev.gitlive.firebase.functions.FunctionsExceptionCode
import dev.gitlive.firebase.functions.code
import dev.gitlive.firebase.functions.details
import kotlinx.serialization.SerializationException
import com.kikepb.core.domain.util.DataError.Remote.NOT_FOUND as REMOTE_NOT_FOUND
import com.kikepb.core.domain.util.DataError.Remote.UNKNOWN as REMOTE_UNKNOWN

/**
 * Translates any exception thrown by the Firebase SDKs into the app's [DataError.Remote], so no Firebase type ever
 * leaks above the `data` layer. Feature-specific errors (e.g. wrong password) are mapped by the feature first.
 */
fun Throwable.toRemoteError(): DataError.Remote = when (this) {
    is FirebaseNetworkException -> NO_INTERNET
    is FirebaseTooManyRequestsException -> TOO_MANY_REQUESTS
    is FirebaseFirestoreException -> code.toRemoteError()
    is FirebaseFunctionsException -> code.toRemoteError()
    is FirebaseAuthException -> UNAUTHORIZED
    is SerializationException -> SERIALIZATION
    is IllegalArgumentException -> BAD_REQUEST
    else -> REMOTE_UNKNOWN
}

/**
 * Like [toRemoteError], plus the backend error code a Cloud Function put in `HttpsError.details.code`
 * (`firebase/functions/src/errors.ts`), so the UI keeps reacting to the same [BackendErrorCode]s as with the own
 * backend (ADR-0010 §5).
 */
fun Throwable.toRemoteErrorWithCode(): RemoteError {
    val code = (this as? FirebaseFunctionsException)?.details?.let(::backendCodeOf)
    return RemoteError(status = toRemoteError(), code = BackendErrorCode.from(code))
}

/** `details` arrives as a map on Android and iOS, and as its string form in some SDK paths. */
internal fun backendCodeOf(details: Any): String? = when (details) {
    is Map<*, *> -> details["code"] as? String
    is String -> CODE_IN_TEXT.find(details)?.groupValues?.get(1)
    else -> CODE_IN_TEXT.find(details.toString())?.groupValues?.get(1)
}

private val CODE_IN_TEXT = Regex("""code["']?\s*[:=]\s*["']?([A-Z_]+)""")

// The exception code enums are typealiases of each platform's enum: their entries can only be qualified
private fun FirestoreExceptionCode.toRemoteError(): DataError.Remote = when (this) {
    FirestoreExceptionCode.INVALID_ARGUMENT, FirestoreExceptionCode.OUT_OF_RANGE -> BAD_REQUEST
    FirestoreExceptionCode.FAILED_PRECONDITION -> BAD_REQUEST
    FirestoreExceptionCode.DEADLINE_EXCEEDED -> REQUEST_TIMEOUT
    FirestoreExceptionCode.NOT_FOUND -> REMOTE_NOT_FOUND
    FirestoreExceptionCode.ALREADY_EXISTS, FirestoreExceptionCode.ABORTED -> CONFLICT
    FirestoreExceptionCode.PERMISSION_DENIED -> FORBIDDEN
    FirestoreExceptionCode.UNAUTHENTICATED -> UNAUTHORIZED
    FirestoreExceptionCode.RESOURCE_EXHAUSTED -> TOO_MANY_REQUESTS
    FirestoreExceptionCode.UNAVAILABLE -> SERVICE_UNAVAILABLE
    FirestoreExceptionCode.INTERNAL, FirestoreExceptionCode.DATA_LOSS, FirestoreExceptionCode.UNIMPLEMENTED -> SERVER_ERROR
    else -> REMOTE_UNKNOWN
}

private fun FunctionsExceptionCode.toRemoteError(): DataError.Remote = when (this) {
    FunctionsExceptionCode.INVALID_ARGUMENT, FunctionsExceptionCode.OUT_OF_RANGE -> BAD_REQUEST
    // HttpsError("failed-precondition") is what the Functions throw for the backend's 409 (state conflicts)
    FunctionsExceptionCode.FAILED_PRECONDITION, FunctionsExceptionCode.ALREADY_EXISTS, FunctionsExceptionCode.ABORTED -> CONFLICT
    FunctionsExceptionCode.DEADLINE_EXCEEDED -> REQUEST_TIMEOUT
    FunctionsExceptionCode.NOT_FOUND -> REMOTE_NOT_FOUND
    FunctionsExceptionCode.PERMISSION_DENIED -> FORBIDDEN
    FunctionsExceptionCode.UNAUTHENTICATED -> UNAUTHORIZED
    FunctionsExceptionCode.RESOURCE_EXHAUSTED -> TOO_MANY_REQUESTS
    FunctionsExceptionCode.UNAVAILABLE -> SERVICE_UNAVAILABLE
    FunctionsExceptionCode.INTERNAL, FunctionsExceptionCode.DATA_LOSS, FunctionsExceptionCode.UNIMPLEMENTED -> SERVER_ERROR
    else -> REMOTE_UNKNOWN
}
