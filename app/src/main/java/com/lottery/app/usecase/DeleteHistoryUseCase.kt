package com.lottery.app.usecase

import com.lottery.app.domain.repository.HistoryRepository

class DeleteHistoryUseCase(
    private val historyRepository: HistoryRepository
) {
    suspend fun deleteRecord(id: Long) {
        historyRepository.deleteRecord(id)
    }

    suspend fun clearAll() {
        historyRepository.clearAll()
    }
}
