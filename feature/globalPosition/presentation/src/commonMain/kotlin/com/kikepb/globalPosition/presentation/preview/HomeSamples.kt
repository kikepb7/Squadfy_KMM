package com.kikepb.globalPosition.presentation.preview

import com.kikepb.club.domain.model.AnnouncementStatus
import com.kikepb.club.domain.model.ClubModel
import com.kikepb.club.domain.model.CurrentAnnouncementModel
import com.kikepb.club.domain.model.MatchAnnouncementModel
import com.kikepb.club.domain.model.MyEnrollmentStatus
import com.kikepb.core.designsystem.triangulacion.components.MatchResult
import com.kikepb.globalPosition.presentation.GlobalPositionUiState
import com.kikepb.globalPosition.presentation.home.HomeAnnouncementStatus
import com.kikepb.globalPosition.presentation.home.HomeClubCardModel
import com.kikepb.globalPosition.presentation.home.HomeLastMatchday
import kotlin.time.Instant

/** Sample Home of the mockup 03-inicio: Albaicín FC with an open sign-up, Cartuja 7 where I am called up. */
internal object HomeSamples {
    private const val ZONE = "Europe/Madrid"
    val now: Instant = Instant.parse("2026-10-13T16:45:00Z")

    private fun club(id: String, name: String, members: Int) = ClubModel(
        id = id, name = name, description = null, clubLogoUrl = null, ownerId = "u-1",
        invitationCode = "K7Q2M9AB", maxMembers = 20, membersCount = members
    )

    private fun current(clubId: String, matchAt: String, confirmed: Int, max: Int, mine: MyEnrollmentStatus) = CurrentAnnouncementModel(
        announcement = MatchAnnouncementModel(
            id = "a-$clubId", matchId = "m-$clubId", clubId = clubId, maxPlayers = max, confirmedCount = confirmed,
            waitlistCount = 0, opensAt = Instant.parse("2026-10-09T22:00:00Z"), closesAt = Instant.parse("2026-10-15T19:00:00Z"),
            drawAt = Instant.parse("2026-10-15T10:00:00Z"), status = AnnouncementStatus.OPEN, entries = emptyList(), waitlist = emptyList()
        ),
        matchScheduledAt = Instant.parse(matchAt),
        myStatus = mine,
        myWaitlistPosition = null
    )

    val cards = listOf(
        HomeClubCardModel(
            club = club("albaicin", "Albaicín FC", 14),
            status = HomeAnnouncementStatus.Loaded(current("albaicin", "2026-10-15T18:00:00Z", 9, 14, MyEnrollmentStatus.NOT_ENROLLED), ZONE)
        ),
        HomeClubCardModel(
            club = club("cartuja", "Cartuja 7", 12),
            status = HomeAnnouncementStatus.Loaded(current("cartuja", "2026-10-22T19:00:00Z", 6, 14, MyEnrollmentStatus.CONFIRMED), ZONE)
        )
    )

    val lastMatchday = HomeLastMatchday(
        clubId = "albaicin", clubName = "Albaicín FC", matchId = "m-last", scheduledAt = Instant.parse("2026-10-08T18:00:00Z"),
        timeZoneId = ZONE, myScore = 4, theirScore = 3, result = MatchResult.WIN, ratingChange = 12,
        form = listOf(MatchResult.WIN, MatchResult.DRAW, MatchResult.WIN)
    )

    val state = GlobalPositionUiState(
        now = now, userName = "Álvaro Gil", cards = cards, isLoadingClubs = false, hasFetchedClubs = true, lastMatchday = lastMatchday
    )
    val empty = GlobalPositionUiState(now = now, userName = "Álvaro Gil", isLoadingClubs = false, hasFetchedClubs = true)
}
