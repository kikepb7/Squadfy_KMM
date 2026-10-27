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
    fun `AC-018-07 published teams open the draw result`() {
        // Spec 018: teams published opens the draw result screen
        assertEquals("squadfy://match/m-1/draw?clubId=c-1", PushRouter.deepLink(club + ("type" to PushRouter.TEAMS_PUBLISHED) + ("team" to "A")))
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

    @Test
    fun `AC-021-03 the match-day reminder opens the club match tab and the result opens the match`() {
        assertEquals("squadfy://club/c-1/announcement", PushRouter.deepLink(club + ("type" to PushRouter.MATCH_REMINDER)))
        assertEquals("squadfy://match/m-1?clubId=c-1", PushRouter.deepLink(club + ("type" to PushRouter.MATCH_COMPLETED)))
    }

    @Test
    fun `AC-021-06 club and season pushes open the club, in their own channel`() {
        listOf(
            PushRouter.CLUB_MEMBER_JOINED, PushRouter.CLUB_ROLE_CHANGED, PushRouter.CLUB_OWNERSHIP_TRANSFERRED,
            PushRouter.SEASON_STARTED, PushRouter.SEASON_CLOSED
        ).forEach { type ->
            val data = mapOf("type" to type, "clubId" to "c-1")
            assertEquals("squadfy://club/c-1/announcement", PushRouter.deepLink(data), type)
            assertEquals(PushChannel.CLUB, PushRouter.channelOf(data), type)
        }
    }

    @Test
    fun `AC-021-07 a new version opens the store, only with a web link`() {
        val update = mapOf("type" to PushRouter.APP_UPDATE, "url" to "https://play.google.com/store/apps/details?id=com.kikepb.squadfy")
        assertEquals(PushDestination.External(update.getValue("url")), PushRouter.route(update))
        assertEquals(update["url"], PushRouter.externalUrl(update))
        assertNull(PushRouter.deepLink(update))
        assertEquals(PushChannel.APP, PushRouter.channelOf(update))
        assertNull(PushRouter.externalUrl(mapOf("type" to PushRouter.APP_ANNOUNCEMENT, "url" to "intent://evil")))
        assertEquals(PushDestination.None, PushRouter.route(mapOf("type" to PushRouter.APP_ANNOUNCEMENT)))
    }

    @Test
    fun `AC-021-07 match and chat pushes keep their channels`() {
        assertEquals(PushChannel.MATCH_UPDATES, PushRouter.channelOf(club + ("type" to PushRouter.ANNOUNCEMENT_OPENED)))
        assertEquals(PushChannel.MATCH_UPDATES, PushRouter.channelOf(club + ("type" to PushRouter.MATCH_COMPLETED)))
        assertEquals(PushChannel.CHAT, PushRouter.channelOf(mapOf("type" to "new_message", "chatId" to "ch-1")))
    }

    @Test
    fun `AC-021-07 a new-version push for the installed version or an older one is stale`() {
        val update = mapOf("type" to PushRouter.APP_UPDATE, "version" to "1.5.0", "url" to "https://play.google.com")

        assertTrue(PushRouter.isStaleUpdate(update, installedVersion = "1.5.0"))
        assertTrue(PushRouter.isStaleUpdate(update, installedVersion = "1.6.0"))
        assertFalse(PushRouter.isStaleUpdate(update, installedVersion = "1.4.9"))
        assertFalse(PushRouter.isStaleUpdate(update - "version", installedVersion = "1.5.0"))
        assertFalse(PushRouter.isStaleUpdate(club + ("type" to PushRouter.ANNOUNCEMENT_OPENED), installedVersion = "1.5.0"))
    }
}
