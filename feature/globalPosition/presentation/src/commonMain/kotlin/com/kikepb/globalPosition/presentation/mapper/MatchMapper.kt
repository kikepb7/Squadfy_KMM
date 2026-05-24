package com.kikepb.globalPosition.presentation.mapper

import com.kikepb.globalPosition.domain.model.ClubModel
import com.kikepb.globalPosition.domain.model.MatchModel
import com.kikepb.globalPosition.domain.model.MatchStatus
import com.kikepb.globalPosition.presentation.model.MatchStatusUi
import com.kikepb.globalPosition.presentation.model.MatchUiModel
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.TimeZone
import kotlinx.datetime.number
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Instant

fun MatchModel.toUiModel(clubsById: Map<String, ClubModel> = emptyMap()): MatchUiModel {
    val club = clubsById[clubId]
    return MatchUiModel(
        id = id,
        clubId = clubId,
        clubName = club?.name,
        clubLogoUrl = club?.logoUrl,
        firstTeamName = "Equipo A",
        firstTeamCode = "A",
        secondTeamName = "Equipo B",
        secondTeamCode = "B",
        firstTeamScore = teamAScore,
        secondTeamScore = teamBScore,
        date = scheduledAt.formatForDisplay(),
        status = status.toUiModel(),
        enrolledCount = enrolledPlayers.size,
        matchday = null
    )
}

fun MatchStatus.toUiModel() = when (this) {
    MatchStatus.SCHEDULED -> MatchStatusUi.SCHEDULED
    MatchStatus.IN_PROGRESS -> MatchStatusUi.IN_PROGRESS
    MatchStatus.FINISHED -> MatchStatusUi.FINISHED
}

private fun Instant.formatForDisplay(): String {
    val dt = toLocalDateTime(TimeZone.currentSystemDefault())
    val day = when (dt.dayOfWeek) {
        DayOfWeek.MONDAY -> "Lun"
        DayOfWeek.TUESDAY -> "Mar"
        DayOfWeek.WEDNESDAY -> "Mié"
        DayOfWeek.THURSDAY -> "Jue"
        DayOfWeek.FRIDAY -> "Vie"
        DayOfWeek.SATURDAY -> "Sáb"
        DayOfWeek.SUNDAY -> "Dom"
    }
    val month = when (dt.month.number) {
        1 -> "Ene"; 2 -> "Feb"; 3 -> "Mar"; 4 -> "Abr"
        5 -> "May"; 6 -> "Jun"; 7 -> "Jul"; 8 -> "Ago"
        9 -> "Sep"; 10 -> "Oct"; 11 -> "Nov"; 12 -> "Dic"
        else -> ""
    }
    val hour = dt.hour.toString().padStart(2, '0')
    val minute = dt.minute.toString().padStart(2, '0')
    return "$day, $day $month · $hour:$minute"
}
