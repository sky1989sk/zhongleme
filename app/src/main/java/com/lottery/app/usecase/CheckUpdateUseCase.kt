package com.lottery.app.usecase

import com.lottery.app.domain.model.UpdateInfo
import com.lottery.app.infra.remote.UpdateApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * 检测是否有新版本。若服务端 versionCode 大于当前 versionCode 则返回服务端信息，否则返回 null。
 */
class CheckUpdateUseCase(
    private val updateApi: UpdateApi,
    private val currentVersionCode: Int
) {
    /**
     * @return 若有新版本返回 [UpdateInfo]，否则 null
     */
    suspend fun check(): UpdateInfo? = withContext(Dispatchers.IO) {
        val info = updateApi.fetchVersion() ?: return@withContext null
        if (info.versionCode <= currentVersionCode) return@withContext null
        if (info.downloadUrl.isBlank()) return@withContext null
        info
    }
}
