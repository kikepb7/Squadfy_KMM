package com.kikepb.club.domain.policy

import com.kikepb.club.domain.model.MatchModel
import com.kikepb.club.domain.model.MatchStatus
import kotlin.time.Instant

/** Which match-management actions a manager is offered (spec 007, BE-004). The backend still validates them. */
object MatchCyclePolicy {

    /** AC-007-01/04/10: events, minutes and the manual score while SCHEDULED with teams. */
    fun canRecord(match: MatchModel, isManager: Boolean): Boolean =
        isManager && match.status == MatchStatus.SCHEDULED && match.hasTeams

    /** AC-007-05: SCHEDULED, already started and with teams. */
    fun canComplete(match: MatchModel, isManager: Boolean, now: Instant): Boolean =
        canRecord(match, isManager) && now >= match.scheduledAt

    /** AC-007-06: only the latest completed match of the club (BE-004 RN-5). */
    fun canReopen(match: MatchModel, isManager: Boolean, latestCompletedId: String?): Boolean =
        isManager && match.status == MatchStatus.COMPLETED && match.id == latestCompletedId

    /** AC-007-07: never once completed or already cancelled. */
    fun canCancel(match: MatchModel, isManager: Boolean): Boolean =
        isManager && match.status == MatchStatus.SCHEDULED

    /** AC-007-04: from 0 to the match duration. */
    fun isValidMinutes(match: MatchModel, minutes: Int?): Boolean = minutes != null && minutes in 0..match.durationMinutes
}
