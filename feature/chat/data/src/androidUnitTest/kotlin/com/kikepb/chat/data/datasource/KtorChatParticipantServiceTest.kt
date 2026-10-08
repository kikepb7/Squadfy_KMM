package com.kikepb.chat.data.datasource

import com.kikepb.chat.data.datasource.remote.participant.KtorChatParticipantService
import com.kikepb.chat.data.fake.FakeSessionStorage
import com.kikepb.chat.data.helper.buildMockHttpClient
import com.kikepb.core.domain.auth.model.AuthInfoModel
import com.kikepb.core.domain.auth.model.UserModel
import io.ktor.client.HttpClient
import com.kikepb.core.domain.util.DataError
import com.kikepb.core.domain.util.Result
import io.ktor.client.request.HttpRequestData
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class KtorChatParticipantServiceTest {

    private val sessionStorage = FakeSessionStorage().apply {
        runBlocking {
            set(
                AuthInfoModel(
                    accessToken = "access",
                    refreshToken = "refresh",
                    user = UserModel(id = "user-id", email = "me@example.com", username = "me", hasVerifiedEmail = true, profilePictureUrl = null)
                )
            )
        }
    }

    private fun createService(client: HttpClient) = KtorChatParticipantService(httpClient = client, sessionStorage = sessionStorage)

    private val participantJson = """
        {"userId":"user-1","username":"alice","profilePictureUrl":null}
    """.trimIndent()

    // --- searchParticipants (backend spec 012 RN-D) ---

    @Test
    fun `AC-015-04 search sends GET users-search with q and maps every result`() = runTest {
        val requests = mutableListOf<HttpRequestData>()
        val client = buildMockHttpClient(
            responseBody = """[{"userId":"user-1","username":"carlos","profilePictureUrl":null},{"userId":"user-2","username":"marcos","profilePictureUrl":"https://cdn/m.png"}]""",
            capturedRequests = requests
        )
        val service = createService(client)

        val result = service.searchParticipants(query = "ar")

        assertTrue(result is Result.Success)
        val participants = (result as Result.Success).data
        assertEquals(listOf("carlos", "marcos"), participants.map { it.username })
        assertEquals("https://cdn/m.png", participants[1].profilePictureUrl)
        assertEquals(HttpMethod.Get, requests.first().method)
        assertTrue(requests.first().url.encodedPath.endsWith("/users/search"))
        assertEquals("ar", requests.first().url.parameters["q"])
    }

    @Test
    fun `AC-015-04 no matches is an empty list`() = runTest {
        val service = createService(buildMockHttpClient(responseBody = "[]"))

        val result = service.searchParticipants(query = "zz")

        assertTrue((result as Result.Success).data.isEmpty())
    }

    // --- getLocalParticipant ---

    @Test
    fun `WHEN getLocalParticipant THEN GET to users route and returns participant`() = runTest {
        val requests = mutableListOf<HttpRequestData>()
        val client = buildMockHttpClient(
            responseBody = participantJson,
            capturedRequests = requests
        )
        val service = createService(client)

        val result = service.getLocalParticipant()

        assertTrue(result is Result.Success)
        assertEquals("user-1", (result as Result.Success).data.userId)
        assertEquals(HttpMethod.Get, requests.first().method)
        assertTrue(requests.first().url.encodedPath.endsWith("/users/user-id"))
    }

    @Test
    fun `GIVEN 401 WHEN getLocalParticipant THEN returns UNAUTHORIZED`() = runTest {
        val client = buildMockHttpClient(status = HttpStatusCode.Unauthorized, responseBody = "")
        val service = createService(client)

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
        val service = createService(client)

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
        val service = createService(client)

        val result = service.getProfilePictureUploadUrl(mimeType = "image/png")

        assertTrue(result is Result.Failure)
        assertEquals(DataError.Remote.SERVER_ERROR, (result as Result.Failure).error)
    }

    // --- uploadProfilePicture ---

    @Test
    fun `GIVEN valid data WHEN uploadProfilePicture THEN PUT to external upload url`() = runTest {
        val requests = mutableListOf<HttpRequestData>()
        val client = buildMockHttpClient(responseBody = "", capturedRequests = requests)
        val service = createService(client)

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
        val service = createService(client)

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
        val service = createService(client)

        val result = service.confirmProfilePictureUpload(publicUrl = "https://cdn.example.com/pic.jpg")

        assertTrue(result is Result.Success)
        assertEquals(HttpMethod.Put, requests.first().method)
        assertTrue(requests.first().url.encodedPath.endsWith("/me/profile-picture"))
    }

    // --- deleteProfilePicture ---

    @Test
    fun `WHEN deleteProfilePicture THEN DELETE to correct route`() = runTest {
        val requests = mutableListOf<HttpRequestData>()
        val client = buildMockHttpClient(responseBody = "", capturedRequests = requests)
        val service = createService(client)

        val result = service.deleteProfilePicture()

        assertTrue(result is Result.Success)
        assertEquals(HttpMethod.Delete, requests.first().method)
        assertTrue(requests.first().url.encodedPath.contains("profile-picture"))
    }

    @Test
    fun `GIVEN 500 WHEN deleteProfilePicture THEN returns SERVER_ERROR`() = runTest {
        val client = buildMockHttpClient(status = HttpStatusCode.InternalServerError, responseBody = "")
        val service = createService(client)

        val result = service.deleteProfilePicture()

        assertTrue(result is Result.Failure)
        assertEquals(DataError.Remote.SERVER_ERROR, (result as Result.Failure).error)
    }

    @Test
    fun `GIVEN no session WHEN getLocalParticipant THEN returns UNAUTHORIZED without calling the API`() = runTest {
        val requests = mutableListOf<HttpRequestData>()
        val client = buildMockHttpClient(responseBody = participantJson, capturedRequests = requests)
        val service = KtorChatParticipantService(httpClient = client, sessionStorage = FakeSessionStorage())

        val result = service.getLocalParticipant()

        assertEquals(DataError.Remote.UNAUTHORIZED, (result as Result.Failure).error)
        assertTrue(requests.isEmpty())
    }
}
