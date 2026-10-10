package com.kikepb.globalPosition.presentation.home

import com.kikepb.club.domain.model.MatchModel
import com.kikepb.club.domain.model.MatchStatus
import com.kikepb.club.domain.model.Team
import com.kikepb.core.designsystem.triangulacion.components.MatchResult
import kotlin.time.Instant

/** «Última jornada» on Home (spec 018): my last completed match, its result, my rating change and my form. */
data class HomeLastMatchday(
    val clubId: String,
    val clubName: String,
    val matchId: String,
    val scheduledAt: Instant,
    val timeZoneId: String?,
    val myScore: Int,
    val theirScore: Int,
    val result: MatchResult,
    val ratingChange: Int?,
    /** Oldest first, at most five. */
    val form: List<MatchResult>
)

object HomeLastMatchdayPolicy {
    private const val FORM_SIZE = 5

    /** My last completed match of one club, or null when I have not played any. */
    fun of(clubId: String, clubName: String, clubMemberId: String, matches: List<MatchModel>, timeZoneId: String?): HomeLastMatchday? {
        val played = matches
            .filter { it.status == MatchStatus.COMPLETED && it.teamOf(clubMemberId) != null }
            .sortedBy { it.scheduledAt }
        val last = played.lastOrNull() ?: return null
        val (mine, theirs) = scoresFor(last, clubMemberId)
        return HomeLastMatchday(
            clubId = clubId,
            clubName = clubName,
            matchId = last.id,
            scheduledAt = last.scheduledAt,
            timeZoneId = timeZoneId,
            myScore = mine,
            theirScore = theirs,
            result = resultOf(mine, theirs),
            ratingChange = last.ratingChanges[clubMemberId],
            form = played.takeLast(FORM_SIZE).map { match -> scoresFor(match, clubMemberId).let { (m, t) -> resultOf(m, t) } }
        )
    }

    /** The most recent among my clubs. */
    fun latest(candidates: List<HomeLastMatchday>): HomeLastMatchday? = candidates.maxByOrNull { it.scheduledAt }

    private fun scoresFor(match: MatchModel, clubMemberId: String): Pair<Int, Int> =
        if (match.teamOf(clubMemberId) == Team.A) match.teamAScore to match.teamBScore else match.teamBScore to match.teamAScore

    private fun resultOf(mine: Int, theirs: Int): MatchResult = when {
        mine > theirs -> MatchResult.WIN
        mine < theirs -> MatchResult.LOSS
        else -> MatchResult.DRAW
    }
}
