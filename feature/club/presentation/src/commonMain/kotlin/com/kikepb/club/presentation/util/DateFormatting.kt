package com.kikepb.club.presentation.util

import androidx.compose.runtime.Composable
import com.kikepb.club.presentation.mapper.formatted
import com.kikepb.club.presentation.mapper.label
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.resources.stringResource
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
