package com.kikepb.core.data.crash

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import com.kikepb.core.domain.crash.CrashReporter
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

class CrashReportingConsentTest {

    private class RecordingCrashReporter : CrashReporter {
        override val isAvailable = true
        val calls = mutableListOf<Boolean>()
        override fun setCollectionEnabled(enabled: Boolean) { calls += enabled }
    }

    private val file = File.createTempFile("consent", ".preferences_pb").apply { delete() }
    private val dataStore = PreferenceDataStoreFactory.create(scope = CoroutineScope(Dispatchers.IO + SupervisorJob())) { file }
    private val reporter = RecordingCrashReporter()
    private val consent = DataStoreCrashReportingConsent(dataStore, reporter)

    @Test
    fun `AC-011-13 crash reports are off until the user opts in`() = runTest {
        assertFalse(consent.observe().first())

        consent.applyStored()

        assertEquals(listOf(false), reporter.calls)
    }

    @Test
    fun `AC-011-13 opting in is stored, applied now and re-applied on the next start`() = runTest {
        consent.set(enabled = true)

        assertTrue(consent.observe().first())
        assertEquals(listOf(true), reporter.calls)

        val nextStartReporter = RecordingCrashReporter()
        DataStoreCrashReportingConsent(dataStore, nextStartReporter).applyStored()
        assertEquals(listOf(true), nextStartReporter.calls)
    }

    @Test
    fun `AC-011-13 the consent can be withdrawn`() = runTest {
        consent.set(enabled = true)
        consent.set(enabled = false)

        assertFalse(consent.observe().first())
        assertEquals(listOf(true, false), reporter.calls)
    }
}
