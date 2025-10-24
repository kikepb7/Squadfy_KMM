package com.kikepb.core.data.networking

import com.kikepb.core.data.networking.UrlConstants.BASE_URL_HTTP
import com.kikepb.core.domain.util.DataError
import com.kikepb.core.domain.util.Result
import io.ktor.client.call.NoTransformationFoundException
import io.ktor.client.call.body
import io.ktor.client.statement.HttpResponse

expect suspend fun <T> platformSafeCall(
    execute: suspend () -> HttpResponse,
    handleResponse: suspend (HttpResponse) -> Result<T, DataError.Remote>
): Result<T, DataError.Remote>

suspend inline fun <reified T> safeCall(noinline execute: suspend () -> HttpResponse): Result<T, DataError.Remote> {
    return platformSafeCall(execute = execute) { response ->
        responseToResult(response = response)
    }
}

suspend inline fun <reified T> responseToResult(response: HttpResponse): Result<T, DataError.Remote> {
    return when (response.status.value) {
        in 200 .. 299 -> {
            try {
                Result.Success(data = response.body<T>())
            } catch (e: NoTransformationFoundException) {
                Result.Failure(error = DataError.Remote.SERIALIZATION)
            }
        }
        400 -> Result.Failure(DataError.Remote.BAD_REQUEST)
        401 -> Result.Failure(DataError.Remote.UNAUTHORIZED)
        403 -> Result.Failure(DataError.Remote.FORBIDDEN)
        404 -> Result.Failure(DataError.Remote.NOT_FOUND)
        408 -> Result.Failure(DataError.Remote.REQUEST_TIMEOUT)
        413 -> Result.Failure(DataError.Remote.PAYLOAD_TOO_LARGE)
        429 -> Result.Failure(DataError.Remote.TOO_MANY_REQUESTS)
        500 -> Result.Failure(DataError.Remote.SERVER_ERROR)
        503 -> Result.Failure(DataError.Remote.SERVICE_UNAVAILABLE)
        else -> Result.Failure(DataError.Remote.UNKNOWN)
    }
}

fun constructRoute(route: String): String {
    return when {
        route.contains(BASE_URL_HTTP) -> route
        route.startsWith("/") -> "${BASE_URL_HTTP}$route"
        else -> "${BASE_URL_HTTP}/$route"
    }
}