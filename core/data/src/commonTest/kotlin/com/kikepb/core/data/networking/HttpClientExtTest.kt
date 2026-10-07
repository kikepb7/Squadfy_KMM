package com.kikepb.core.data.networking

import com.kikepb.core.domain.util.BackendErrorCode
import com.kikepb.core.domain.util.DataError
import com.kikepb.core.domain.util.RemoteError
import com.kikepb.core.domain.util.Result
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.Serializable
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class HttpClientExtTest {

    @Serializable
    private data class SampleDto(val id: String, val role: String = "PLAYER", val photo: String? = null)

    private fun client(status: HttpStatusCode, body: String, contentType: ContentType = ContentType.Application.Json) =
        HttpClient(MockEngine { respond(content = body, status = status, headers = headersOf(HttpHeaders.ContentType, contentType.toString())) }) {
            install(ContentNegotiation) { json(squadfyJson) }
            defaultRequest { contentType(ContentType.Application.Json) }
        }

    private inline fun <reified T> Result<T, RemoteError>.failure(): RemoteError = (this as Result.Failure).error

    @Test
    fun `AC-002-04 201 responses are decoded like 200`() = runTest {
        val result = client(HttpStatusCode.Created, """{"id":"club-1"}""").apiPost<Unit, SampleDto>(route = "/clubs", body = Unit)

        assertEquals(Result.Success(SampleDto(id = "club-1")), result)
    }

    @Test
    fun `AC-002-04 204 without body maps to Unit`() = runTest {
        val result = client(HttpStatusCode.NoContent, "").apiDelete<Unit>(route = "/clubs/c/members/me")

        assertEquals(Result.Success(Unit), result)
    }

    @Test
    fun `AC-002-02 extra fields, unknown values and explicit nulls do not break decoding`() = runTest {
        val result = client(HttpStatusCode.OK, """{"id":"m-1","role":"PLAYER","photo":null,"brandNewField":42}""")
            .apiGet<SampleDto>(route = "/x")

        assertEquals(Result.Success(SampleDto(id = "m-1")), result)
    }

    @Test
    fun `AC-002-03 business error keeps status, code and message`() = runTest {
        val error = client(HttpStatusCode.Forbidden, """{"code":"BANNED_FROM_CLUB","message":"You are banned"}""")
            .apiPost<Unit, SampleDto>(route = "/clubs/join", body = Unit).failure()

        assertEquals(DataError.Remote.FORBIDDEN, error.status)
        assertEquals(BackendErrorCode.BANNED_FROM_CLUB, error.code)
        assertEquals(listOf("You are banned"), error.messages)
    }

    @Test
    fun `AC-002-03 validation error exposes every message`() = runTest {
        val error = client(HttpStatusCode.BadRequest, """{"code":"VALIDATION_ERROR","errors":["name is blank","maxMembers must be > 0"]}""")
            .apiPost<Unit, SampleDto>(route = "/clubs", body = Unit).failure()

        assertEquals(BackendErrorCode.VALIDATION_ERROR, error.code)
        assertEquals(listOf("name is blank", "maxMembers must be > 0"), error.messages)
    }

    @Test
    fun `AC-002-03 401 without body and HTML 5xx have no code`() = runTest {
        val unauthorized = client(HttpStatusCode.Unauthorized, "").apiGet<SampleDto>(route = "/me").failure()
        val serverError = client(HttpStatusCode.InternalServerError, "<html>boom</html>", ContentType.Text.Html)
            .apiGet<SampleDto>(route = "/me").failure()

        assertEquals(DataError.Remote.UNAUTHORIZED, unauthorized.status)
        assertNull(unauthorized.code)
        assertEquals(DataError.Remote.SERVER_ERROR, serverError.status)
        assertNull(serverError.code)
    }

    @Test
    fun `AC-002-05 USER_EXITS typo and unknown codes are tolerated`() {
        assertEquals(BackendErrorCode.USER_EXISTS, BackendErrorCode.from("USER_EXITS"))
        assertEquals(BackendErrorCode.USER_EXISTS, BackendErrorCode.from("USER_EXISTS"))
        assertEquals(BackendErrorCode.UNKNOWN, BackendErrorCode.from("SOMETHING_NEW"))
        assertNull(BackendErrorCode.from(null))
    }

    @Test
    fun `legacy helpers keep returning the HTTP status as DataError`() = runTest {
        val result = client(HttpStatusCode.Conflict, """{"code":"CONFLICT","message":"Already a member"}""")
            .post<Unit, SampleDto>(route = "/clubs/join", body = Unit)

        assertEquals(Result.Failure(DataError.Remote.CONFLICT), result)
    }

    @Test
    fun `AC-002-01 routes are relative and auth refresh exclusion keeps change-password authenticated`() {
        assertEquals(true, "/api/v1/auth/login".isPublicAuthRoute())
        assertEquals(true, "/api/v1/auth/refresh".isPublicAuthRoute())
        assertEquals(false, "/api/v1/auth/change-password".isPublicAuthRoute())
        assertEquals(false, "/api/v1/clubs".isPublicAuthRoute())
    }
}
