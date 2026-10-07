package com.kikepb.core.data.networking

import com.kikepb.core.data.networking.UrlConstants.BASE_URL_HTTP
import com.kikepb.core.domain.util.BackendErrorCode
import com.kikepb.core.domain.util.DataError
import com.kikepb.core.domain.util.RemoteError
import com.kikepb.core.domain.util.Result
import com.kikepb.core.domain.util.mapError
import io.ktor.client.HttpClient
import io.ktor.client.call.NoTransformationFoundException
import io.ktor.client.call.body
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.delete
import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.client.request.url
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

expect suspend fun <T> platformSafeCall(
    execute: suspend () -> HttpResponse,
    handleResponse: suspend (HttpResponse) -> Result<T, DataError.Remote>
): Result<T, DataError.Remote>

/** Backend API v1 error body (`BACKEND.md` §11). */
@Serializable
data class ErrorResponseDto(
    val code: String? = null,
    val message: String? = null,
    val errors: List<String> = emptyList()
)

@PublishedApi
internal val errorJson = Json { ignoreUnknownKeys = true }

// region API v1 helpers: return the backend error code (spec 002). New repositories should use these.

suspend inline fun <reified Request, reified Response : Any> HttpClient.apiPost(
    route: String,
    body: Request,
    queryParams: Map<String, Any> = mapOf(),
    crossinline builder: HttpRequestBuilder.() -> Unit = {}
): Result<Response, RemoteError> = apiSafeCall {
    post {
        url(constructRoute(route))
        queryParams.forEach { (key, value) -> parameter(key, value) }
        setBody(body)
        builder()
    }
}

suspend inline fun <reified Response : Any> HttpClient.apiGet(
    route: String,
    queryParams: Map<String, Any> = mapOf(),
    crossinline builder: HttpRequestBuilder.() -> Unit = {}
): Result<Response, RemoteError> = apiSafeCall {
    get {
        url(constructRoute(route))
        queryParams.forEach { (key, value) -> parameter(key, value) }
        builder()
    }
}

suspend inline fun <reified Request, reified Response : Any> HttpClient.apiPut(
    route: String,
    body: Request,
    queryParams: Map<String, Any> = mapOf(),
    crossinline builder: HttpRequestBuilder.() -> Unit = {}
): Result<Response, RemoteError> = apiSafeCall {
    put {
        url(constructRoute(route))
        queryParams.forEach { (key, value) -> parameter(key, value) }
        setBody(body)
        builder()
    }
}

suspend inline fun <reified Request, reified Response : Any> HttpClient.apiPatch(
    route: String,
    body: Request,
    queryParams: Map<String, Any> = mapOf(),
    crossinline builder: HttpRequestBuilder.() -> Unit = {}
): Result<Response, RemoteError> = apiSafeCall {
    patch {
        url(constructRoute(route))
        queryParams.forEach { (key, value) -> parameter(key, value) }
        setBody(body)
        builder()
    }
}

suspend inline fun <reified Response : Any> HttpClient.apiDelete(
    route: String,
    queryParams: Map<String, Any> = mapOf(),
    crossinline builder: HttpRequestBuilder.() -> Unit = {}
): Result<Response, RemoteError> = apiSafeCall {
    delete {
        url(constructRoute(route))
        queryParams.forEach { (key, value) -> parameter(key, value) }
        builder()
    }
}

suspend inline fun <reified Response : Any> HttpClient.apiPostMultipart(
    route: String,
    content: MultiPartFormDataContent
): Result<Response, RemoteError> = apiSafeCall {
    post {
        url(constructRoute(route))
        setBody(content)
    }
}

suspend inline fun <reified Response : Any> HttpClient.apiPutMultipart(
    route: String,
    content: MultiPartFormDataContent
): Result<Response, RemoteError> = apiSafeCall {
    put {
        url(constructRoute(route))
        setBody(content)
    }
}

suspend inline fun <reified T> apiSafeCall(noinline execute: suspend () -> HttpResponse): Result<T, RemoteError> {
    // platformSafeCall maps transport exceptions (no internet, timeout...) per platform; the HTTP
    // response itself is parsed here so the backend error code survives.
    var apiResult: Result<T, RemoteError>? = null
    val transportResult = platformSafeCall(execute = execute) { response ->
        apiResult = responseToApiResult<T>(response = response)
        Result.Success(Unit)
    }
    return apiResult ?: when (transportResult) {
        is Result.Failure -> Result.Failure(RemoteError(status = transportResult.error))
        is Result.Success -> Result.Failure(RemoteError(status = DataError.Remote.UNKNOWN))
    }
}

