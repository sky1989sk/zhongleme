package com.lottery.app.usecase

import com.lottery.app.domain.model.WonStatus
import com.lottery.app.domain.repository.HistoryRepository

class UpdateWonStatusUseCase(
    private val historyRepository: HistoryRepository
) {
    suspend operator fun invoke(recordId: Long, status: WonStatus) {
        historyRepository.updateWonStatus(recordId, status)
    }
}
