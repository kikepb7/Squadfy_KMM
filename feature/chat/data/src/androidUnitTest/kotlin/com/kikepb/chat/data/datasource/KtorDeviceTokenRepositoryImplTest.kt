package com.kikepb.chat.data.datasource

import com.kikepb.chat.data.helper.buildMockHttpClient
import com.kikepb.chat.data.notification.KtorDeviceTokenRepositoryImpl
import com.kikepb.core.domain.util.DataError
import com.kikepb.core.domain.util.Result
import io.ktor.client.request.HttpRequestData
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class KtorDeviceTokenRepositoryImplTest {

    // --- registerToken ---

    @Test
    fun `GIVEN valid token and platform WHEN registerToken THEN POST to correct route`() = runTest {
        val requests = mutableListOf<HttpRequestData>()
        val client = buildMockHttpClient(responseBody = "", capturedRequests = requests)
        val repository = KtorDeviceTokenRepositoryImpl(httpClient = client)

        val result = repository.registerToken(token = "device-token-123", platform = "android")

        assertTrue(result is Result.Success)
        assertEquals(HttpMethod.Post, requests.first().method)
        assertTrue(requests.first().url.encodedPath.contains("notification"))
        assertTrue(requests.first().url.encodedPath.contains("register"))
    }

    @Test
    fun `GIVEN server error WHEN registerToken THEN returns SERVER_ERROR`() = runTest {
        val client = buildMockHttpClient(status = HttpStatusCode.InternalServerError, responseBody = "")
        val repository = KtorDeviceTokenRepositoryImpl(httpClient = client)

        val result = repository.registerToken(token = "token", platform = "android")

        assertTrue(result is Result.Failure)
        assertEquals(DataError.Remote.SERVER_ERROR, (result as Result.Failure).error)
    }

    @Test
    fun `GIVEN 401 WHEN registerToken THEN returns UNAUTHORIZED`() = runTest {
        val client = buildMockHttpClient(status = HttpStatusCode.Unauthorized, responseBody = "")
        val repository = KtorDeviceTokenRepositoryImpl(httpClient = client)

        val result = repository.registerToken(token = "token", platform = "android")

        assertTrue(result is Result.Failure)
        assertEquals(DataError.Remote.UNAUTHORIZED, (result as Result.Failure).error)
    }

    // --- unregisterToken ---

    @Test
    fun `GIVEN valid token WHEN unregisterToken THEN DELETE to correct route with token in path`() = runTest {
        val requests = mutableListOf<HttpRequestData>()
        val client = buildMockHttpClient(responseBody = "", capturedRequests = requests)
        val repository = KtorDeviceTokenRepositoryImpl(httpClient = client)

        val result = repository.unregisterToken(token = "device-token-abc")

        assertTrue(result is Result.Success)
        assertEquals(HttpMethod.Delete, requests.first().method)
        assertTrue(requests.first().url.encodedPath.contains("device-token-abc"))
    }

    @Test
    fun `GIVEN server error WHEN unregisterToken THEN returns SERVER_ERROR`() = runTest {
        val client = buildMockHttpClient(status = HttpStatusCode.InternalServerError, responseBody = "")
        val repository = KtorDeviceTokenRepositoryImpl(httpClient = client)

        val result = repository.unregisterToken(token = "token")

        assertTrue(result is Result.Failure)
        assertEquals(DataError.Remote.SERVER_ERROR, (result as Result.Failure).error)
    }

    @Test
    fun `GIVEN 404 WHEN unregisterToken THEN returns NOT_FOUND`() = runTest {
        val client = buildMockHttpClient(status = HttpStatusCode.NotFound, responseBody = "")
        val repository = KtorDeviceTokenRepositoryImpl(httpClient = client)

        val result = repository.unregisterToken(token = "unknown-token")

        assertTrue(result is Result.Failure)
        assertEquals(DataError.Remote.NOT_FOUND, (result as Result.Failure).error)
    }
}