suspend inline fun <reified T> responseToApiResult(response: HttpResponse): Result<T, RemoteError> {
    val statusCode = response.status.value
    if (statusCode in 200..299) {
        // 204 (leave, kick, ban...) and empty 200 bodies map to Unit
        if (T::class == Unit::class) {
            @Suppress("UNCHECKED_CAST")
            return Result.Success(Unit as T)
        }
        return try {
            Result.Success(data = response.body<T>())
        } catch (e: NoTransformationFoundException) {
            Result.Failure(RemoteError(status = DataError.Remote.SERIALIZATION))
        }
    }

    val errorBody = runCatching { response.bodyAsText() }.getOrNull()
        ?.takeIf { it.isNotBlank() }
        ?.let { text -> runCatching { errorJson.decodeFromString<ErrorResponseDto>(text) }.getOrNull() }

    return Result.Failure(
        RemoteError(
            status = httpStatusToDataError(statusCode),
            code = BackendErrorCode.from(errorBody?.code),
            messages = listOfNotNull(errorBody?.message) + errorBody?.errors.orEmpty()
        )
    )
}

fun httpStatusToDataError(statusCode: Int): DataError.Remote = when (statusCode) {
    400 -> DataError.Remote.BAD_REQUEST
    401 -> DataError.Remote.UNAUTHORIZED
    403 -> DataError.Remote.FORBIDDEN
    404 -> DataError.Remote.NOT_FOUND
    408 -> DataError.Remote.REQUEST_TIMEOUT
    409 -> DataError.Remote.CONFLICT
    413 -> DataError.Remote.PAYLOAD_TOO_LARGE
    429 -> DataError.Remote.TOO_MANY_REQUESTS
    500 -> DataError.Remote.SERVER_ERROR
    503 -> DataError.Remote.SERVICE_UNAVAILABLE
    else -> DataError.Remote.UNKNOWN
}

// endregion

// region Legacy helpers: same behavior, error reduced to DataError.Remote until each repository migrates

suspend inline fun <reified Request, reified Response : Any> HttpClient.post(
    route: String,
    body: Request,
    queryParams: Map<String, Any> = mapOf(),
    crossinline builder: HttpRequestBuilder.() -> Unit = {}
): Result<Response, DataError.Remote> =
    apiPost<Request, Response>(route = route, body = body, queryParams = queryParams, builder = builder).mapError { it.status }

suspend inline fun <reified Response : Any> HttpClient.get(
    route: String,
    queryParams: Map<String, Any> = mapOf(),
    crossinline builder: HttpRequestBuilder.() -> Unit = {}
): Result<Response, DataError.Remote> =
    apiGet<Response>(route = route, queryParams = queryParams, builder = builder).mapError { it.status }

suspend inline fun <reified Request, reified Response : Any> HttpClient.put(
    route: String,
    queryParams: Map<String, Any> = mapOf(),
    body: Request,
    crossinline builder: HttpRequestBuilder.() -> Unit = {}
): Result<Response, DataError.Remote> =
    apiPut<Request, Response>(route = route, body = body, queryParams = queryParams, builder = builder).mapError { it.status }

suspend inline fun <reified Request, reified Response : Any> HttpClient.patch(
    route: String,
    body: Request,
    queryParams: Map<String, Any> = mapOf(),
    crossinline builder: HttpRequestBuilder.() -> Unit = {}
): Result<Response, DataError.Remote> =
    apiPatch<Request, Response>(route = route, body = body, queryParams = queryParams, builder = builder).mapError { it.status }

suspend inline fun <reified Response : Any> HttpClient.postMultipart(
    route: String,
    content: MultiPartFormDataContent
): Result<Response, DataError.Remote> = apiPostMultipart<Response>(route = route, content = content).mapError { it.status }

suspend inline fun <reified Response : Any> HttpClient.delete(
    route: String,
    queryParams: Map<String, Any> = mapOf(),
    crossinline builder: HttpRequestBuilder.() -> Unit = {}
): Result<Response, DataError.Remote> =
    apiDelete<Response>(route = route, queryParams = queryParams, builder = builder).mapError { it.status }

suspend inline fun <reified T> safeCall(noinline execute: suspend () -> HttpResponse): Result<T, DataError.Remote> =
    apiSafeCall<T>(execute = execute).mapError { it.status }

// endregion

fun constructRoute(route: String): String {
    return when {
        route.contains(BASE_URL_HTTP) -> route
        route.startsWith("/") -> "${BASE_URL_HTTP}$route"
        else -> "${BASE_URL_HTTP}/$route"
    }
}
