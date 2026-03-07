package com.lottery.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import com.lottery.app.data.StylePreference
import com.lottery.app.ui.navigation.LotteryNavHost
import com.lottery.app.ui.theme.DesignStyle
import com.lottery.app.ui.theme.LotteryAppTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val container = (application as LotteryApplication).container

        setContent {
            val designStyle by StylePreference.designStyleFlow(this@MainActivity)
                .collectAsState(initial = DesignStyle.MATERIAL)
            val scope = rememberCoroutineScope()

            LotteryAppTheme(designStyle = designStyle) {
                LotteryNavHost(
                    container = container,
                    currentStyle = designStyle,
                    onStyleChange = { newStyle ->
                        scope.launch {
                            StylePreference.setDesignStyle(this@MainActivity, newStyle)
                        }
                    }
                )
            }
        }
    }
}
