package com.kikepb.core.firebase.functions

import com.kikepb.core.domain.util.RemoteError
import com.kikepb.core.domain.util.Result
import com.kikepb.core.domain.logger.SquadfyLogger
import com.kikepb.core.firebase.error.toRemoteErrorWithCode
import kotlinx.coroutines.CancellationException
import dev.gitlive.firebase.functions.FirebaseFunctions
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.serializer
import kotlin.time.Duration.Companion.seconds

/**
 * Calls the domain's callable Cloud Functions (`firebase/functions`, spec 019 ADR-0010 §2). Errors keep the backend
 * error code the function sent, so repositories map them exactly as with the own backend.
 *
 * ```
 * functions.call<JoinClubRequest, ClubDto>("joinClub", JoinClubRequest(code))
 * ```
 */
class FunctionsClient(
    val functions: FirebaseFunctions,
    private val logger: SquadfyLogger? = null
) {

    suspend fun <I, O> call(
        name: String,
        input: I,
        inputSerializer: SerializationStrategy<I>,
        outputDeserializer: DeserializationStrategy<O>
    ): Result<O, RemoteError> = try {
        Result.Success(
            functions.httpsCallable(name, timeout = TIMEOUT)
                .invoke(inputSerializer, input)
                .data(outputDeserializer)
        )
    } catch (e: CancellationException) {
        throw e
    } catch (e: Throwable) {
        // Server messages are technical and carry no personal data; the input is never logged
        logger?.warn("Callable $name failed: ${e::class.simpleName}: ${e.message}")
        Result.Failure(e.toRemoteErrorWithCode())
    }

    suspend inline fun <reified I, reified O> call(name: String, input: I): Result<O, RemoteError> =
        call(name, input, serializer<I>(), serializer<O>())

    /** For functions that answer nothing useful (`{ ok: true }`). */
    suspend inline fun <reified I> run(name: String, input: I): Result<Unit, RemoteError> =
        when (val result = call(name, input, serializer<I>(), OkResponse.serializer())) {
            is Result.Success -> Result.Success(Unit)
            is Result.Failure -> result
        }

    companion object {
        /** Must match `firebase/functions/src/config.ts` (europe-west1, close to the users and GDPR-friendly). */
        const val REGION = "europe-west1"
        val TIMEOUT = 30.seconds
    }
}

@kotlinx.serialization.Serializable
data class OkResponse(val ok: Boolean = true)

/** Empty input for callables that take no parameters. */
@kotlinx.serialization.Serializable
data object NoInput
