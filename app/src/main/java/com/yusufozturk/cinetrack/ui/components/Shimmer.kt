package com.yusufozturk.cinetrack.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.unit.dp

@Composable
fun ShimmerBox(modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val progress by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 700, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "shimmerProgress"
    )

    // Rengi saydamlaştırıp arka planla karıştırmak yerine (açık temada görünmez hale geliyordu),
    // iki TAM OPAK renk arasında geçiş yapıyoruz: surfaceVariant ile onSurfaceVariant karışımı.
    // Bu, açık ve koyu temanın ikisinde de garanti kontrast sağlıyor.
    val baseColor = MaterialTheme.colorScheme.surfaceVariant
    val highlightColor = MaterialTheme.colorScheme.onSurfaceVariant
        .copy(alpha = 0.3f)
        .compositeOver(baseColor)
    val color = lerp(baseColor, highlightColor, progress)

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(color)
    )
}