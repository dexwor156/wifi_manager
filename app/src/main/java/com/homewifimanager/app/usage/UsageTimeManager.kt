package com.homewifimanager.app.usage

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

object UsageTimeManager {

    private val FREE_START: LocalTime = LocalTime.MIDNIGHT
    private val FREE_END: LocalTime = LocalTime.of(8, 0)

    /**
     * Free period:
     * 12:00 AM <= time < 8:00 AM
     *
     * Metered period:
     * 8:00 AM <= time < 12:00 AM
     */
    fun isFreePeriod(
        dateTime: LocalDateTime = LocalDateTime.now()
    ): Boolean {
        val time = dateTime.toLocalTime()

        return !time.isBefore(FREE_START) &&
                time.isBefore(FREE_END)
    }

    fun isMeteredPeriod(
        dateTime: LocalDateTime = LocalDateTime.now()
    ): Boolean {
        return !isFreePeriod(dateTime)
    }

    /**
     * Date used for today's allowance.
     *
     * At midnight a new usage day begins.
     */
    fun usageDate(
        dateTime: LocalDateTime = LocalDateTime.now()
    ): LocalDate {
        return dateTime.toLocalDate()
    }

    /**
     * Returns the next important usage boundary.
     *
     * During the free period -> today at 8 AM.
     * During the metered period -> next midnight.
     */
    fun nextBoundary(
        dateTime: LocalDateTime = LocalDateTime.now()
    ): LocalDateTime {

        return if (isFreePeriod(dateTime)) {

            dateTime
                .toLocalDate()
                .atTime(FREE_END)

        } else {

            dateTime
                .toLocalDate()
                .plusDays(1)
                .atStartOfDay()
        }
    }
}