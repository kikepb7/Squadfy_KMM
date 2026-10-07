package com.kikepb.core.data.networking

import com.kikepb.core.domain.auth.model.AuthInfoModel
import com.kikepb.core.domain.auth.model.UserModel
import com.kikepb.core.domain.auth.repository.SessionStorage
import com.kikepb.core.domain.logger.SquadfyLogger
import com.kikepb.core.domain.util.Result
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.MockRequestHandleScope
import io.ktor.client.engine.mock.respond
import io.ktor.client.request.HttpRequestData
import io.ktor.client.request.HttpResponseData
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.Serializable
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class TokenRefreshTest {

    @Serializable
    private data class MeDto(val id: String)

    private class FakeSessionStorage(initial: AuthInfoModel?) : SessionStorage {
        val current = MutableStateFlow(initial)
        override fun observeAuthInfo(): Flow<AuthInfoModel?> = current
        override suspend fun set(info: AuthInfoModel?) { current.value = info }
    }

    private object NoOpLogger : SquadfyLogger {
        override fun debug(message: String) = Unit
        override fun info(message: String) = Unit
        override fun warn(message: String) = Unit
        override fun error(message: String, throwable: Throwable?) = Unit
    }

    private val session = AuthInfoModel(
        accessToken = "old-access",
        refreshToken = "old-refresh",
        user = UserModel(id = "u-1", email = "me@example.com", username = "me", hasVerifiedEmail = true, profilePictureUrl = "https://cdn/pic.jpg")
    )

    private val jsonHeaders = headersOf(HttpHeaders.ContentType, "application/json")

    /** Protected route answers 401 to the old token and 200 to the new one; refresh answers [refreshResponse]. */
    private fun engine(refreshResponse: MockRequestHandleScope.(HttpRequestData) -> HttpResponseData) = MockEngine { request ->
        when {
            request.url.encodedPath.endsWith("/auth/refresh") -> refreshResponse(request)
            request.headers[HttpHeaders.Authorization] == "Bearer new-access" ->
                respond("""{"id":"u-1"}""", HttpStatusCode.OK, jsonHeaders)
            else -> respond("", HttpStatusCode.Unauthorized, headersOf(HttpHeaders.WWWAuthenticate, "Bearer"))
        }
    }

    @Test
    fun `AC-002-10 refresh rotates and persists the new token pair keeping the profile picture`() = runTest {
        val storage = FakeSessionStorage(session)
        val client = HttpClientFactory(NoOpLogger, storage).create(engine {
            respond(
                """{"user":{"id":"u-1","email":"me@example.com","username":"me","hasVerifiedEmail":true},"accessToken":"new-access","refreshToken":"new-refresh"}""",
                HttpStatusCode.OK,
                jsonHeaders
            )
        })

        val result = client.apiGet<MeDto>(route = "/me")

        assertEquals(Result.Success(MeDto(id = "u-1")), result)
        val stored = assertNotNull(storage.current.value)
        assertEquals("new-access", stored.accessToken)
        assertEquals("new-refresh", stored.refreshToken)
        assertEquals("https://cdn/pic.jpg", stored.user.profilePictureUrl)
    }

    @Test
    fun `AC-002-10 refresh rejected with INVALID_TOKEN ends the session`() = runTest {
        val storage = FakeSessionStorage(session)
        val client = HttpClientFactory(NoOpLogger, storage).create(engine {
            respond("""{"code":"INVALID_TOKEN","message":"Invalid token"}""", HttpStatusCode.Unauthorized, jsonHeaders)
        })

        client.apiGet<MeDto>(route = "/me")

        assertNull(storage.current.value)
    }

    @Test
    fun `AC-002-09 refresh rate limited keeps the session`() = runTest {
        val storage = FakeSessionStorage(session)
        val client = HttpClientFactory(NoOpLogger, storage).create(engine {
            respond("""{"code":"RATE_LIMIT_EXCEEDED","message":"Too many requests"}""", HttpStatusCode.TooManyRequests, jsonHeaders)
        })

        client.apiGet<MeDto>(route = "/me")

        assertEquals(session, storage.current.value)
    }
}
