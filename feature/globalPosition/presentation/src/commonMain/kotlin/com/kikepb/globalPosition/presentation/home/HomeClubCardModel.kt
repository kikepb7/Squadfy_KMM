package com.kikepb.globalPosition.presentation.home

import com.kikepb.club.domain.model.ClubModel
import com.kikepb.club.domain.model.CurrentAnnouncementModel
import com.kikepb.club.domain.model.MyEnrollmentStatus
import com.kikepb.club.domain.policy.AnnouncementWindowPolicy
import com.kikepb.club.domain.policy.WindowState
import kotlin.time.Instant

/** What a home card knows about the club's current announcement (AC-010-01/06). */
sealed interface HomeAnnouncementStatus {
    data object Loading : HomeAnnouncementStatus
    data object NoMatch : HomeAnnouncementStatus
    data object Unavailable : HomeAnnouncementStatus
    data class Loaded(val current: CurrentAnnouncementModel, val timeZoneId: String?) : HomeAnnouncementStatus
}

data class HomeClubCardModel(
    val club: ClubModel,
    val status: HomeAnnouncementStatus = HomeAnnouncementStatus.Loading,
    val isEnrolling: Boolean = false
) {
    val current: CurrentAnnouncementModel? get() = (status as? HomeAnnouncementStatus.Loaded)?.current

    fun windowState(now: Instant): WindowState? = current?.let { AnnouncementWindowPolicy.state(it.announcement, now) }

    /** AC-010-02: open window and I am not signed up. */
    fun canEnroll(now: Instant): Boolean = windowState(now) == WindowState.OPEN && current?.myStatus == MyEnrollmentStatus.NOT_ENROLLED

    val freeSeats: Int? get() = current?.announcement?.let { (it.maxPlayers - it.confirmedCount).coerceAtLeast(0) }
}

/**
 * AC-010-03: open announcements where I am not signed up first, then the rest by match date,
 * and clubs without match (or unavailable) last.
 */
fun List<HomeClubCardModel>.sortedForHome(now: Instant): List<HomeClubCardModel> = sortedWith(
    compareBy<HomeClubCardModel> { card ->
        when {
            card.canEnroll(now) -> 0
            card.current != null -> 1
            else -> 2
        }
    }.thenBy { it.current?.matchScheduledAt ?: Instant.DISTANT_FUTURE }.thenBy { it.club.name.lowercase() }
)
