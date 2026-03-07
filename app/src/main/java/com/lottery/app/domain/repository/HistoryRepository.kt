package com.lottery.app.domain.repository

import com.lottery.app.domain.model.HistoryRecord
import com.lottery.app.domain.model.LotteryType
import com.lottery.app.domain.model.WonStatus
import kotlinx.coroutines.flow.Flow

interface HistoryRepository {
    fun getAllHistory(): Flow<List<HistoryRecord>>
    fun getHistoryByType(type: LotteryType): Flow<List<HistoryRecord>>
    suspend fun saveRecord(record: HistoryRecord)
    suspend fun deleteRecord(id: Long)
    suspend fun clearAll()
    suspend fun updateWonStatus(id: Long, status: WonStatus)
}
