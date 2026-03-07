package com.lottery.app.ui.about

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lottery.app.BuildConfig
import com.lottery.app.domain.model.ChangelogEntry

@Composable
fun AboutScreen(
    viewModel: AboutViewModel,
    onCheckUpdate: (() -> Unit)? = null
) {
    val changelog by viewModel.changelog.collectAsState()
    val updateLogs by viewModel.updateLogs.collectAsState()
    val overrideUrl by viewModel.overrideUrl.collectAsState()
    val scrollState = rememberScrollState()
    var showDebugPanel by remember { mutableStateOf(false) }
    var versionTapCount by remember { mutableIntStateOf(0) }
    var lastTapTime by remember { mutableStateOf(0L) }
    var urlInput by remember { mutableStateOf("") }

    LaunchedEffect(showDebugPanel) {
        if (showDebugPanel) viewModel.loadOverrideUrl()
    }
    LaunchedEffect(overrideUrl) {
        if (showDebugPanel) urlInput = overrideUrl
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "蝌蚪云-中了么",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "版本 ${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.clickable {
                val now = System.currentTimeMillis()
                if (now - lastTapTime > 2000L) versionTapCount = 0
                versionTapCount++
                lastTapTime = now
                if (versionTapCount >= 7) {
                    showDebugPanel = true
                    viewModel.loadOverrideUrl()
                    versionTapCount = 0
                }
            }
        )
        if (onCheckUpdate != null) {
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = onCheckUpdate) {
                Text("检查更新")
            }
        }
        Spacer(modifier = Modifier.height(24.dp))

        if (showDebugPanel) {
            DebugPanel(
                urlInput = urlInput,
                onUrlChange = { urlInput = it },
                defaultUrl = BuildConfig.UPDATE_SERVER_BASE_URL,
                onSave = { viewModel.setOverrideUrl(urlInput) },
                onCheckWithLogs = { viewModel.checkUpdateWithLogs() },
                logs = updateLogs
            )
            Spacer(modifier = Modifier.height(24.dp))
        }

        Text(
            text = "更新记录",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(12.dp))

        if (changelog.isEmpty()) {
            Text(
                text = "暂无更新记录",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.fillMaxWidth()
            )
        } else {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                changelog.forEach { entry ->
                    ChangelogCard(entry = entry)
                }
            }
        }
    }
}

@Composable
private fun DebugPanel(
    urlInput: String,
    onUrlChange: (String) -> Unit,
    defaultUrl: String,
    onSave: () -> Unit,
    onCheckWithLogs: () -> Unit,
    logs: List<String>
) {
    val focusManager = LocalFocusManager.current
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "更新服务器（调试）",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = urlInput,
                onValueChange = onUrlChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("服务器地址") },
                placeholder = {
                    Text(
                        if (defaultUrl.isNotBlank()) "未填写则使用: $defaultUrl" else "未配置则使用构建默认",
                        fontSize = 12.sp
                    )
                },
                singleLine = true
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(onClick = { focusManager.clearFocus(); onSave() }) {
                    Text("保存")
                }
                Button(onClick = { focusManager.clearFocus(); onCheckWithLogs() }) {
                    Text("检查更新（带日志）")
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "连接日志",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            val logContainer = Modifier
                .fillMaxWidth()
                .heightIn(min = 80.dp, max = 200.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.surface)
                .padding(8.dp)
            if (logs.isEmpty()) {
                Box(
                    modifier = logContainer,
                    contentAlignment = Alignment.CenterStart
                ) {
                    Text(
                        text = "点击「检查更新（带日志）」查看请求过程",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontFamily = FontFamily.Monospace
                    )
                }
            } else {
                LazyColumn(modifier = logContainer) {
                    items(logs) { line ->
                        Text(
                            text = line,
                            style = MaterialTheme.typography.bodySmall,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ChangelogCard(entry: ChangelogEntry) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "v${entry.versionName}",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = entry.releaseDate,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = entry.releaseNotes,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
