package com.kikepb.club.data.datasource

import com.kikepb.club.data.datasource.remote.KtorStandingsRepository
import com.kikepb.club.domain.model.StatsSortBy
import com.kikepb.core.data.networking.squadfyJson
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.HttpRequestData
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/** `GET /clubs/{id}/stats?sortBy=&from=&to=` (backend spec 012 RN-B). */
class StandingsPeriodRequestTest {

    private val requests = mutableListOf<HttpRequestData>()
    private val repository = KtorStandingsRepository(
        HttpClient(MockEngine { request ->
            requests += request
            respond(content = "[]", headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()))
        }) { install(ContentNegotiation) { json(squadfyJson) } }
    )

    @Test
    fun `AC-015-07 a period sends inclusive from and to dates`() = runTest {
        repository.getStats("c-1", StatsSortBy.ASSISTS, LocalDate(2026, 1, 1)..LocalDate(2026, 10, 8))

        val url = requests.single().url
        assertEquals("ASSISTS", url.parameters["sortBy"])
        assertEquals("2026-01-01", url.parameters["from"])
        assertEquals("2026-10-08", url.parameters["to"])
    }

    @Test
    fun `AC-015-07 all time sends no dates`() = runTest {
        repository.getStats("c-1", StatsSortBy.GOALS)

        val url = requests.single().url
        assertNull(url.parameters["from"])
        assertNull(url.parameters["to"])
    }
}
