package com.kikepb.club.datafirebase.util

import com.kikepb.club.domain.error.ClubError
import com.kikepb.club.domain.error.ClubOperation
import com.kikepb.club.domain.error.toClubError
import com.kikepb.core.domain.util.DataError
import com.kikepb.core.domain.util.EmptyResult
import com.kikepb.core.domain.util.RemoteError
import com.kikepb.core.domain.util.Result
import com.kikepb.core.firebase.error.toRemoteError
import kotlinx.coroutines.CancellationException

/** Callable results keep the backend codes, so the club errors are mapped exactly as with the own backend. */
internal fun <T> Result<T, RemoteError>.asClubResult(operation: ClubOperation = ClubOperation.OTHER): Result<T, ClubError> =
    when (this) {
        is Result.Success -> this
        is Result.Failure -> Result.Failure(error.toClubError(operation))
    }

internal fun <T> Result<T, RemoteError>.asEmptyClubResult(operation: ClubOperation = ClubOperation.OTHER): EmptyResult<ClubError> =
    when (this) {
        is Result.Success -> Result.Success(Unit)
        is Result.Failure -> Result.Failure(error.toClubError(operation))
    }

/**
 * Reads denied by the rules mean the user is no longer a member (left, removed or banned): the screens react to
 * [ClubError.NotClubMember] as they did to the own backend's 403 NOT_CLUB_MEMBER.
 */
internal suspend inline fun <T> clubRead(crossinline block: suspend () -> T): Result<T, ClubError> = try {
    Result.Success(block())
} catch (e: CancellationException) {
    throw e
} catch (e: Throwable) {
    Result.Failure(
        when (val status = e.toRemoteError()) {
            DataError.Remote.FORBIDDEN -> ClubError.NotClubMember
            DataError.Remote.NOT_FOUND -> ClubError.NotFound
            else -> ClubError.Remote(RemoteError(status = status))
        }
    )
}
