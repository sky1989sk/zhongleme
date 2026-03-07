package com.lottery.app.domain.model

data class HistoryRecord(
    val id: Long = 0,
    val lotteryType: LotteryType,
    val playType: PlayType,
    val result: GenerateResult,
    val issueNumber: String = "",
    val drawDate: String = "",
    val wonStatus: WonStatus = WonStatus.UNKNOWN,
    val createdAt: Long = System.currentTimeMillis()
)
