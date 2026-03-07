package com.lottery.app.domain.model

/**
 * 更新记录中的单条版本信息。
 */
data class ChangelogEntry(
    val versionCode: Int,
    val versionName: String,
    val releaseDate: String,
    val releaseNotes: String
)
