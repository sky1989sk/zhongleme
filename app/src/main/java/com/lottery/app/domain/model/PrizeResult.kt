package com.lottery.app.domain.model

data class PrizeResult(
    val lotteryType: LotteryType,
    val issue: String,
    val drawNumbers: LotteryNumber,
    val drawTime: Long
)
