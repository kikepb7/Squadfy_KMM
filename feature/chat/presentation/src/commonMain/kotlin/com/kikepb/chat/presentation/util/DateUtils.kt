package com.kikepb.chat.presentation.util

import com.kikepb.core.presentation.util.UiText
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.format
import kotlinx.datetime.format.char
import kotlinx.datetime.minus
import kotlinx.datetime.toLocalDateTime
import squadfy_app.feature.chat.presentation.generated.resources.Res
import squadfy_app.feature.chat.presentation.generated.resources.today
import squadfy_app.feature.chat.presentation.generated.resources.yesterday
import kotlin.time.Clock
import kotlin.time.Instant

object DateUtils {

    fun formatMessageTime(instant: Instant, clock: Clock= Clock.System): UiText {
        val timeZone = TimeZone.currentSystemDefault()
        val messageDateTime = instant.toLocalDateTime(timeZone = timeZone)
        val todayDate = clock.now().toLocalDateTime(timeZone = timeZone).date
        val yesterdayDate = todayDate.minus(value = 1, unit = DateTimeUnit.DAY)

        val formattedDateTime = messageDateTime.format(
            LocalDateTime.Format {
                day()
                char(value ='/')
                monthNumber()
                char(value = '/')
                year()
                char(value = '/')
                amPmHour()
                char(value = ':')
                minute()
                amPmMarker(am = "am", pm = "pm")
            }
        )

        return when (messageDateTime.date) {
            todayDate -> UiText.Resource(id = Res.string.today)
            yesterdayDate -> UiText.Resource(id = Res.string.yesterday)
            else -> UiText.DynamicString(value = formattedDateTime)
        }
    }
}