package com.lottery.app.domain.model

enum class GeneratorStrategy(val displayName: String) {
    PURE_RANDOM("纯随机"),
    TAIL_PRIORITY("尾号优先"),
    HOT_COLD_BALANCE("冷热号平衡"),
    ODD_EVEN_BALANCE("奇偶均衡"),
    BIG_SMALL_BALANCE("大小均衡")
}
