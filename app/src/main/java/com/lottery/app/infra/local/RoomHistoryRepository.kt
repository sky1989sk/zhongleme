package com.lottery.app.infra.local

import com.lottery.app.domain.model.*
import com.lottery.app.domain.repository.HistoryRepository
import com.lottery.app.infra.local.db.HistoryDao
import com.lottery.app.infra.local.db.HistoryEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RoomHistoryRepository(
    private val dao: HistoryDao
) : HistoryRepository {

    override fun getAllHistory(): Flow<List<HistoryRecord>> {
        return dao.getAllHistory().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getHistoryByType(type: LotteryType): Flow<List<HistoryRecord>> {
        return dao.getHistoryByType(type.name).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun saveRecord(record: HistoryRecord) {
        dao.insert(record.toEntity())
    }

    override suspend fun deleteRecord(id: Long) {
        dao.deleteById(id)
    }

    override suspend fun clearAll() {
        dao.clearAll()
    }

    override suspend fun updateWonStatus(id: Long, status: WonStatus) {
        dao.updateWonStatus(id, status.code)
    }

    private fun HistoryEntity.toDomain(): HistoryRecord {
        return HistoryRecord(
            id = id,
            lotteryType = LotteryType.valueOf(lotteryType),
            playType = PlayType.valueOf(playType),
            result = ResultJsonConverter.fromJson(resultJson),
            issueNumber = issueNumber,
            wonStatus = WonStatus.fromCode(wonStatus),
            createdAt = createdAt
        )
    }

    private fun HistoryRecord.toEntity(): HistoryEntity {
        return HistoryEntity(
            id = if (id == 0L) 0 else id,
            lotteryType = lotteryType.name,
            playType = playType.name,
            resultJson = ResultJsonConverter.toJson(result),
            issueNumber = issueNumber,
            wonStatus = wonStatus.code,
            createdAt = createdAt
        )
    }
}
