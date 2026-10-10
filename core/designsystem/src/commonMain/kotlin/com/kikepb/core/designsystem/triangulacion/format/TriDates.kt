package com.kikepb.core.designsystem.triangulacion.format

import androidx.compose.runtime.Composable
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.isoDayNumber
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import squadfy_app.core.designsystem.generated.resources.Res
import squadfy_app.core.designsystem.generated.resources.tri_date_day_month
import squadfy_app.core.designsystem.generated.resources.tri_date_full
import squadfy_app.core.designsystem.generated.resources.tri_wd_short_1
import squadfy_app.core.designsystem.generated.resources.tri_wd_short_2
import squadfy_app.core.designsystem.generated.resources.tri_wd_short_3
import squadfy_app.core.designsystem.generated.resources.tri_wd_short_4
import squadfy_app.core.designsystem.generated.resources.tri_wd_short_5
import squadfy_app.core.designsystem.generated.resources.tri_wd_short_6
import squadfy_app.core.designsystem.generated.resources.tri_wd_short_7
import squadfy_app.core.designsystem.generated.resources.tri_wd_long_1
import squadfy_app.core.designsystem.generated.resources.tri_wd_long_2
import squadfy_app.core.designsystem.generated.resources.tri_wd_long_3
import squadfy_app.core.designsystem.generated.resources.tri_wd_long_4
import squadfy_app.core.designsystem.generated.resources.tri_wd_long_5
import squadfy_app.core.designsystem.generated.resources.tri_wd_long_6
import squadfy_app.core.designsystem.generated.resources.tri_wd_long_7
import squadfy_app.core.designsystem.generated.resources.tri_mo_1
import squadfy_app.core.designsystem.generated.resources.tri_mo_2
import squadfy_app.core.designsystem.generated.resources.tri_mo_3
import squadfy_app.core.designsystem.generated.resources.tri_mo_4
import squadfy_app.core.designsystem.generated.resources.tri_mo_5
import squadfy_app.core.designsystem.generated.resources.tri_mo_6
import squadfy_app.core.designsystem.generated.resources.tri_mo_7
import squadfy_app.core.designsystem.generated.resources.tri_mo_8
import squadfy_app.core.designsystem.generated.resources.tri_mo_9
import squadfy_app.core.designsystem.generated.resources.tri_mo_10
import squadfy_app.core.designsystem.generated.resources.tri_mo_11
import squadfy_app.core.designsystem.generated.resources.tri_mo_12
import kotlin.time.Instant

/**
 * Dates of the match cycle as the kit writes them (spec 018, APP-RN-03): «Jue 15 oct · 20:00», «JUEVES 15 OCT»,
 * always in the club time zone; when the device is elsewhere the city is added: «20:00 (Madrid)».
 */
object TriDates {
    private val shortDays = listOf(Res.string.tri_wd_short_1, Res.string.tri_wd_short_2, Res.string.tri_wd_short_3, Res.string.tri_wd_short_4, Res.string.tri_wd_short_5, Res.string.tri_wd_short_6, Res.string.tri_wd_short_7)
    private val longDays = listOf(Res.string.tri_wd_long_1, Res.string.tri_wd_long_2, Res.string.tri_wd_long_3, Res.string.tri_wd_long_4, Res.string.tri_wd_long_5, Res.string.tri_wd_long_6, Res.string.tri_wd_long_7)
    private val months = listOf(Res.string.tri_mo_1, Res.string.tri_mo_2, Res.string.tri_mo_3, Res.string.tri_mo_4, Res.string.tri_mo_5, Res.string.tri_mo_6, Res.string.tri_mo_7, Res.string.tri_mo_8, Res.string.tri_mo_9, Res.string.tri_mo_10, Res.string.tri_mo_11, Res.string.tri_mo_12)

    fun zoneOf(timeZoneId: String?): TimeZone =
        timeZoneId?.let { runCatching { TimeZone.of(it) }.getOrNull() } ?: TimeZone.currentSystemDefault()

    fun local(instant: Instant, timeZoneId: String?): LocalDateTime = instant.toLocalDateTime(zoneOf(timeZoneId))

    fun shortDay(day: DayOfWeek): StringResource = shortDays[day.isoDayNumber - 1]
    fun longDay(day: DayOfWeek): StringResource = longDays[day.isoDayNumber - 1]
    fun month(monthNumber: Int): StringResource = months[monthNumber - 1]

    fun time(local: LocalDateTime): String = "${local.hour.toString().padStart(2, '0')}:${local.minute.toString().padStart(2, '0')}"

    /** «(Madrid)» when the device is not in the club zone, otherwise empty. */
    fun zoneSuffix(timeZoneId: String?): String {
        val zone = zoneOf(timeZoneId)
        return if (zone.id != TimeZone.currentSystemDefault().id) " (${zone.id.substringAfterLast('/').replace('_', ' ')})" else ""
    }
}

/** «Jue 15 oct · 20:00». */
@Composable
fun formatMatchDate(instant: Instant, timeZoneId: String?): String {
    val local = TriDates.local(instant, timeZoneId)
    return stringResource(
        Res.string.tri_date_full,
        stringResource(TriDates.shortDay(local.dayOfWeek)),
        local.day,
        stringResource(TriDates.month(local.month.ordinal + 1)),
        TriDates.time(local)
    )
}

/** «15 oct». */
@Composable
fun formatDayMonth(date: LocalDate): String =
    stringResource(Res.string.tri_date_day_month, date.day, stringResource(TriDates.month(date.month.ordinal + 1)))

/** «JUEVES 15 OCT» for the caps line above a title. */
@Composable
fun formatLongDayCaps(date: LocalDate): String =
    "${stringResource(TriDates.longDay(date.dayOfWeek))} ${formatDayMonth(date)}".uppercase()

/** «20:00 (Madrid)» or «20:00». */
@Composable
fun formatClubTime(instant: Instant, timeZoneId: String?): String =
    TriDates.time(TriDates.local(instant, timeZoneId)) + TriDates.zoneSuffix(timeZoneId)
