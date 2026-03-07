package com.lottery.app.domain.model

enum class LotteryType(
    val displayName: String,
    val frontLabel: String,
    val backLabel: String
) {
    SSQ(
        displayName = "双色球",
        frontLabel = "红球",
        backLabel = "蓝球"
    ),
    DLT(
        displayName = "超级大乐透",
        frontLabel = "前区",
        backLabel = "后区"
    )
}
