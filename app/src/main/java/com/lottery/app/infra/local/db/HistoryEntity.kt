package com.lottery.app.infra.local.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "history_records")
data class HistoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val lotteryType: String,
    val playType: String,
    val resultJson: String,
    @ColumnInfo(defaultValue = "")
    val issueNumber: String = "",
    @ColumnInfo(defaultValue = "")
    val drawDate: String = "",
    @ColumnInfo(defaultValue = "0")
    val wonStatus: Int = 0,
    val createdAt: Long
)
