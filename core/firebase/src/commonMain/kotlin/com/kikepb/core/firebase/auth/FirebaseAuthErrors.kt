package com.kikepb.core.firebase.auth

import com.kikepb.core.domain.util.DataError
import com.kikepb.core.firebase.error.toRemoteError
import dev.gitlive.firebase.auth.FirebaseAuthActionCodeException
import dev.gitlive.firebase.auth.FirebaseAuthInvalidCredentialsException
import dev.gitlive.firebase.auth.FirebaseAuthInvalidUserException
import dev.gitlive.firebase.auth.FirebaseAuthRecentLoginRequiredException
import dev.gitlive.firebase.auth.FirebaseAuthUserCollisionException
import dev.gitlive.firebase.auth.FirebaseAuthWeakPasswordException

/**
 * Firebase Auth errors as the statuses the auth screens already react to with the own backend (spec 019 AC-019-04):
 * wrong credentials or an invalid/expired link → UNAUTHORIZED, existing account → CONFLICT, weak password → BAD_REQUEST.
 */
internal fun Throwable.toAuthError(): DataError.Remote = when (this) {
    is FirebaseAuthWeakPasswordException -> DataError.Remote.BAD_REQUEST
    is FirebaseAuthInvalidCredentialsException -> DataError.Remote.UNAUTHORIZED
    is FirebaseAuthInvalidUserException -> DataError.Remote.UNAUTHORIZED
    is FirebaseAuthActionCodeException -> DataError.Remote.UNAUTHORIZED
    is FirebaseAuthRecentLoginRequiredException -> DataError.Remote.UNAUTHORIZED
    is FirebaseAuthUserCollisionException -> DataError.Remote.CONFLICT
    else -> toRemoteError()
}
