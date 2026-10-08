package com.kikepb.core.data.auth

import com.kikepb.core.data.networking.squadfyJson
import com.kikepb.core.domain.util.DataError
import com.kikepb.core.domain.util.Result
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.HttpRequestData
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.TextContent
import io.ktor.http.contentType
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/** Contract of `DELETE /me` (backend spec 010): body `{password}`, 204 on success, 401 INVALID_CREDENTIALS otherwise. */
class DeleteAccountRequestTest {

    private val requests = mutableListOf<HttpRequestData>()

    private fun repository(status: HttpStatusCode, body: String = "") = KtorAuthRepositoryImpl(
        HttpClient(MockEngine { request ->
            requests += request
            respond(content = body, status = status, headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()))
        }) {
            install(ContentNegotiation) { json(squadfyJson) }
            defaultRequest { contentType(ContentType.Application.Json) }
        }
    )

    @Test
    fun `AC-011-07 delete account sends DELETE me with the password and maps 204 to success`() = runTest {
        val result = repository(HttpStatusCode.NoContent).deleteAccount(password = "Secret123")

        assertEquals(Result.Success(Unit), result)
        val request = requests.single()
        assertEquals(HttpMethod.Delete, request.method)
        assertTrue(request.url.encodedPath.endsWith("/api/v1/me"), request.url.encodedPath)
        assertEquals("""{"password":"Secret123"}""", (request.body as TextContent).text)
    }

    @Test
    fun `AC-011-07 a wrong password is UNAUTHORIZED`() = runTest {
        val result = repository(HttpStatusCode.Unauthorized, """{"code":"INVALID_CREDENTIALS","message":"Invalid credentials"}""")
            .deleteAccount(password = "wrong")

        assertEquals(DataError.Remote.UNAUTHORIZED, (result as Result.Failure).error)
    }
}
