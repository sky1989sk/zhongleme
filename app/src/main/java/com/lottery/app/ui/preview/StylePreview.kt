package com.lottery.app.ui.preview

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.lottery.app.ui.components.*
import com.lottery.app.ui.theme.DesignStyle
import com.lottery.app.ui.theme.LotteryAppTheme

/**
 * Reusable panel that renders all styled components in a single scrollable column.
 * Wrap it in [LotteryAppTheme] with the desired [DesignStyle] to visually verify
 * that every component renders correctly under that style.
 */
@Composable
private fun ComponentShowcase(styleName: String) {
    Surface(
        color = MaterialTheme.colorScheme.background,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "风格：$styleName",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )

            SectionTitle("SegmentedControl (2 items)")
            var seg2 by remember { mutableStateOf("双色球") }
            StyledSegmentedControl(
                items = listOf("双色球", "大乐透"),
                selectedItem = seg2,
                onItemSelected = { seg2 = it },
                label = { it }
            )

            SectionTitle("SegmentedControl (3 items)")
            var seg3 by remember { mutableStateOf("全部") }
            StyledSegmentedControl(
                items = listOf("全部", "双色球", "大乐透"),
                selectedItem = seg3,
                onItemSelected = { seg3 = it },
                label = { it }
            )

            HorizontalDivider()

            SectionTitle("NumberBall")
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                NumberBall(number = 7, ballType = BallType.RED)
                NumberBall(number = 16, ballType = BallType.BLUE)
                NumberBall(number = 3, ballType = BallType.DAN)
            }

            HorizontalDivider()

            SectionTitle("StyledCard")
            StyledCard(modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp)) {
                    Text("双色球 · 普通", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        listOf(3, 11, 18, 22, 27, 33).forEach {
                            NumberBall(number = it, ballType = BallType.RED, size = 36.dp)
                        }
                        NumberBall(number = 8, ballType = BallType.BLUE, size = 36.dp)
                    }
                }
            }

            HorizontalDivider()

            SectionTitle("StyledButton")
            StyledButton(onClick = {}) {
                Text("生成号码")
            }
            StyledButton(onClick = {}, enabled = false) {
                Text("禁用状态")
            }
        }
    }
}

@Composable
private fun SectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.primary
    )
}

// ─── One Preview per Style ───────────────────────────────────

@Preview(showBackground = true, name = "All Components - Material", widthDp = 380)
@Composable
private fun ShowcaseMaterial() {
    LotteryAppTheme(designStyle = DesignStyle.MATERIAL) { ComponentShowcase("Material") }
}

@Preview(showBackground = true, name = "All Components - Harmony", widthDp = 380)
@Composable
private fun ShowcaseHarmony() {
    LotteryAppTheme(designStyle = DesignStyle.HARMONY) { ComponentShowcase("鸿蒙") }
}

@Preview(showBackground = true, name = "All Components - iOS", widthDp = 380)
@Composable
private fun ShowcaseIos() {
    LotteryAppTheme(designStyle = DesignStyle.IOS26) { ComponentShowcase("iOS") }
}
