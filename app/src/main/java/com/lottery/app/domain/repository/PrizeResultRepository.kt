package com.lottery.app.domain.repository

import com.lottery.app.domain.model.LotteryType
import com.lottery.app.domain.model.PrizeResult

interface PrizeResultRepository {
    suspend fun getLatestResult(lotteryType: LotteryType): PrizeResult?
    suspend fun getResultByIssue(lotteryType: LotteryType, issue: String): PrizeResult?
}
