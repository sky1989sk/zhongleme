package com.lottery.app.domain.util

import com.lottery.app.domain.model.LotteryType
import java.util.Calendar
import java.util.TimeZone

/**
 * Calculates the lottery issue/period number for a given timestamp.
 *
 * SSQ (双色球): draws on Tue(3), Thu(5), Sun(1)
 * DLT (大乐透): draws on Mon(2), Wed(4), Sat(7)
 *
 * Issue format: YYYYNNN (e.g. "2026031" = 2026年第31期)
 * The cutoff for a draw day is 20:00 -- if before 20:00 on a draw day, this draw counts;
 * if after 20:00 on a draw day, the next draw counts.
 *
 * Draws scheduled during market suspension periods (春节/国庆休市) are excluded
 * from the issue count. Suspension dates are sourced from annual Ministry of Finance
 * announcements; future years without official data use estimates based on the
 * Chinese New Year date.
 */
object IssueCalculator {

    private val ZONE = TimeZone.getTimeZone("Asia/Shanghai")

    private val SSQ_DRAW_DAYS = setOf(Calendar.TUESDAY, Calendar.THURSDAY, Calendar.SUNDAY)
    private val DLT_DRAW_DAYS = setOf(Calendar.MONDAY, Calendar.WEDNESDAY, Calendar.SATURDAY)

    private data class SuspensionPeriod(
        val startMonth: Int, val startDay: Int,
        val endMonth: Int, val endDay: Int
    )

    /** Official Spring Festival suspensions (财政部公告). Update each December. */
    private val SPRING_FESTIVAL_SUSPENSIONS: Map<Int, SuspensionPeriod> = mapOf(
        2024 to SuspensionPeriod(2, 8, 2, 17),
        2025 to SuspensionPeriod(1, 27, 2, 5),
        2026 to SuspensionPeriod(2, 14, 2, 23),
    )

    /** Chinese New Year Day 1 (Gregorian) for years without official suspension data. */
    private val CHINESE_NEW_YEAR_DATES: Map<Int, Pair<Int, Int>> = mapOf(
        2027 to Pair(2, 6),
        2028 to Pair(1, 26),
        2029 to Pair(2, 13),
        2030 to Pair(2, 3),
        2031 to Pair(1, 23),
        2032 to Pair(2, 11),
        2033 to Pair(1, 31),
        2034 to Pair(2, 19),
        2035 to Pair(2, 8),
    )

    private val NATIONAL_DAY_SUSPENSION = SuspensionPeriod(10, 1, 10, 4)

    fun calculate(lotteryType: LotteryType, timestamp: Long): String {
        val drawDays = when (lotteryType) {
            LotteryType.SSQ -> SSQ_DRAW_DAYS
            LotteryType.DLT -> DLT_DRAW_DAYS
        }
        val nextDrawDate = findNextDrawDate(timestamp, drawDays)
        val issueIndex = countDrawsSinceYearStart(nextDrawDate, drawDays)
        val year = nextDrawDate.get(Calendar.YEAR)
        return "%d%03d".format(year, issueIndex)
    }

    private fun findNextDrawDate(timestamp: Long, drawDays: Set<Int>): Calendar {
        val cal = Calendar.getInstance(ZONE).apply { timeInMillis = timestamp }
        val dayOfWeek = cal.get(Calendar.DAY_OF_WEEK)
        val hour = cal.get(Calendar.HOUR_OF_DAY)

        if (dayOfWeek in drawDays && hour < 20 && !isInSuspension(cal)) {
            return cal
        }

        cal.add(Calendar.DAY_OF_YEAR, 1)
        while (cal.get(Calendar.DAY_OF_WEEK) !in drawDays || isInSuspension(cal)) {
            cal.add(Calendar.DAY_OF_YEAR, 1)
        }
        return cal
    }

    private fun countDrawsSinceYearStart(drawDate: Calendar, drawDays: Set<Int>): Int {
        val year = drawDate.get(Calendar.YEAR)
        val start = Calendar.getInstance(ZONE).apply {
            set(year, Calendar.JANUARY, 1, 0, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }
        var count = 0
        val cursor = start.clone() as Calendar
        while (!cursor.after(drawDate)) {
            if (cursor.get(Calendar.DAY_OF_WEEK) in drawDays && !isInSuspension(cursor)) {
                count++
            }
            if (cursor.get(Calendar.YEAR) == drawDate.get(Calendar.YEAR) &&
                cursor.get(Calendar.DAY_OF_YEAR) == drawDate.get(Calendar.DAY_OF_YEAR)
            ) {
                break
            }
            cursor.add(Calendar.DAY_OF_YEAR, 1)
        }
        return count
    }

    private fun getSuspensionPeriods(year: Int): List<SuspensionPeriod> {
        val periods = mutableListOf(NATIONAL_DAY_SUSPENSION)
        val spring = SPRING_FESTIVAL_SUSPENSIONS[year]
        if (spring != null) {
            periods.add(spring)
        } else {
            val cny = CHINESE_NEW_YEAR_DATES[year]
            if (cny != null) {
                val estimated = estimateSpringFestivalSuspension(year, cny.first, cny.second)
                periods.add(estimated)
            }
        }
        return periods
    }

    private fun estimateSpringFestivalSuspension(
        year: Int, cnyMonth: Int, cnyDay: Int
    ): SuspensionPeriod {
        val cal = Calendar.getInstance(ZONE).apply {
            set(year, cnyMonth - 1, cnyDay, 0, 0, 0)
        }
        cal.add(Calendar.DAY_OF_YEAR, -3)
        val startMonth = cal.get(Calendar.MONTH) + 1
        val startDay = cal.get(Calendar.DAY_OF_MONTH)

        cal.add(Calendar.DAY_OF_YEAR, 9)
        val endMonth = cal.get(Calendar.MONTH) + 1
        val endDay = cal.get(Calendar.DAY_OF_MONTH)

        return SuspensionPeriod(startMonth, startDay, endMonth, endDay)
    }

    private fun isInSuspension(date: Calendar): Boolean {
        val year = date.get(Calendar.YEAR)
        val month = date.get(Calendar.MONTH) + 1
        val day = date.get(Calendar.DAY_OF_MONTH)
        val dayValue = month * 100 + day

        for (period in getSuspensionPeriods(year)) {
            val start = period.startMonth * 100 + period.startDay
            val end = period.endMonth * 100 + period.endDay
            if (dayValue in start..end) return true
        }
        return false
    }
}
