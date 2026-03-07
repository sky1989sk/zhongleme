package com.lottery.app.infra.local.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface HistoryDao {

    @Query("SELECT * FROM history_records ORDER BY createdAt DESC")
    fun getAllHistory(): Flow<List<HistoryEntity>>

    @Query("SELECT * FROM history_records WHERE lotteryType = :type ORDER BY createdAt DESC")
    fun getHistoryByType(type: String): Flow<List<HistoryEntity>>

    @Insert
    suspend fun insert(entity: HistoryEntity)

    @Query("DELETE FROM history_records WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM history_records")
    suspend fun clearAll()

    @Query("UPDATE history_records SET wonStatus = :status WHERE id = :id")
    suspend fun updateWonStatus(id: Long, status: Int)
}
