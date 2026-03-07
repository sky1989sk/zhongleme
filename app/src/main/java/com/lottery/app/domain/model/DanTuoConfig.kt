package com.lottery.app.domain.model

data class DanTuoConfig(
    val frontDanCount: Int,
    val frontTuoCount: Int,
    val backDanCount: Int = 0,
    val backTuoCount: Int = 0
)
