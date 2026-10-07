package com.kikepb.chat.data.datasource

import com.kikepb.chat.data.datasource.remote.message.KtorChatMessageService
import com.kikepb.chat.data.helper.buildMockHttpClient
import com.kikepb.core.domain.util.DataError
import com.kikepb.core.domain.util.Result
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.client.request.HttpRequestData
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class KtorChatMessageServiceTest {

    // --- fetchMessages ---

    @Test
    fun `GIVEN valid chatId WHEN fetchMessages without before THEN GET to correct route and returns messages`() = runTest {
        val requests = mutableListOf<HttpRequestData>()
        val client = buildMockHttpClient(
            responseBody = """
                [
                  {
                    "id": "msg-1",
                    "chatId": "chat-1",
                    "content": "Hello!",
                    "createdAt": "2024-01-01T00:00:00Z",
                    "senderId": "user-1"
                  }
                ]
            """.trimIndent(),
            capturedRequests = requests
        )
        val service = KtorChatMessageService(httpClient = client)

        val result = service.fetchMessages(chatId = "chat-1", before = null)

        assertTrue(result is Result.Success)
        val messages = (result as Result.Success).data
        assertEquals(1, messages.size)
        assertEquals("msg-1", messages.first().id)
        assertEquals("Hello!", messages.first().content)
        assertEquals(HttpMethod.Get, requests.first().method)
        assertTrue(requests.first().url.encodedPath.endsWith("/chats/chat-1/messages"))
    }

    @Test
    fun `GIVEN before cursor WHEN fetchMessages THEN includes before query parameter`() = runTest {
        val requests = mutableListOf<HttpRequestData>()
        val client = buildMockHttpClient(
            responseBody = "[]",
            capturedRequests = requests
        )
        val service = KtorChatMessageService(httpClient = client)

        service.fetchMessages(chatId = "chat-1", before = "some-cursor")

        val queryParams = requests.first().url.parameters
        assertEquals("some-cursor", queryParams["before"])
    }

    @Test
    fun `GIVEN no before cursor WHEN fetchMessages THEN before param is absent`() = runTest {
        val requests = mutableListOf<HttpRequestData>()
        val client = buildMockHttpClient(responseBody = "[]", capturedRequests = requests)
        val service = KtorChatMessageService(httpClient = client)

        service.fetchMessages(chatId = "chat-1", before = null)

        val queryParams = requests.first().url.parameters
        assertTrue(queryParams["before"] == null)
    }

    @Test
    fun `GIVEN pageSize WHEN fetchMessages THEN pageSize query parameter is set`() = runTest {
        val requests = mutableListOf<HttpRequestData>()
        val client = buildMockHttpClient(responseBody = "[]", capturedRequests = requests)
        val service = KtorChatMessageService(httpClient = client)

        service.fetchMessages(chatId = "chat-1", before = null)

        val queryParams = requests.first().url.parameters
        assertTrue(queryParams["pageSize"] != null)
    }

    @Test
    fun `GIVEN multiple messages WHEN fetchMessages THEN all messages are returned`() = runTest {
        val client = buildMockHttpClient(
            responseBody = """
                [
                  {"id":"msg-1","chatId":"chat-1","content":"Hi","createdAt":"2024-01-01T00:00:00Z","senderId":"u1"},
                  {"id":"msg-2","chatId":"chat-1","content":"Hello","createdAt":"2024-01-01T00:01:00Z","senderId":"u2"}
                ]
            """.trimIndent()
        )
        val service = KtorChatMessageService(httpClient = client)

        val result = service.fetchMessages(chatId = "chat-1", before = null)

        assertTrue(result is Result.Success)
        assertEquals(2, (result as Result.Success).data.size)
    }

    @Test
    fun `GIVEN 404 response WHEN fetchMessages THEN returns NOT_FOUND`() = runTest {
        val client = buildMockHttpClient(status = HttpStatusCode.NotFound, responseBody = "")
        val service = KtorChatMessageService(httpClient = client)

        val result = service.fetchMessages(chatId = "missing-chat", before = null)

        assertTrue(result is Result.Failure)
        assertEquals(DataError.Remote.NOT_FOUND, (result as Result.Failure).error)
    }

    @Test
    fun `GIVEN 500 response WHEN fetchMessages THEN returns SERVER_ERROR`() = runTest {
        val client = buildMockHttpClient(status = HttpStatusCode.InternalServerError, responseBody = "")
        val service = KtorChatMessageService(httpClient = client)

        val result = service.fetchMessages(chatId = "chat-1", before = null)

        assertTrue(result is Result.Failure)
        assertEquals(DataError.Remote.SERVER_ERROR, (result as Result.Failure).error)
    }

    // --- deleteMessage ---

    @Test
    fun `GIVEN valid messageId WHEN deleteMessage THEN DELETE to correct route`() = runTest {
        val requests = mutableListOf<HttpRequestData>()
        val client = buildMockHttpClient(responseBody = "", capturedRequests = requests)
        val service = KtorChatMessageService(httpClient = client)

        val result = service.deleteMessage(messageId = "msg-42")

        assertTrue(result is Result.Success)
        assertEquals(HttpMethod.Delete, requests.first().method)
        assertTrue(requests.first().url.encodedPath.endsWith("/messages/msg-42"))
    }

    @Test
    fun `GIVEN 404 response WHEN deleteMessage THEN returns NOT_FOUND`() = runTest {
        val client = buildMockHttpClient(status = HttpStatusCode.NotFound, responseBody = "")
        val service = KtorChatMessageService(httpClient = client)

        val result = service.deleteMessage(messageId = "gone")

        assertTrue(result is Result.Failure)
        assertEquals(DataError.Remote.NOT_FOUND, (result as Result.Failure).error)
    }
}
