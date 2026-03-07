package com.lottery.app.usecase

import com.lottery.app.domain.generator.LotteryGenerator
import com.lottery.app.domain.model.*
import com.lottery.app.domain.repository.HistoryRepository
import com.lottery.app.domain.util.IssueCalculator

class GenerateNumbersUseCase(
    private val generator: LotteryGenerator,
    private val historyRepository: HistoryRepository
) {
    suspend fun generateStandard(
        lotteryType: LotteryType,
        count: Int,
        strategy: GeneratorStrategy = GeneratorStrategy.PURE_RANDOM
    ): GenerateResult.StandardResult {
        val numbers = generator.generateStandard(lotteryType, count, strategy)
        val result = GenerateResult.StandardResult(numbers)
        val now = System.currentTimeMillis()
        historyRepository.saveRecord(
            HistoryRecord(
                lotteryType = lotteryType,
                playType = PlayType.STANDARD,
                result = result,
                issueNumber = IssueCalculator.calculate(lotteryType, now),
                createdAt = now
            )
        )
        return result
    }

    suspend fun generateMultiple(lotteryType: LotteryType, config: MultipleConfig): GenerateResult.MultipleResult {
        val numbers = generator.generateMultiple(lotteryType, config)
        val result = GenerateResult.MultipleResult(numbers)
        val now = System.currentTimeMillis()
        historyRepository.saveRecord(
            HistoryRecord(
                lotteryType = lotteryType,
                playType = PlayType.MULTIPLE,
                result = result,
                issueNumber = IssueCalculator.calculate(lotteryType, now),
                createdAt = now
            )
        )
        return result
    }

    suspend fun generateDanTuo(lotteryType: LotteryType, config: DanTuoConfig): GenerateResult.DanTuoResult {
        val danTuo = generator.generateDanTuo(lotteryType, config)
        val result = GenerateResult.DanTuoResult(danTuo)
        val now = System.currentTimeMillis()
        historyRepository.saveRecord(
            HistoryRecord(
                lotteryType = lotteryType,
                playType = PlayType.DAN_TUO,
                result = result,
                issueNumber = IssueCalculator.calculate(lotteryType, now),
                createdAt = now
            )
        )
        return result
    }
}
