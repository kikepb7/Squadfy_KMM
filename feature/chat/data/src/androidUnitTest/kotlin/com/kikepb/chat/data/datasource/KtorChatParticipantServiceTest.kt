package com.kikepb.chat.data.datasource

import com.kikepb.chat.data.datasource.remote.participant.KtorChatParticipantService
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

class KtorChatParticipantServiceTest {

    private val participantJson = """
        {"userId":"user-1","username":"alice","profilePictureUrl":null}
    """.trimIndent()

    // --- searchParticipant ---

    @Test
    fun `GIVEN query WHEN searchParticipant THEN GET to participants route with query param`() = runTest {
        val requests = mutableListOf<HttpRequestData>()
        val client = buildMockHttpClient(
            responseBody = participantJson,
            capturedRequests = requests
        )
        val service = KtorChatParticipantService(httpClient = client)

        val result = service.searchParticipant(query = "alice")

        assertTrue(result is Result.Success)
        val participant = (result as Result.Success).data
        assertEquals("user-1", participant.userId)
        assertEquals("alice", participant.username)
        assertEquals(HttpMethod.Get, requests.first().method)
        assertEquals("alice", requests.first().url.parameters["query"])
    }

    @Test
    fun `GIVEN no participant matches WHEN searchParticipant THEN returns NOT_FOUND`() = runTest {
        val client = buildMockHttpClient(status = HttpStatusCode.NotFound, responseBody = "")
        val service = KtorChatParticipantService(httpClient = client)

        val result = service.searchParticipant(query = "nobody")

        assertTrue(result is Result.Failure)
        assertEquals(DataError.Remote.NOT_FOUND, (result as Result.Failure).error)
    }

    // --- getLocalParticipant ---

    @Test
    fun `WHEN getLocalParticipant THEN GET to participants route and returns participant`() = runTest {
        val requests = mutableListOf<HttpRequestData>()
        val client = buildMockHttpClient(
            responseBody = participantJson,
            capturedRequests = requests
        )
        val service = KtorChatParticipantService(httpClient = client)

        val result = service.getLocalParticipant()

        assertTrue(result is Result.Success)
        assertEquals("user-1", (result as Result.Success).data.userId)
        assertEquals(HttpMethod.Get, requests.first().method)
        assertTrue(requests.first().url.encodedPath.contains("participants"))
    }

    @Test
    fun `GIVEN 401 WHEN getLocalParticipant THEN returns UNAUTHORIZED`() = runTest {
        val client = buildMockHttpClient(status = HttpStatusCode.Unauthorized, responseBody = "")
        val service = KtorChatParticipantService(httpClient = client)

        val result = service.getLocalParticipant()

        assertTrue(result is Result.Failure)
        assertEquals(DataError.Remote.UNAUTHORIZED, (result as Result.Failure).error)
    }

    // --- getProfilePictureUploadUrl ---

    @Test
    fun `GIVEN mimeType WHEN getProfilePictureUploadUrl THEN POST with mimeType and returns upload urls`() = runTest {
        val requests = mutableListOf<HttpRequestData>()
        val client = buildMockHttpClient(
            responseBody = """
                {
                  "uploadUrl": "https://storage.example.com/upload",
                  "publicUrl": "https://cdn.example.com/pic.jpg",
                  "headers": {"Content-Type": "image/jpeg"}
                }
            """.trimIndent(),
            capturedRequests = requests
        )
        val service = KtorChatParticipantService(httpClient = client)

        val result = service.getProfilePictureUploadUrl(mimeType = "image/jpeg")

        assertTrue(result is Result.Success)
        val uploadUrls = (result as Result.Success).data
        assertEquals("https://storage.example.com/upload", uploadUrls.uploadUrl)
        assertEquals("https://cdn.example.com/pic.jpg", uploadUrls.publicUrl)
        assertEquals("image/jpeg", requests.first().url.parameters["mimeType"])
        assertEquals(HttpMethod.Post, requests.first().method)
    }

    @Test
    fun `GIVEN server error WHEN getProfilePictureUploadUrl THEN returns SERVER_ERROR`() = runTest {
        val client = buildMockHttpClient(status = HttpStatusCode.InternalServerError, responseBody = "")
        val service = KtorChatParticipantService(httpClient = client)

        val result = service.getProfilePictureUploadUrl(mimeType = "image/png")

        assertTrue(result is Result.Failure)
        assertEquals(DataError.Remote.SERVER_ERROR, (result as Result.Failure).error)
    }

    // --- uploadProfilePicture ---

    @Test
    fun `GIVEN valid data WHEN uploadProfilePicture THEN PUT to external upload url`() = runTest {
        val requests = mutableListOf<HttpRequestData>()
        val client = buildMockHttpClient(responseBody = "", capturedRequests = requests)
        val service = KtorChatParticipantService(httpClient = client)

        val result = service.uploadProfilePicture(
            uploadUrl = "https://storage.example.com/upload",
            imageBytes = byteArrayOf(1, 2, 3),
            headers = mapOf("Content-Type" to "image/jpeg")
        )

        assertTrue(result is Result.Success)
        assertEquals(HttpMethod.Put, requests.first().method)
        assertTrue(requests.first().url.toString().contains("storage.example.com"))
    }

    @Test
    fun `GIVEN payload too large WHEN uploadProfilePicture THEN returns PAYLOAD_TOO_LARGE`() = runTest {
        val client = buildMockHttpClient(status = HttpStatusCode.PayloadTooLarge, responseBody = "")
        val service = KtorChatParticipantService(httpClient = client)

        val result = service.uploadProfilePicture(
            uploadUrl = "https://storage.example.com/upload",
            imageBytes = ByteArray(1000),
            headers = emptyMap()
        )

        assertTrue(result is Result.Failure)
        assertEquals(DataError.Remote.PAYLOAD_TOO_LARGE, (result as Result.Failure).error)
    }

    // --- confirmProfilePictureUpload ---

    @Test
    fun `GIVEN public url WHEN confirmProfilePictureUpload THEN POST to confirm route`() = runTest {
        val requests = mutableListOf<HttpRequestData>()
        val client = buildMockHttpClient(responseBody = "", capturedRequests = requests)
        val service = KtorChatParticipantService(httpClient = client)

        val result = service.confirmProfilePictureUpload(publicUrl = "https://cdn.example.com/pic.jpg")

        assertTrue(result is Result.Success)
        assertEquals(HttpMethod.Post, requests.first().method)
        assertTrue(requests.first().url.encodedPath.contains("confirm-profile-picture"))
    }

    // --- deleteProfilePicture ---

    @Test
    fun `WHEN deleteProfilePicture THEN DELETE to correct route`() = runTest {
        val requests = mutableListOf<HttpRequestData>()
        val client = buildMockHttpClient(responseBody = "", capturedRequests = requests)
        val service = KtorChatParticipantService(httpClient = client)

        val result = service.deleteProfilePicture()

        assertTrue(result is Result.Success)
        assertEquals(HttpMethod.Delete, requests.first().method)
        assertTrue(requests.first().url.encodedPath.contains("profile-picture"))
    }

    @Test
    fun `GIVEN 500 WHEN deleteProfilePicture THEN returns SERVER_ERROR`() = runTest {
        val client = buildMockHttpClient(status = HttpStatusCode.InternalServerError, responseBody = "")
        val service = KtorChatParticipantService(httpClient = client)

        val result = service.deleteProfilePicture()

        assertTrue(result is Result.Failure)
        assertEquals(DataError.Remote.SERVER_ERROR, (result as Result.Failure).error)
    }
}
