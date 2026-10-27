package com.kikepb.club.domain.usecase

import com.kikepb.club.domain.error.ClubError
import com.kikepb.club.domain.fake.FakeClubRepository
import com.kikepb.core.domain.analytics.AnalyticsEvent
import com.kikepb.core.domain.analytics.AnalyticsTracker
import com.kikepb.core.domain.util.Result
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AnalyticsEventsTest {

    private class FakeAnalyticsTracker : AnalyticsTracker {
        val events = mutableListOf<AnalyticsEvent>()
        override val isAvailable = true
        override fun logEvent(event: AnalyticsEvent) { events += event }
        override fun logScreenView(screenName: String) = Unit
        override fun setCollectionEnabled(enabled: Boolean) = Unit
    }

    private val repository = FakeClubRepository()
    private val analytics = FakeAnalyticsTracker()

    @Test
    fun `AC-021-09 creating and joining a club log their event without ids or names`() = runTest {
        // Act
        CreateClubUseCase(repository, analytics)("Squadfy FC", null, null, null, null)
        JoinClubUseCase(repository, analytics)("AB12CD34", null, null)

        // Assert
        assertEquals(listOf(AnalyticsEvent("club_created"), AnalyticsEvent("club_joined")), analytics.events)
    }

    @Test
    fun `AC-021-09 nothing is logged when the action fails`() = runTest {
        // Arrange
        repository.joinResult = Result.Failure(ClubError.BannedFromClub)

        // Act
        CreateClubUseCase(repository, analytics)("  ", null, null, null, null)
        JoinClubUseCase(repository, analytics)("AB12CD34", null, null)

        // Assert
        assertTrue(analytics.events.isEmpty())
    }
}
