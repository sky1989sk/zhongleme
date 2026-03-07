package com.lottery.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import com.lottery.app.data.StylePreference
import com.lottery.app.domain.model.UpdateInfo
import com.lottery.app.ui.navigation.LotteryNavHost
import com.lottery.app.ui.theme.DesignStyle
import com.lottery.app.ui.theme.LotteryAppTheme
import com.lottery.app.ui.update.UpdateDialog
import kotlinx.coroutines.launch

private fun isUpdateServerConfigured(): Boolean =
    com.lottery.app.BuildConfig.UPDATE_SERVER_BASE_URL.isNotBlank()

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val container = (application as LotteryApplication).container

        setContent {
            val designStyle by StylePreference.designStyleFlow(this@MainActivity)
                .collectAsState(initial = DesignStyle.MATERIAL)
            val scope = rememberCoroutineScope()
            var updateInfo by remember { mutableStateOf<UpdateInfo?>(null) }

            LaunchedEffect(Unit) {
                updateInfo = container.checkUpdateUseCase.check()
            }

            LotteryAppTheme(designStyle = designStyle) {
                LotteryNavHost(
                    container = container,
                    currentStyle = designStyle,
                    onStyleChange = { newStyle ->
                        scope.launch {
                            StylePreference.setDesignStyle(this@MainActivity, newStyle)
                        }
                    },
                    onCheckUpdateRequested = if (isUpdateServerConfigured()) {
                        {
                            scope.launch {
                                updateInfo = container.checkUpdateUseCase.check()
                            }
                        }
                    } else null
                )
            }

            updateInfo?.let { info ->
                UpdateDialog(
                    updateInfo = info,
                    onDismiss = { updateInfo = null },
                    onUpdate = { updateInfo = null }
                )
            }
        }
    }
}
