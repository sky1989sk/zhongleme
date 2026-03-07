package com.lottery.app.domain.generator

import com.lottery.app.domain.model.*

interface LotteryGenerator {

    fun generateStandard(
        lotteryType: LotteryType,
        count: Int = 1,
        strategy: GeneratorStrategy = GeneratorStrategy.PURE_RANDOM
    ): List<LotteryNumber>

    fun generateMultiple(
        lotteryType: LotteryType,
        config: MultipleConfig
    ): LotteryNumber

    fun generateDanTuo(
        lotteryType: LotteryType,
        config: DanTuoConfig
    ): DanTuoNumber
}
