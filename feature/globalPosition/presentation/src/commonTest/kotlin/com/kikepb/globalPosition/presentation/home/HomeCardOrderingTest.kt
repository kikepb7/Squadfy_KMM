package com.kikepb.globalPosition.presentation.home

import com.kikepb.club.domain.model.AnnouncementStatus
import com.kikepb.club.domain.model.ClubModel
import com.kikepb.club.domain.model.CurrentAnnouncementModel
import com.kikepb.club.domain.model.MatchAnnouncementModel
import com.kikepb.club.domain.model.MyEnrollmentStatus
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.time.Instant

class HomeCardOrderingTest {

    private val now = Instant.parse("2026-10-07T12:00:00Z")

    @Test
    fun `AC-010-03 open announcements without me first, then by match date, then clubs without match`() {
        val cards = listOf(
            card("none", HomeAnnouncementStatus.NoMatch),
            card("later", loaded(match = "2026-10-20T18:00:00Z", status = MyEnrollmentStatus.CONFIRMED)),
            card("open-not-me", loaded(match = "2026-10-25T18:00:00Z", status = MyEnrollmentStatus.NOT_ENROLLED)),
            card("error", HomeAnnouncementStatus.Unavailable),
            card("sooner", loaded(match = "2026-10-09T18:00:00Z", status = MyEnrollmentStatus.WAITLISTED))
        )

        assertEquals(listOf("open-not-me", "sooner", "later", "error", "none"), cards.sortedForHome(now).map { it.club.id })
    }

    @Test
    fun `AC-010-02 the sign-up shortcut needs an open window and me not signed up`() {
        assertTrue(card("a", loaded(status = MyEnrollmentStatus.NOT_ENROLLED)).canEnroll(now))
        assertFalse(card("b", loaded(status = MyEnrollmentStatus.CONFIRMED)).canEnroll(now))
        assertFalse(card("c", loaded(status = MyEnrollmentStatus.NOT_ENROLLED, closesAt = "2026-10-06T20:00:00Z")).canEnroll(now))
    }

    @Test
    fun `AC-010-01 free spots never go below zero`() {
        assertEquals(7, card("a", loaded(confirmed = 3)).freeSeats)
        assertEquals(0, card("b", loaded(confirmed = 12)).freeSeats)
    }

    private fun card(id: String, status: HomeAnnouncementStatus) = HomeClubCardModel(
        club = ClubModel(id = id, name = id, description = null, clubLogoUrl = null, ownerId = "o", invitationCode = "X", maxMembers = null, membersCount = 1),
        status = status
    )

    private fun loaded(
        match: String = "2026-10-15T18:00:00Z",
        status: MyEnrollmentStatus = MyEnrollmentStatus.NOT_ENROLLED,
        closesAt: String = "2026-10-14T20:00:00Z",
        confirmed: Int = 0
    ) = HomeAnnouncementStatus.Loaded(
        current = CurrentAnnouncementModel(
            announcement = MatchAnnouncementModel(
                id = "a", matchId = "m", clubId = "c", maxPlayers = 10, confirmedCount = confirmed, waitlistCount = 0,
                opensAt = Instant.parse("2026-10-01T00:00:00Z"), closesAt = Instant.parse(closesAt), drawAt = Instant.parse(closesAt),
                status = AnnouncementStatus.OPEN, entries = emptyList(), waitlist = emptyList()
            ),
            matchScheduledAt = Instant.parse(match),
            myStatus = status,
            myWaitlistPosition = null
        ),
        timeZoneId = "Europe/Madrid"
    )
}
