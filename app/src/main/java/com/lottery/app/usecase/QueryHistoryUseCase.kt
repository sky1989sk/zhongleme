package com.lottery.app.usecase

import com.lottery.app.domain.model.HistoryRecord
import com.lottery.app.domain.model.LotteryType
import com.lottery.app.domain.repository.HistoryRepository
import kotlinx.coroutines.flow.Flow

class QueryHistoryUseCase(
    private val historyRepository: HistoryRepository
) {
    fun getAllHistory(): Flow<List<HistoryRecord>> {
        return historyRepository.getAllHistory()
    }

    fun getHistoryByType(type: LotteryType): Flow<List<HistoryRecord>> {
        return historyRepository.getHistoryByType(type)
    }
}
