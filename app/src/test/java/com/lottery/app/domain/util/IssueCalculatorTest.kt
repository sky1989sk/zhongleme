package com.lottery.app.domain.util

import com.lottery.app.domain.model.LotteryType
import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.Calendar
import java.util.TimeZone

class IssueCalculatorTest {

    private val zone = TimeZone.getTimeZone("Asia/Shanghai")

    private fun ts(year: Int, month: Int, day: Int, hour: Int = 12): Long {
        return Calendar.getInstance(zone).apply {
            set(year, month - 1, day, hour, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }

    // ---- DLT (大乐透): Mon, Wed, Sat ----

    @Test
    fun `DLT 2026-01-03 Sat first draw of year`() {
        assertEquals("2026001", IssueCalculator.calculate(LotteryType.DLT, ts(2026, 1, 3)))
    }

    @Test
    fun `DLT 2026-01-05 Mon second draw`() {
        assertEquals("2026002", IssueCalculator.calculate(LotteryType.DLT, ts(2026, 1, 5)))
    }

    @Test
    fun `DLT 2026-02-11 Wed last draw before spring festival`() {
        assertEquals("2026018", IssueCalculator.calculate(LotteryType.DLT, ts(2026, 2, 11)))
    }

    @Test
    fun `DLT during spring festival suspension maps to first draw after`() {
        // 2026 suspension: Feb 14-23. First DLT draw after = Wed Feb 25
        assertEquals("2026019", IssueCalculator.calculate(LotteryType.DLT, ts(2026, 2, 15)))
        assertEquals("2026019", IssueCalculator.calculate(LotteryType.DLT, ts(2026, 2, 20)))
    }

    @Test
    fun `DLT 2026-02-14 Sat suspension start`() {
        assertEquals("2026019", IssueCalculator.calculate(LotteryType.DLT, ts(2026, 2, 14)))
    }

    @Test
    fun `DLT 2026-02-23 Mon suspension end`() {
        assertEquals("2026019", IssueCalculator.calculate(LotteryType.DLT, ts(2026, 2, 23)))
    }

    @Test
    fun `DLT 2026-02-25 Wed first draw after spring festival`() {
        assertEquals("2026019", IssueCalculator.calculate(LotteryType.DLT, ts(2026, 2, 25)))
    }

    @Test
    fun `DLT 2026-03-07 Sat matches real ticket 26023`() {
        assertEquals("2026023", IssueCalculator.calculate(LotteryType.DLT, ts(2026, 3, 7)))
    }

    @Test
    fun `DLT non-draw day maps to next draw`() {
        // 2026-01-02 is Friday -> next DLT draw is Sat Jan 3
        assertEquals("2026001", IssueCalculator.calculate(LotteryType.DLT, ts(2026, 1, 2)))
    }

    @Test
    fun `DLT after 20h cutoff maps to next draw`() {
        // Sat Jan 3 after 20:00 -> next draw Mon Jan 5
        assertEquals("2026002", IssueCalculator.calculate(LotteryType.DLT, ts(2026, 1, 3, 21)))
    }

    // ---- SSQ (双色球): Tue, Thu, Sun ----

    @Test
    fun `SSQ 2026-01-01 Thu first draw of year`() {
        assertEquals("2026001", IssueCalculator.calculate(LotteryType.SSQ, ts(2026, 1, 1)))
    }

    @Test
    fun `SSQ 2026-01-04 Sun second draw`() {
        assertEquals("2026002", IssueCalculator.calculate(LotteryType.SSQ, ts(2026, 1, 4)))
    }

    @Test
    fun `SSQ 2026-02-12 Thu last SSQ draw before spring festival`() {
        assertEquals("2026019", IssueCalculator.calculate(LotteryType.SSQ, ts(2026, 2, 12)))
    }

    @Test
    fun `SSQ during spring festival suspension maps to first draw after`() {
        assertEquals("2026020", IssueCalculator.calculate(LotteryType.SSQ, ts(2026, 2, 15)))
    }

    @Test
    fun `SSQ 2026-02-24 Tue first draw after spring festival`() {
        assertEquals("2026020", IssueCalculator.calculate(LotteryType.SSQ, ts(2026, 2, 24)))
    }

    @Test
    fun `SSQ 2026-03-07 Sat maps to next SSQ draw Sun Mar 8`() {
        assertEquals("2026025", IssueCalculator.calculate(LotteryType.SSQ, ts(2026, 3, 7)))
    }

    // ---- National Day suspension ----

    @Test
    fun `DLT national day suspension Oct 1-4`() {
        // Oct 1, 2026 is Thursday (non-draw day for DLT).
        // Oct 3, 2026 is Saturday (draw day but in suspension) -> next draw Mon Oct 5
        val resultOct3 = IssueCalculator.calculate(LotteryType.DLT, ts(2026, 10, 3))
        val resultOct5 = IssueCalculator.calculate(LotteryType.DLT, ts(2026, 10, 5))
        assertEquals(resultOct5, resultOct3)
    }

    @Test
    fun `SSQ national day suspension Oct 1-4`() {
        // Oct 1, 2026 is Thursday (draw day for SSQ but in suspension) -> next draw Sun Oct 5? Let's check.
        // Oct 4 = Sunday but in suspension. Next SSQ draw = Tue Oct 6.
        val resultOct1 = IssueCalculator.calculate(LotteryType.SSQ, ts(2026, 10, 1))
        val resultOct6 = IssueCalculator.calculate(LotteryType.SSQ, ts(2026, 10, 6))
        assertEquals(resultOct6, resultOct1)
    }

    // ---- 2025 Spring Festival (Jan 27 - Feb 5) ----

    @Test
    fun `DLT 2025 spring festival boundary`() {
        // 2025-01-25 Sat = last DLT draw before suspension
        // 2025 suspension: Jan 27 - Feb 5
        // Next DLT draw after suspension: Feb 8 Sat (Feb 6 Thu is not DLT day, Feb 7 Fri no, Feb 8 Sat yes)
        // Wait: DLT = Mon, Wed, Sat. Feb 6 is Thu, Feb 7 Fri, Feb 8 Sat. So first DLT after = Sat Feb 8? 
        // Actually let me check: Jan 27 Mon is in suspension, Jan 29 Wed in suspension, Feb 1 Sat in suspension, 
        // Feb 3 Mon in suspension, Feb 5 Wed in suspension.
        // Feb 6 is Thu (not DLT). Feb 7 Fri (not DLT). Feb 8 Sat = first DLT after suspension.
        val beforeSuspension = IssueCalculator.calculate(LotteryType.DLT, ts(2025, 1, 25))
        val duringSuspension = IssueCalculator.calculate(LotteryType.DLT, ts(2025, 1, 28))
        val afterSuspension = IssueCalculator.calculate(LotteryType.DLT, ts(2025, 2, 8))
        assertEquals(afterSuspension, duringSuspension)
        // Count: Jan 1 Wed(1), Jan 4 Sat(2), Jan 6 Mon(3), Jan 8 Wed(4), Jan 11 Sat(5),
        // Jan 13 Mon(6), Jan 15 Wed(7), Jan 18 Sat(8), Jan 20 Mon(9), Jan 22 Wed(10),
        // Jan 25 Sat(11). So before = 2025011. After suspension = 2025012.
        assertEquals("2025011", beforeSuspension)
        assertEquals("2025012", afterSuspension)
    }
}
