package com.kikepb.core.domain.update

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class AppUpdatePolicyTest {

    @Test
    fun `AC-021-08 below the minimum version the update is required`() {
        val config = AppVersionConfig(latestVersion = "1.5.0", minVersion = "1.4.0")

        assertEquals(UpdateStatus.REQUIRED, AppUpdatePolicy.status("1.3.9", config))
        assertEquals(UpdateStatus.REQUIRED, AppUpdatePolicy.status("1.3.9", config, dismissedVersion = "1.5.0"))
    }

    @Test
    fun `AC-021-08 below the latest version the update is optional until dismissed`() {
        val config = AppVersionConfig(latestVersion = "1.5.0", minVersion = "1.4.0")

        assertEquals(UpdateStatus.OPTIONAL, AppUpdatePolicy.status("1.4.2", config))
        assertEquals(UpdateStatus.UP_TO_DATE, AppUpdatePolicy.status("1.4.2", config, dismissedVersion = "1.5.0"))
        assertEquals(UpdateStatus.OPTIONAL, AppUpdatePolicy.status("1.4.2", config.copy(latestVersion = "1.6.0"), dismissedVersion = "1.5.0"))
    }

    @Test
    fun `AC-021-08 the latest version, newer builds and unpublished values never ask`() {
        assertEquals(UpdateStatus.UP_TO_DATE, AppUpdatePolicy.status("1.5.0", AppVersionConfig(latestVersion = "1.5.0", minVersion = "1.4.0")))
        assertEquals(UpdateStatus.UP_TO_DATE, AppUpdatePolicy.status("1.10.0", AppVersionConfig(latestVersion = "1.9.0")))
        assertEquals(UpdateStatus.UP_TO_DATE, AppUpdatePolicy.status("1.0.0", AppVersionConfig()))
        assertEquals(UpdateStatus.UP_TO_DATE, AppUpdatePolicy.status("dev", AppVersionConfig(minVersion = "9.0.0")))
    }

    @Test
    fun `versions compare by number, with missing parts as zero and suffixes ignored`() {
        assertEquals(UpdateStatus.UP_TO_DATE, AppUpdatePolicy.status("1.4", AppVersionConfig(minVersion = "1.4.0")))
        assertEquals(UpdateStatus.REQUIRED, AppUpdatePolicy.status("1.4.0-beta", AppVersionConfig(minVersion = "1.4.1")))
        assertEquals(listOf(2, 0, 1), AppUpdatePolicy.parse(" 2.0.1+45 "))
        assertNull(AppUpdatePolicy.parse("1.x"))
    }
}
