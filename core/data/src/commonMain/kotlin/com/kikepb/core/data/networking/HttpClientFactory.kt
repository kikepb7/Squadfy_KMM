package com.kikepb.core.data.networking

import com.kikepb.core.data.BuildKonfig
import com.kikepb.core.data.auth.dto.AuthInfoSerializableDTO
import com.kikepb.core.data.auth.dto.request.RefreshRequestDTO
import com.kikepb.core.data.mappers.toDomain
import com.kikepb.core.domain.auth.repository.SessionStorage
import com.kikepb.core.domain.logger.SquadfyLogger
import com.kikepb.core.domain.util.DataError
import com.kikepb.core.domain.util.onFailure
import com.kikepb.core.domain.util.onSuccess
import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.BearerTokens
import io.ktor.client.plugins.auth.providers.bearer
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.http.HttpHeaders
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.client.statement.request
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.serialization.json.Json

class HttpClientFactory(
    private val squadfyLogger: SquadfyLogger,
    private val sessionStorage: SessionStorage
) {

    fun create(engine: HttpClientEngine): HttpClient {
        return HttpClient(engine = engine) {
            install(ContentNegotiation) {
                json(
                    json = squadfyJson
                )
            }
            install(HttpTimeout) {
                socketTimeoutMillis = 20_000L
                requestTimeoutMillis = 20_000L
            }
            // AC-011-04: no HTTP logging in release; in debug, headers only and without credentials
            if (!BuildKonfig.IS_RELEASE) {
                install(Logging) {
                    logger = object : Logger {
                        override fun log(message: String) = squadfyLogger.debug(message = message)
                    }
                    level = LogLevel.HEADERS
                    sanitizeHeader { header -> header == HttpHeaders.Authorization }
                }
            }
            install(WebSockets) {
                pingIntervalMillis = 20_000L
            }
            defaultRequest {
                contentType(ContentType.Application.Json)
            }

            install(Auth) {
                bearer {
                    loadTokens {
                        sessionStorage
                            .observeAuthInfo()
                            .firstOrNull()
                            ?.let {
                                BearerTokens(
                                    accessToken = it.accessToken,
                                    refreshToken = it.refreshToken
                                )
                            }
                    }
                    refreshTokens {
                        if (response.request.url.encodedPath.isPublicAuthRoute()) return@refreshTokens null

                        val authInfo = sessionStorage
                            .observeAuthInfo()
                            .firstOrNull()

                        if (authInfo?.refreshToken.isNullOrBlank()) {
                            sessionStorage.set(null)
                            return@refreshTokens null
                        }

                        var bearerTokens: BearerTokens? = null
                        client.apiPost<RefreshRequestDTO, AuthInfoSerializableDTO>(
                            route = "/auth/refresh",
                            body = RefreshRequestDTO(
                                refreshToken = authInfo.refreshToken
                            ),
                            builder = {
                                markAsRefreshTokenRequest()
                            }
                        ).onSuccess { newAuthInfo ->
                            // The refresh token rotates: persist the new pair. v1 UserDto has no picture,
                            // so keep the one already stored in the session.
                            val refreshed = newAuthInfo.toDomain()
                            sessionStorage.set(
                                refreshed.copy(user = refreshed.user.copy(profilePictureUrl = authInfo.user.profilePictureUrl))
                            )
                            bearerTokens = BearerTokens(
                                accessToken = newAuthInfo.accessToken,
                                refreshToken = newAuthInfo.refreshToken
                            )
                        }.onFailure { error ->
                            // Only an invalid/expired/used refresh token ends the session (APP-RN-11).
                            // No connection, timeouts or 429 keep it so the next request can retry.
                            if (error.status == DataError.Remote.UNAUTHORIZED) sessionStorage.set(null)
                        }

                        bearerTokens
                    }
                }
            }
        }
    }
}

/** JSON config shared by the HTTP client: tolerant to new fields and unknown enum values (APP-RN-13). */
val squadfyJson = Json {
    ignoreUnknownKeys = true
    coerceInputValues = true
    explicitNulls = false
}

/** Public auth endpoints never trigger a token refresh; `change-password` is authenticated. */
internal fun String.isPublicAuthRoute(): Boolean = contains("/auth/") && !contains("/auth/change-password")
