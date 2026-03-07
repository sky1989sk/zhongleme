package com.lottery.app.domain.model

data class LotteryRule(
    val lotteryType: LotteryType,
    val frontMin: Int,
    val frontMax: Int,
    val frontPickCount: Int,
    val backMin: Int,
    val backMax: Int,
    val backPickCount: Int,
    val frontMultipleMin: Int,
    val frontMultipleMax: Int,
    val backMultipleMin: Int,
    val backMultipleMax: Int,
    val frontDanMax: Int,
    val backDanMax: Int
) {
    companion object {
        val SSQ = LotteryRule(
            lotteryType = LotteryType.SSQ,
            frontMin = 1, frontMax = 33, frontPickCount = 6,
            backMin = 1, backMax = 16, backPickCount = 1,
            frontMultipleMin = 7, frontMultipleMax = 20,
            backMultipleMin = 1, backMultipleMax = 16,
            frontDanMax = 5,
            backDanMax = 0
        )

        val DLT = LotteryRule(
            lotteryType = LotteryType.DLT,
            frontMin = 1, frontMax = 35, frontPickCount = 5,
            backMin = 1, backMax = 12, backPickCount = 2,
            frontMultipleMin = 6, frontMultipleMax = 18,
            backMultipleMin = 3, backMultipleMax = 8,
            frontDanMax = 4,
            backDanMax = 1
        )

        fun of(type: LotteryType): LotteryRule = when (type) {
            LotteryType.SSQ -> SSQ
            LotteryType.DLT -> DLT
        }
    }
}
