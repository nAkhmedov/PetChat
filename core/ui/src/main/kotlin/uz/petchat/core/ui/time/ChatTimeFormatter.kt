package uz.petchat.core.ui.time

import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject

enum class RelativeDay { TODAY, YESTERDAY, OTHER }

class ChatTimeFormatter @Inject constructor(
    private val clock: Clock,
) {
    private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")
    private val weekDayFormatter = DateTimeFormatter.ofPattern("EEE")
    private val shortDateFormatter = DateTimeFormatter.ofPattern("dd.MM.yy")
    private val dayMonthFormatter = DateTimeFormatter.ofPattern("d MMMM")
    private val dayMonthYearFormatter = DateTimeFormatter.ofPattern("d MMMM yyyy")

    fun localDate(epochMillis: Long): LocalDate = zoned(epochMillis).toLocalDate()

    fun time(epochMillis: Long): String = timeFormatter.format(zoned(epochMillis))

    fun chatListTimestamp(epochMillis: Long): String {
        val dateTime = zoned(epochMillis)
        val today = LocalDate.now(clock)
        val date = dateTime.toLocalDate()
        return when {
            date == today -> timeFormatter.format(dateTime)
            date.isAfter(today.minusDays(7)) -> weekDayFormatter.format(dateTime)
            else -> shortDateFormatter.format(dateTime)
        }
    }

    fun relativeDay(date: LocalDate): RelativeDay {
        val today = LocalDate.now(clock)
        return when (date) {
            today -> RelativeDay.TODAY
            today.minusDays(1) -> RelativeDay.YESTERDAY
            else -> RelativeDay.OTHER
        }
    }

    fun dayHeader(date: LocalDate): String =
        if (date.year == LocalDate.now(clock).year) {
            dayMonthFormatter.format(date)
        } else {
            dayMonthYearFormatter.format(date)
        }

    private fun zoned(epochMillis: Long): ZonedDateTime =
        Instant.ofEpochMilli(epochMillis).atZone(clock.zone)
}
