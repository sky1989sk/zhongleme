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
 * 支持 overrideBaseUrl 覆盖默认 baseUrl，以及 onLog 回调输出连接过程日志。
 */
class UpdateApi(
    private val baseUrl: String,
    private val gson: Gson = Gson(),
    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .build()
) {
    fun fetchVersion(overrideBaseUrl: String? = null, onLog: ((String) -> Unit)? = null): UpdateInfo? {
        val effectiveBase = overrideBaseUrl?.takeIf { it.isNotBlank() } ?: baseUrl
        if (effectiveBase.isBlank()) {
            onLog?.invoke("未配置更新服务器地址")
            return null
        }
        val url = effectiveBase.trimEnd('/') + "/api/v1/version"
        onLog?.invoke("请求: $url")
        val request = Request.Builder().url(url).get().build()
        return try {
            client.newCall(request).execute().use { response ->
                onLog?.invoke("响应: ${response.code} ${response.message}")
                if (!response.isSuccessful) {
                    onLog?.invoke("失败: HTTP ${response.code}")
                    return null
                }
                val body = try {
                    response.body?.string()
                } catch (e: Exception) {
                    onLog?.invoke("失败: 响应体读取异常 - ${e.message}")
                    return null
                }
                if (body.isNullOrBlank()) {
                    onLog?.invoke("失败: 响应体为空")
                    return null
                }
                val info = gson.fromJson(body, UpdateInfo::class.java)
                onLog?.invoke("解析成功: ${info?.versionName ?: "null"}")
                info
            }
        } catch (e: Exception) {
            onLog?.invoke("失败: ${e.message}")
            null
        }
    }

    fun fetchChangelog(overrideBaseUrl: String? = null, onLog: ((String) -> Unit)? = null): List<ChangelogEntry>? {
        val effectiveBase = overrideBaseUrl?.takeIf { it.isNotBlank() } ?: baseUrl
        if (effectiveBase.isBlank()) {
            onLog?.invoke("未配置更新服务器地址")
            return null
        }
        val url = effectiveBase.trimEnd('/') + "/api/v1/changelog"
        onLog?.invoke("请求: $url")
        val request = Request.Builder().url(url).get().build()
        return try {
            client.newCall(request).execute().use { response ->
                onLog?.invoke("响应: ${response.code} ${response.message}")
                if (!response.isSuccessful) {
                    onLog?.invoke("失败: HTTP ${response.code}")
                    return null
                }
                val body = try {
                    response.body?.string()
                } catch (e: Exception) {
                    onLog?.invoke("失败: 响应体读取异常 - ${e.message}")
                    return null
                }
                if (body.isNullOrBlank()) {
                    onLog?.invoke("失败: 响应体为空")
                    return null
                }
                val wrapper = gson.fromJson(body, ChangelogResponse::class.java)
                val list = wrapper?.versions ?: emptyList()
                onLog?.invoke("解析成功: ${list.size} 条记录")
                list
            }
        } catch (e: Exception) {
            onLog?.invoke("失败: ${e.message}")
            null
        }
    }

    private data class ChangelogResponse(
        @SerializedName("versions") val versions: List<ChangelogEntry>?
    )
}
