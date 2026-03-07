package com.lottery.app.domain.model

sealed class GenerateResult {
    data class StandardResult(
        val numbers: List<LotteryNumber>
    ) : GenerateResult()

    data class MultipleResult(
        val numbers: LotteryNumber
    ) : GenerateResult()

    data class DanTuoResult(
        val danTuo: DanTuoNumber
    ) : GenerateResult()
}
