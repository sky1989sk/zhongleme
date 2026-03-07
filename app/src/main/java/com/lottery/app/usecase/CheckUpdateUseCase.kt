package com.lottery.app.usecase

import com.lottery.app.domain.model.UpdateInfo
import com.lottery.app.infra.remote.UpdateApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * 检测是否有新版本。若服务端 versionCode 大于当前 versionCode 则返回服务端信息，否则返回 null。
 * 支持 getEffectiveBaseUrl 提供当前有效 baseUrl，以及 onLog 输出连接过程日志。
 */
class CheckUpdateUseCase(
    private val updateApi: UpdateApi,
    private val currentVersionCode: Int,
    private val getEffectiveBaseUrl: suspend () -> String
) {
    /**
     * @param onLog 可选，用于输出请求与结果日志（调试面板使用）
     * @return 若有新版本返回 [UpdateInfo]，否则 null
     */
    suspend fun check(onLog: ((String) -> Unit)? = null): UpdateInfo? = withContext(Dispatchers.IO) {
        val baseUrl = getEffectiveBaseUrl()
        if (baseUrl.isBlank()) {
            onLog?.invoke("未配置更新服务器地址")
            return@withContext null
        }
        val info = updateApi.fetchVersion(baseUrl, onLog) ?: run {
            onLog?.invoke("结果: 获取版本信息失败")
            return@withContext null
        }
        if (info.versionCode <= currentVersionCode) {
            onLog?.invoke("结果: 已是最新版本")
            return@withContext null
        }
        if (info.downloadUrl.isBlank()) {
            onLog?.invoke("结果: 服务端未提供下载地址")
            return@withContext null
        }
        onLog?.invoke("结果: 发现新版本 ${info.versionName}")
        info
    }
}
