package com.kikepb.core.data.analytics

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import com.kikepb.core.domain.analytics.AnalyticsEvent
import com.kikepb.core.domain.analytics.AnalyticsTracker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Test
import java.io.File
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class AnalyticsConsentTest {

    private class RecordingTracker : AnalyticsTracker {
        override val isAvailable = true
        val calls = mutableListOf<Boolean>()
        override fun logEvent(event: AnalyticsEvent) = Unit
        override fun logScreenView(screenName: String) = Unit
        override fun setCollectionEnabled(enabled: Boolean) { calls += enabled }
    }

    private val file = File.createTempFile("analytics", ".preferences_pb").apply { delete() }
    private val dataStore = PreferenceDataStoreFactory.create(scope = CoroutineScope(Dispatchers.IO + SupervisorJob())) { file }
    private val tracker = RecordingTracker()
    private val consent = DataStoreAnalyticsConsent(dataStore, tracker)

    @Test
    fun `AC-019-18 analytics is off until the user opts in`() = runTest {
        assertFalse(consent.observe().first())

        consent.applyStored()

        assertEquals(listOf(false), tracker.calls)
    }

    @Test
    fun `AC-019-18 opting in is stored, applied now and re-applied on the next start`() = runTest {
        consent.set(enabled = true)

        assertTrue(consent.observe().first())
        assertEquals(listOf(true), tracker.calls)

        val nextStart = RecordingTracker()
        DataStoreAnalyticsConsent(dataStore, nextStart).applyStored()
        assertEquals(listOf(true), nextStart.calls)
    }

    @Test
    fun `AC-019-18 withdrawing consent switches collection off`() = runTest {
        consent.set(enabled = true)
        consent.set(enabled = false)

        assertFalse(consent.observe().first())
        assertEquals(listOf(true, false), tracker.calls)
    }
}
