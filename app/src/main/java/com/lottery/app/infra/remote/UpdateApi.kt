package com.lottery.app.infra.remote

import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import com.lottery.app.domain.model.ChangelogEntry
import com.lottery.app.domain.model.UpdateInfo
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit

/**
 * 从版本管理服务端拉取当前发布版本信息。
 */
class UpdateApi(
    private val baseUrl: String,
    private val gson: Gson = Gson(),
    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .build()
) {
    fun fetchVersion(): UpdateInfo? {
        if (baseUrl.isBlank()) return null
        val url = baseUrl.trimEnd('/') + "/api/v1/version"
        val request = Request.Builder().url(url).get().build()
        return try {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return null
                val body = response.body?.string() ?: return null
                gson.fromJson(body, UpdateInfo::class.java)
            }
        } catch (_: Exception) {
            null
        }
    }

    fun fetchChangelog(): List<ChangelogEntry>? {
        if (baseUrl.isBlank()) return null
        val url = baseUrl.trimEnd('/') + "/api/v1/changelog"
        val request = Request.Builder().url(url).get().build()
        return try {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return null
                val body = response.body?.string() ?: return null
                val wrapper = gson.fromJson(body, ChangelogResponse::class.java)
                wrapper?.versions ?: emptyList()
            }
        } catch (_: Exception) {
            null
        }
    }

    private data class ChangelogResponse(
        @SerializedName("versions") val versions: List<ChangelogEntry>?
    )
}
