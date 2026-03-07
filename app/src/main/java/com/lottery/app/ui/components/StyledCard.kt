package com.lottery.app.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.lottery.app.ui.theme.DesignStyle
import com.lottery.app.ui.theme.LocalDesignStyle
import com.lottery.app.ui.theme.LotteryAppTheme

@Composable
fun StyledCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    val style = LocalDesignStyle.current
    when (style) {
        DesignStyle.MATERIAL -> {
            ElevatedCard(
                modifier = modifier,
                elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp),
                content = content
            )
        }
        DesignStyle.HARMONY -> {
            Card(
                modifier = modifier,
                shape = MaterialTheme.shapes.medium,
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp),
                content = content
            )
        }
        DesignStyle.IOS26 -> {
            OutlinedCard(
                modifier = modifier,
                shape = MaterialTheme.shapes.large,
                colors = CardDefaults.outlinedCardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
                content = content
            )
        }
    }
}

// region Previews

@Composable
private fun CardPreviewContent() {
    StyledCard(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
        Text("示例卡片内容", modifier = Modifier.padding(16.dp))
    }
}

@Preview(showBackground = true, name = "Card - Material")
@Composable
private fun PreviewCardMaterial() {
    LotteryAppTheme(designStyle = DesignStyle.MATERIAL) { CardPreviewContent() }
}

@Preview(showBackground = true, name = "Card - Harmony")
@Composable
private fun PreviewCardHarmony() {
    LotteryAppTheme(designStyle = DesignStyle.HARMONY) { CardPreviewContent() }
}

@Preview(showBackground = true, name = "Card - iOS")
@Composable
private fun PreviewCardIos() {
    LotteryAppTheme(designStyle = DesignStyle.IOS26) { CardPreviewContent() }
}

// endregion
