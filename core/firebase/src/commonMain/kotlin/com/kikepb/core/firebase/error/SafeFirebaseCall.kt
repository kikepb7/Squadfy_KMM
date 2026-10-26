package com.kikepb.core.firebase.error

import com.kikepb.core.domain.util.DataError
import com.kikepb.core.domain.util.RemoteError
import com.kikepb.core.domain.util.Result
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

/**
 * Runs a Firebase call and returns a typed [Result] instead of throwing (spec 019 AC-019-03). Cancellation is always
 * rethrown so structured concurrency keeps working.
 *
 * ```
 * suspend fun leave(chatId: String) = safeFirebaseCall { chats.document(chatId).delete() }
 * ```
 */
suspend inline fun <T> safeFirebaseCall(
    crossinline mapError: (Throwable) -> DataError.Remote = { it.toRemoteError() },
    crossinline block: suspend () -> T
): Result<T, DataError.Remote> {
    return try {
        Result.Success(block())
    } catch (e: CancellationException) {
        throw e
    } catch (e: Throwable) {
        Result.Failure(mapError(e))
    }
}

/** Same as [safeFirebaseCall], keeping the backend error code a Cloud Function sent in `details.code`. */
suspend inline fun <T> safeRemoteCall(crossinline block: suspend () -> T): Result<T, RemoteError> {
    return try {
        Result.Success(block())
    } catch (e: CancellationException) {
        throw e
    } catch (e: Throwable) {
        Result.Failure(e.toRemoteErrorWithCode())
    }
}

/**
 * Wraps a Firebase listener flow (snapshots, auth state…) so errors such as a revoked permission end the stream with
 * a [Result.Failure] instead of crashing the collector.
 */
fun <T> Flow<T>.asResultFlow(): Flow<Result<T, DataError.Remote>> =
    map<T, Result<T, DataError.Remote>> { Result.Success(it) }
        .catch { throwable ->
            if (throwable is CancellationException) throw throwable
            emit(Result.Failure(throwable.toRemoteError()))
        }
