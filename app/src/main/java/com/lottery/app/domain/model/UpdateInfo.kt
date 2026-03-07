package com.lottery.app.domain.model

/**
 * 服务端返回的版本信息，用于检测更新与展示更新内容。
 */
data class UpdateInfo(
    val versionCode: Int,
    val versionName: String,
    val downloadUrl: String,
    val releaseNotes: String,
    val minVersionCode: Int = 1
)
