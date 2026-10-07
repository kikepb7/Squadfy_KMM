package com.kikepb.core.domain.notification

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class PushRouterTest {

    private val club = mapOf("clubId" to "c-1", "matchId" to "m-1", "announcementId" to "a-1")

    @Test
    fun `AC-009-08 announcement, waitlist, cancelled and rescheduled open the club match tab`() {
        listOf(
            PushRouter.ANNOUNCEMENT_OPENED, PushRouter.ANNOUNCEMENT_CLOSING_SOON, PushRouter.WAITLIST_PROMOTED,
            PushRouter.MATCH_CANCELLED, PushRouter.MATCH_RESCHEDULED
        ).forEach { type ->
            assertEquals("squadfy://club/c-1/announcement", PushRouter.deepLink(club + ("type" to type)), type)
        }
    }

    @Test
    fun `AC-009-08 published teams open the match detail`() {
        assertEquals("squadfy://match/m-1?clubId=c-1", PushRouter.deepLink(club + ("type" to PushRouter.TEAMS_PUBLISHED) + ("team" to "A")))
    }

    @Test
    fun `AC-009-08 chat messages open the chat, also without type`() {
        assertEquals("squadfy://chat_details/ch-1", PushRouter.deepLink(mapOf("type" to "new_message", "chatId" to "ch-1")))
        assertEquals("squadfy://chat_details/ch-1", PushRouter.deepLink(mapOf("chatId" to "ch-1")))
        assertTrue(PushRouter.isChat(mapOf("chatId" to "ch-1")))
    }

    @Test
    fun `AC-009-08 unknown types or missing ids just open the app`() {
        assertNull(PushRouter.deepLink(mapOf("type" to "match.something_new", "clubId" to "c-1")))
        assertNull(PushRouter.deepLink(mapOf("type" to PushRouter.TEAMS_PUBLISHED, "clubId" to "c-1")))
        assertEquals(PushDestination.None, PushRouter.route(emptyMap()))
        assertFalse(PushRouter.isChat(club + ("type" to PushRouter.ANNOUNCEMENT_OPENED)))
    }
}
