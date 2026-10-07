package com.kikepb.core.domain.notification

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class InAppPushCenterTest {

    private val message = PushMessage(mapOf("type" to PushRouter.TEAMS_PUBLISHED, "clubId" to "c-1", "matchId" to "m-1"))

    @Test
    fun `AC-009-04 a push for the visible club is handled in-app`() {
        val center = InAppPushCenter()
        center.onClubVisible("c-1")

        assertTrue(center.onForegroundMessage(message))
    }

    @Test
    fun `AC-009-04 other clubs and hidden screens get a system notification`() {
        val center = InAppPushCenter()
        center.onClubVisible("c-2")
        assertFalse(center.onForegroundMessage(message))

        center.onClubVisible("c-1")
        center.onClubVisible("c-1") // match tab, then match detail
        center.onClubHidden("c-1")
        assertTrue(center.isClubVisible("c-1"))
        center.onClubHidden("c-1")
        assertFalse(center.onForegroundMessage(message))
    }

    @Test
    fun `AC-009-04 chat pushes without club are never swallowed`() {
        val center = InAppPushCenter()
        center.onClubVisible("c-1")

        assertFalse(center.onForegroundMessage(PushMessage(mapOf("type" to "new_message", "chatId" to "x"))))
    }
}
