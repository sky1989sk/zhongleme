package com.lottery.app.infra.remote

import com.lottery.app.domain.model.LotteryType
import com.lottery.app.domain.model.PrizeResult
import com.lottery.app.domain.repository.PrizeResultRepository

/**
 * Placeholder for future remote API integration.
 * Replace with actual HTTP client (e.g. Retrofit) when ready.
 */
class StubPrizeResultRepository : PrizeResultRepository {

    override suspend fun getLatestResult(lotteryType: LotteryType): PrizeResult? {
        return null
    }

    override suspend fun getResultByIssue(lotteryType: LotteryType, issue: String): PrizeResult? {
        return null
    }
}
