package com.lottery.app.ui.update

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Build
import android.os.Environment
import androidx.core.content.FileProvider
import com.lottery.app.domain.model.UpdateInfo
import java.io.File

/**
 * 使用 DownloadManager 下载 APK，下载完成后发起安装。
 */
object UpdateHelper {

    private var pendingDownloadId: Long = -1
    private var pendingReceiver: BroadcastReceiver? = null

    fun startDownloadAndInstall(context: Context, updateInfo: UpdateInfo) {
        val dm = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val request = DownloadManager.Request(Uri.parse(updateInfo.downloadUrl)).apply {
            setTitle("中了么 ${updateInfo.versionName}")
            setDescription("正在下载更新")
            setMimeType("application/vnd.android.package-archive")
            setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            setDestinationInExternalFilesDir(context, Environment.DIRECTORY_DOWNLOADS, "zhongleme-update.apk")
        }
        pendingDownloadId = dm.enqueue(request)

        val receiver = object : BroadcastReceiver() {
            override fun onReceive(ctx: Context, intent: Intent) {
                val id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
                if (id != pendingDownloadId) return
                try {
                    val uri = dm.getUriForDownloadedFile(id)
                    if (uri != null) {
                        installApk(ctx, uri)
                    }
                } finally {
                    try { ctx.unregisterReceiver(this) } catch (_: Exception) { }
                    pendingReceiver = null
                }
            }
        }
        pendingReceiver = receiver
        val flag = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) Context.RECEIVER_NOT_EXPORTED else 0
        context.registerReceiver(receiver, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE), flag)
    }

    private fun installApk(context: Context, uri: Uri) {
        var installUri = uri
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && uri.scheme == "file") {
            val file = File(uri.path ?: return)
            installUri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        }
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(installUri, "application/vnd.android.package-archive")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }
}
