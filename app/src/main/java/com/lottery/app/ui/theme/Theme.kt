package com.lottery.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color

private val MaterialLightColors = lightColorScheme(
    primary = Primary,
    onPrimary = Color.White,
    secondary = Secondary,
    onSecondary = Color.White,
    surface = Color.White,
    onSurface = OnSurfaceLight,
    surfaceVariant = SurfaceLight,
    background = Color(0xFFFAFAFA),
    onBackground = OnSurfaceLight
)

private val MaterialDarkColors = darkColorScheme(
    primary = Blue500,
    onPrimary = Color.White,
    secondary = Red500,
    onSecondary = Color.White,
    surface = Color(0xFF1E1E1E),
    onSurface = Color(0xFFE0E0E0),
    surfaceVariant = Color(0xFF2C2C2C),
    background = Color(0xFF121212),
    onBackground = Color(0xFFE0E0E0)
)

@Composable
fun LotteryAppTheme(
    designStyle: DesignStyle = DesignStyle.MATERIAL,
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = when (designStyle) {
        DesignStyle.MATERIAL -> if (darkTheme) MaterialDarkColors else MaterialLightColors
        DesignStyle.HARMONY -> if (darkTheme) harmonyDarkColors() else harmonyLightColors()
        DesignStyle.IOS26 -> if (darkTheme) iosDarkColors() else iosLightColors()
    }

    val typography = when (designStyle) {
        DesignStyle.MATERIAL -> AppTypography
        DesignStyle.HARMONY -> harmonyTypography()
        DesignStyle.IOS26 -> iosTypography()
    }

    val shapes = when (designStyle) {
        DesignStyle.MATERIAL -> MaterialTheme.shapes
        DesignStyle.HARMONY -> harmonyShapes()
        DesignStyle.IOS26 -> iosShapes()
    }

    CompositionLocalProvider(LocalDesignStyle provides designStyle) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = typography,
            shapes = shapes,
            content = content
        )
    }
}
