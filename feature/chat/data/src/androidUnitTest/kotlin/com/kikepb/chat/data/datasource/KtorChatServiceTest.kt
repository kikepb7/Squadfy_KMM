package com.kikepb.chat.data.datasource

import com.kikepb.chat.data.datasource.remote.KtorChatService
import com.kikepb.chat.data.helper.buildMockHttpClient
import com.kikepb.core.domain.util.DataError
import com.kikepb.core.domain.util.Result
import io.ktor.client.request.HttpRequestData
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class KtorChatServiceTest {

    // --- createChat ---

    @Test
    fun `GIVEN valid user ids WHEN createChat THEN POST to correct route and returns chat`() = runTest {
        val requests = mutableListOf<HttpRequestData>()
        val client = buildMockHttpClient(
            responseBody = """
                {
                  "id": "chat-1",
                  "participants": [{"userId":"u1","username":"alice","profilePictureUrl":null}],
                  "lastActivityAt": "2024-01-01T00:00:00Z",
                  "lastMessage": null
                }
            """.trimIndent(),
            capturedRequests = requests
        )
        val service = KtorChatService(httpClient = client)

        val result = service.createChat(otherUserIds = listOf("u2"))

        assertTrue(result is Result.Success)
        assertEquals("chat-1", (result as Result.Success).data.id)
        assertEquals(HttpMethod.Post, requests.first().method)
        assertTrue(requests.first().url.encodedPath.contains("/chat"))
    }

    @Test
    fun `GIVEN server error WHEN createChat THEN returns SERVER_ERROR`() = runTest {
        val client = buildMockHttpClient(status = HttpStatusCode.InternalServerError, responseBody = "")
        val service = KtorChatService(httpClient = client)

        val result = service.createChat(otherUserIds = listOf("u2"))

        assertTrue(result is Result.Failure)
        assertEquals(DataError.Remote.SERVER_ERROR, (result as Result.Failure).error)
    }

    // --- getChats ---

    @Test
    fun `WHEN getChats THEN GET to chat route and returns list`() = runTest {
        val requests = mutableListOf<HttpRequestData>()
        val client = buildMockHttpClient(
            responseBody = """
                [
                  {
                    "id": "chat-1",
                    "participants": [{"userId":"u1","username":"alice","profilePictureUrl":null}],
                    "lastActivityAt": "2024-01-01T00:00:00Z",
                    "lastMessage": null
                  }
                ]
            """.trimIndent(),
            capturedRequests = requests
        )
        val service = KtorChatService(httpClient = client)

        val result = service.getChats()

        assertTrue(result is Result.Success)
        val chats = (result as Result.Success).data
        assertEquals(1, chats.size)
        assertEquals("chat-1", chats.first().id)
        assertEquals(HttpMethod.Get, requests.first().method)
    }

    @Test
    fun `GIVEN 401 response WHEN getChats THEN returns UNAUTHORIZED`() = runTest {
        val client = buildMockHttpClient(status = HttpStatusCode.Unauthorized, responseBody = "")
        val service = KtorChatService(httpClient = client)

        val result = service.getChats()

        assertTrue(result is Result.Failure)
        assertEquals(DataError.Remote.UNAUTHORIZED, (result as Result.Failure).error)
    }

    // --- getChatById ---

    @Test
    fun `GIVEN valid chatId WHEN getChatById THEN GET to correct route and returns chat`() = runTest {
        val requests = mutableListOf<HttpRequestData>()
        val client = buildMockHttpClient(
            responseBody = """
                {
                  "id": "chat-42",
                  "participants": [],
                  "lastActivityAt": "2024-01-01T00:00:00Z",
                  "lastMessage": null
                }
            """.trimIndent(),
            capturedRequests = requests
        )
        val service = KtorChatService(httpClient = client)

        val result = service.getChatById(chatId = "chat-42")

        assertTrue(result is Result.Success)
        assertEquals("chat-42", (result as Result.Success).data.id)
        assertTrue(requests.first().url.encodedPath.contains("chat-42"))
    }

    @Test
    fun `GIVEN chat not found WHEN getChatById THEN returns NOT_FOUND`() = runTest {
        val client = buildMockHttpClient(status = HttpStatusCode.NotFound, responseBody = "")
        val service = KtorChatService(httpClient = client)

        val result = service.getChatById(chatId = "missing")

        assertTrue(result is Result.Failure)
        assertEquals(DataError.Remote.NOT_FOUND, (result as Result.Failure).error)
    }

    // --- leaveChat ---

    @Test
    fun `GIVEN valid chatId WHEN leaveChat THEN DELETE to correct route and returns success`() = runTest {
        val requests = mutableListOf<HttpRequestData>()
        val client = buildMockHttpClient(responseBody = "", capturedRequests = requests)
        val service = KtorChatService(httpClient = client)

        val result = service.leaveChat(chatId = "chat-1")

        assertTrue(result is Result.Success)
        assertEquals(HttpMethod.Delete, requests.first().method)
        assertTrue(requests.first().url.encodedPath.contains("chat-1"))
        assertTrue(requests.first().url.encodedPath.contains("leave"))
    }

    @Test
    fun `GIVEN server error WHEN leaveChat THEN returns SERVER_ERROR`() = runTest {
        val client = buildMockHttpClient(status = HttpStatusCode.InternalServerError, responseBody = "")
        val service = KtorChatService(httpClient = client)

        val result = service.leaveChat(chatId = "chat-1")

        assertTrue(result is Result.Failure)
        assertEquals(DataError.Remote.SERVER_ERROR, (result as Result.Failure).error)
    }

    // --- addParticipantsToChat ---

    @Test
    fun `GIVEN valid inputs WHEN addParticipantsToChat THEN POST to correct route and returns updated chat`() = runTest {
        val requests = mutableListOf<HttpRequestData>()
        val client = buildMockHttpClient(
            responseBody = """
                {
                  "id": "chat-1",
                  "participants": [
                    {"userId":"u1","username":"alice","profilePictureUrl":null},
                    {"userId":"u2","username":"bob","profilePictureUrl":null}
                  ],
                  "lastActivityAt": "2024-01-01T00:00:00Z",
                  "lastMessage": null
                }
            """.trimIndent(),
            capturedRequests = requests
        )
        val service = KtorChatService(httpClient = client)

        val result = service.addParticipantsToChat(chatId = "chat-1", userIds = listOf("u2"))

        assertTrue(result is Result.Success)
        assertEquals(2, (result as Result.Success).data.participants.size)
        assertEquals(HttpMethod.Post, requests.first().method)
        assertTrue(requests.first().url.encodedPath.contains("chat-1"))
        assertTrue(requests.first().url.encodedPath.contains("add"))
    }

    @Test
    fun `GIVEN conflict WHEN addParticipantsToChat THEN returns CONFLICT`() = runTest {
        val client = buildMockHttpClient(status = HttpStatusCode.Conflict, responseBody = "")
        val service = KtorChatService(httpClient = client)

        val result = service.addParticipantsToChat(chatId = "chat-1", userIds = listOf("u2"))

        assertTrue(result is Result.Failure)
        assertEquals(DataError.Remote.CONFLICT, (result as Result.Failure).error)
    }
}
