package com.yusufozturk.cinetrack.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColors = darkColorScheme(
    primary = FlicksRed,
    onPrimary = Color.White,
    background = FlicksBackground,
    onBackground = FlicksTextPrimary,
    surface = FlicksSurface,
    onSurface = FlicksTextPrimary,
    // İkincil metinler (tarih, tür, açıklama) için
    onSurfaceVariant = FlicksTextSecondary,
    surfaceVariant = FlicksSurface,
)

private val LightColors = lightColorScheme(
    primary = FlicksRed,
    onPrimary = Color.White,
    background = FlicksBackgroundLight,
    onBackground = FlicksTextPrimaryLight,
    surface = FlicksSurfaceLight,
    onSurface = FlicksTextPrimaryLight,
    onSurfaceVariant = FlicksTextSecondaryLight,
    surfaceVariant = FlicksSurfaceLight,
)

@Composable
fun CineTrackTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        content = content
    )
}