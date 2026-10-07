package com.kikepb.club.presentation.util

import androidx.compose.runtime.Composable
import com.kikepb.club.presentation.mapper.formatted
import com.kikepb.club.presentation.mapper.label
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.resources.stringResource
import squadfy_app.feature.club.presentation.generated.resources.Res
import squadfy_app.feature.club.presentation.generated.resources.duration_days_hours
import squadfy_app.feature.club.presentation.generated.resources.duration_hours_minutes
import squadfy_app.feature.club.presentation.generated.resources.duration_minutes
import kotlin.time.Duration
import kotlin.time.Instant

/**
 * APP-RN-03: match-cycle instants are shown in the club time zone, e.g. "Jue 15/10 20:00".
 * When the device is in another zone the club zone is appended.
 */
@Composable
fun formatDateTime(instant: Instant, timeZoneId: String): String {
    val zone = runCatching { TimeZone.of(timeZoneId) }.getOrDefault(TimeZone.currentSystemDefault())
    val local = instant.toLocalDateTime(zone)
    val day = stringResource(local.dayOfWeek.label).take(3)
    val date = "${local.day.toString().padStart(2, '0')}/${local.month.ordinal.plus(1).toString().padStart(2, '0')}"
    val suffix = if (zone.id != TimeZone.currentSystemDefault().id) " (${zone.id.substringAfterLast('/')})" else ""
    return "$day $date ${local.time.formatted()}$suffix"
}

/** Countdown text: "2 d 4 h", "5 h 12 min" or "3 min" (AC-005-03). */
@Composable
fun formatDuration(duration: Duration): String {
    val totalMinutes = duration.inWholeMinutes.coerceAtLeast(1)
    val days = (totalMinutes / (24 * 60)).toInt()
    val hours = ((totalMinutes / 60) % 24).toInt()
    val minutes = (totalMinutes % 60).toInt()
    return when {
        days > 0 -> stringResource(Res.string.duration_days_hours, days, hours)
        hours > 0 -> stringResource(Res.string.duration_hours_minutes, hours, minutes)
        else -> stringResource(Res.string.duration_minutes, minutes)
    }
}
