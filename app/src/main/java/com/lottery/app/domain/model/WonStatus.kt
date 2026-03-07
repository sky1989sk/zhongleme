package com.lottery.app.domain.model

enum class WonStatus(val code: Int, val displayName: String) {
    UNKNOWN(0, "未知"),
    WON(1, "已中奖"),
    NOT_WON(2, "未中奖");

    companion object {
        fun fromCode(code: Int): WonStatus = entries.firstOrNull { it.code == code } ?: UNKNOWN
    }
}
