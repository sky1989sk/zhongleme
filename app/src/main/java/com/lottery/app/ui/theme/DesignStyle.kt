package com.lottery.app.ui.theme

import androidx.compose.runtime.staticCompositionLocalOf

enum class DesignStyle(val displayName: String) {
    MATERIAL("Material"),
    HARMONY("鸿蒙"),
    IOS26("iOS")
}

val LocalDesignStyle = staticCompositionLocalOf { DesignStyle.MATERIAL }
