package com.lottery.app.ui.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.lottery.app.ui.theme.DesignStyle
import com.lottery.app.ui.theme.LocalDesignStyle
import com.lottery.app.ui.theme.LotteryAppTheme

@Composable
fun StyledButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: @Composable RowScope.() -> Unit
) {
    val style = LocalDesignStyle.current
    when (style) {
        DesignStyle.MATERIAL -> {
            Button(
                onClick = onClick,
                modifier = modifier
                    .fillMaxWidth()
                    .height(52.dp),
                enabled = enabled,
                shape = RoundedCornerShape(28.dp),
                content = content
            )
        }
        DesignStyle.HARMONY -> {
            Button(
                onClick = onClick,
                modifier = modifier
                    .fillMaxWidth()
                    .height(48.dp),
                enabled = enabled,
                shape = RoundedCornerShape(40.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                contentPadding = PaddingValues(horizontal = 24.dp, vertical = 0.dp),
                content = content
            )
        }
        DesignStyle.IOS26 -> {
            Button(
                onClick = onClick,
                modifier = modifier
                    .fillMaxWidth()
                    .height(50.dp),
                enabled = enabled,
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                content = content
            )
        }
    }
}

// region Previews

@Composable
private fun ButtonPreviewContent() {
    StyledButton(
        onClick = {},
        modifier = Modifier.padding(16.dp)
    ) {
        Text("生成号码")
    }
}

@Preview(showBackground = true, name = "Button - Material")
@Composable
private fun PreviewButtonMaterial() {
    LotteryAppTheme(designStyle = DesignStyle.MATERIAL) { ButtonPreviewContent() }
}

@Preview(showBackground = true, name = "Button - Harmony")
@Composable
private fun PreviewButtonHarmony() {
    LotteryAppTheme(designStyle = DesignStyle.HARMONY) { ButtonPreviewContent() }
}

@Preview(showBackground = true, name = "Button - iOS")
@Composable
private fun PreviewButtonIos() {
    LotteryAppTheme(designStyle = DesignStyle.IOS26) { ButtonPreviewContent() }
}

// endregion
