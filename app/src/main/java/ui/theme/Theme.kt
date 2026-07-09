package com.yusufozturk.cinetrack.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val FlicksColorScheme = darkColorScheme(
    primary = FlicksRed,
    background = FlicksBackground,
    surface = FlicksSurface,
    onPrimary = Color.White,
    onBackground = FlicksTextPrimary,
    onSurface = FlicksTextPrimary,
)

@Composable
fun CineTrackTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = FlicksColorScheme,
        content = content
    )
}