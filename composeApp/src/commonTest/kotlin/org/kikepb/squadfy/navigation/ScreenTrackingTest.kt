package org.kikepb.squadfy.navigation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class ScreenTrackingTest {

    @Test
    fun `AC-019-18 the screen name is the route class without its arguments`() {
        assertEquals(
            "ClubDetail",
            screenNameOf("com.kikepb.club.presentation.navigation.ClubGraphRoutes.ClubDetailRoute/{clubId}?logoUploadFailed={logoUploadFailed}")
        )
        assertEquals("MatchResult", screenNameOf("com.kikepb.club.presentation.navigation.ClubGraphRoutes.MatchResultRoute/{clubId}/{matchId}"))
        assertEquals("ClubsList", screenNameOf("com.kikepb.club.presentation.navigation.ClubGraphRoutes.ClubsListRoute"))
        assertEquals("Login", screenNameOf("com.kikepb.auth.presentation.navigation.AuthGraphRoutes.Login"))
    }

    @Test
    fun `AC-019-18 a destination without a route is not logged`() {
        assertNull(screenNameOf(null))
        assertNull(screenNameOf(""))
    }
}
