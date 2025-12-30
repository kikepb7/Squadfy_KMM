package com.kikepb.chat.presentation.util

import com.kikepb.core.presentation.util.UiText
import kotlinx.datetime.DateTimeUnit.Companion.DAY
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.format
import kotlinx.datetime.format.char
import kotlinx.datetime.minus
import kotlinx.datetime.toLocalDateTime
import squadfy_app.feature.chat.presentation.generated.resources.Res.string as RString
import squadfy_app.feature.chat.presentation.generated.resources.today
import squadfy_app.feature.chat.presentation.generated.resources.today_value
import squadfy_app.feature.chat.presentation.generated.resources.yesterday
import squadfy_app.feature.chat.presentation.generated.resources.yesterday_value
import kotlin.time.Clock
import kotlin.time.Instant

object DateUtils {

    fun formatMessageTime(instant: Instant, clock: Clock= Clock.System): UiText {
        val timeZone = TimeZone.currentSystemDefault()
        val messageDateTime = instant.toLocalDateTime(timeZone = timeZone)
        val todayDate = clock.now().toLocalDateTime(timeZone = timeZone).date
        val yesterdayDate = todayDate.minus(value = 1, unit = DAY)

        val formattedTime = messageDateTime.format(
            format = LocalDateTime.Format {
                amPmHour()
                char(value = ':')
                minute()
                amPmMarker(am = "am", pm = "pm")
            }
        )
        val formattedDateTime = messageDateTime.format(
            LocalDateTime.Format {
                day()
                char(value ='/')
                monthNumber()
                char(value = '/')
                year()
                char(value = '/')
                chars(value = formattedTime)
            }
        )

        return when (messageDateTime.date) {
            todayDate -> UiText.Resource(id = RString.today_value, args = arrayOf(formattedTime))
            yesterdayDate -> UiText.Resource(id = RString.yesterday_value, args = arrayOf(formattedTime))
            else -> UiText.DynamicString(value = formattedDateTime)
        }
    }

    fun formatDateSeparator(date: LocalDate, clock: Clock = Clock.System): UiText {
        val timeZone = TimeZone.currentSystemDefault()
        val today = clock.now().toLocalDateTime(timeZone = timeZone).date
        val yesterday = today.minus(value = 1, unit = DAY)

        return when (date) {
            today -> UiText.Resource(RString.today)
            yesterday -> UiText.Resource(RString.yesterday)
            else -> {
                val formatted = date.format(
                    format = LocalDate.Format {
                        day()
                        char(value ='/')
                        monthNumber()
                        char(value = '/')
                        year()
                    }
                )
                UiText.DynamicString(value = formatted)
            }
        }
    }
}