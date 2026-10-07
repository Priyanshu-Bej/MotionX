package com.motionx.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val MotionXColors = darkColorScheme(
    primary = Color(0xFF7EDCC7),
    onPrimary = Color(0xFF00382E),
    background = Color(0xFF101419),
    onBackground = Color(0xFFE3E8EC),
    surface = Color(0xFF181E25),
    onSurface = Color(0xFFE3E8EC),
    surfaceVariant = Color(0xFF242D36),
    onSurfaceVariant = Color(0xFFA8B5BE),
)

@Composable
fun MotionXTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = MotionXColors, content = content)
}
