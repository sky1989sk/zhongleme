package com.lottery.app.domain.util

import com.lottery.app.domain.model.GenerateResult
import com.lottery.app.domain.model.LotteryRule
import com.lottery.app.domain.model.LotteryType

/**
 * 根据生成结果计算投注注数与购买金额。
 *
 * 定价规则：双色球与大乐透均为 2 元/注（普通投注）。
 * - 普通：注数 = 号码条数
 * - 复式：注数 = C(前区所选个数, 前区应选个数) × C(后区所选个数, 后区应选个数)
 * - 胆拖：注数 = C(前拖个数, 前区应选个数 - 前胆个数) × C(后拖个数, 后区应选个数 - 后胆个数)
 */
object PriceCalculator {

    private const val PRICE_PER_TICKET = 2

    fun calcTicketCount(result: GenerateResult, lotteryType: LotteryType): Int {
        val rule = LotteryRule.of(lotteryType)
        return when (result) {
            is GenerateResult.StandardResult -> result.numbers.size
            is GenerateResult.MultipleResult -> {
                val front = combination(result.numbers.frontNumbers.size, rule.frontPickCount)
                val back = combination(result.numbers.backNumbers.size, rule.backPickCount)
                front * back
            }
            is GenerateResult.DanTuoResult -> {
                val dt = result.danTuo
                val front = combination(dt.frontTuo.size, rule.frontPickCount - dt.frontDan.size)
                val back = combination(dt.backTuo.size, rule.backPickCount - dt.backDan.size)
                (front * back).coerceAtLeast(0)
            }
        }
    }

    fun calcTotalPrice(ticketCount: Int): Int = ticketCount * PRICE_PER_TICKET

    fun combination(n: Int, k: Int): Int {
        if (k < 0 || k > n) return 0
        if (k == 0 || k == n) return 1
        val kk = minOf(k, n - k)
        var result = 1L
        for (i in 0 until kk) {
            result = result * (n - i) / (i + 1)
        }
        return result.toInt()
    }
}
