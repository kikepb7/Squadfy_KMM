package com.kikepb.chat.data.websocket

import com.kikepb.chat.data.websocket.remote.parseClubDataChange
import com.kikepb.core.data.networking.squadfyJson
import com.kikepb.core.domain.realtime.ClubDataChange
import com.kikepb.core.domain.realtime.ClubDataScope
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/** Payloads as the backend sends them (spec 012 RN-A, `ChatWebSocketIntegrationTest`). */
class ClubDataChangedParsingTest {

    @Test
    fun `AC-015-06 a match change carries its club and match`() {
        assertEquals(
            ClubDataChange(clubId = "c-1", scope = ClubDataScope.MATCH, matchId = "m-1"),
            parseClubDataChange(squadfyJson, """{"clubId":"c-1","scope":"MATCH","matchId":"m-1"}""")
        )
    }

    @Test
    fun `AC-015-06 schedule and absence changes have no match`() {
        assertEquals(
            ClubDataChange(clubId = "c-1", scope = ClubDataScope.SCHEDULE),
            parseClubDataChange(squadfyJson, """{"clubId":"c-1","scope":"SCHEDULE","matchId":null}""")
        )
        assertEquals(ClubDataScope.ABSENCES, parseClubDataChange(squadfyJson, """{"clubId":"c-1","scope":"ABSENCES"}""")?.scope)
    }

    @Test
    fun `AC-015-06 unknown scopes and malformed payloads are ignored`() {
        assertNull(parseClubDataChange(squadfyJson, """{"clubId":"c-1","scope":"BILLING"}"""))
        assertNull(parseClubDataChange(squadfyJson, "not json"))
    }
}
