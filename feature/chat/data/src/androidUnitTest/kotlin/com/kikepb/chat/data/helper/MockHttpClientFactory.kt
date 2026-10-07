package com.kikepb.chat.data.helper

import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.MockRequestHandleScope
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.HttpRequestData
import io.ktor.client.request.HttpResponseData
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

fun buildMockHttpClient(
    status: HttpStatusCode = HttpStatusCode.OK,
    responseBody: String = "{}",
    capturedRequests: MutableList<HttpRequestData> = mutableListOf(),
    handler: (suspend MockRequestHandleScope.(HttpRequestData) -> HttpResponseData)? = null
): HttpClient {
    val engine = MockEngine { request ->
        capturedRequests.add(request)
        if (handler != null) {
            handler(request)
        } else {
            respond(
                content = responseBody,
                status = status,
                headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            )
        }
    }

    return HttpClient(engine) {
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true })
        }
        // Mirrors HttpClientFactory: without it setBody() cannot serialize request DTOs.
        defaultRequest {
            contentType(ContentType.Application.Json)
        }
    }
}
