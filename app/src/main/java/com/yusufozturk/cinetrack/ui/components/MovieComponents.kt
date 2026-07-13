package com.yusufozturk.cinetrack.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yusufozturk.cinetrack.data.model.RatingFormatter
import com.yusufozturk.cinetrack.ui.theme.FlicksRed
import com.yusufozturk.cinetrack.ui.theme.FlicksSurface
import com.yusufozturk.cinetrack.ui.theme.FlicksTextSecondary

@Composable
fun RatingBadge(rating: Double) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(Color.Black.copy(alpha = 0.7f))
            .padding(horizontal = 6.dp, vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.Star,
            contentDescription = null,
            tint = Color(0xFFFFC107),
            modifier = Modifier.size(12.dp)
        )
        Text(text = " ${RatingFormatter.format(rating)}", color = Color.White, fontSize = 12.sp)
    }
}

@Composable
fun GenrePill(text: String, onClick: (() -> Unit)? = null) {
    val isClickable = onClick != null

    var modifier = Modifier
        .clip(RoundedCornerShape(20.dp))
        .background(FlicksSurface)

    if (isClickable) {
        modifier = modifier
            .border(width = 1.dp, color = FlicksRed.copy(alpha = 0.6f), shape = RoundedCornerShape(20.dp))
            .clickable { onClick?.invoke() }
    }

    Row(
        modifier = modifier.padding(
            start = 12.dp,
            end = if (isClickable) 6.dp else 12.dp,
            top = 6.dp,
            bottom = 6.dp
        ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = text,
            color = if (isClickable) Color.White else Color.White.copy(alpha = 0.85f),
            fontSize = 12.sp
        )
        if (isClickable) {
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = FlicksRed,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
fun StarRatingBar(
    rating: Int,
    onRatingChanged: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(modifier = modifier) {
        for (star in 1..5) {
            Icon(
                imageVector = if (star <= rating) Icons.Filled.Star else Icons.Filled.StarBorder,
                contentDescription = "$star star",
                tint = Color(0xFFFFC107),
                modifier = Modifier
                    .size(32.dp)
                    .clickable { onRatingChanged(star) }
            )
        }
    }
}

@Composable
fun ErrorStateView(
    message: String = "Something went wrong. Check your connection and try again.",
    onRetry: () -> Unit,
    isRetrying: Boolean = false,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(RoundedCornerShape(36.dp))
                .background(FlicksSurface),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.WifiOff,
                contentDescription = null,
                tint = FlicksTextSecondary,
                modifier = Modifier.size(32.dp)
            )
        }
        Text(
            text = "Couldn't load content",
            color = Color.White,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 16.dp)
        )
        Text(
            text = message,
            color = FlicksTextSecondary,
            fontSize = 13.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 6.dp, start = 8.dp, end = 8.dp)
        )
        Button(
            onClick = onRetry,
            enabled = !isRetrying,
            colors = ButtonDefaults.buttonColors(
                containerColor = FlicksRed,
                disabledContainerColor = FlicksRed.copy(alpha = 0.6f)
            ),
            modifier = Modifier.padding(top = 20.dp)
        ) {
            if (isRetrying) {
                CircularProgressIndicator(
                    color = Color.White,
                    strokeWidth = 2.dp,
                    modifier = Modifier.size(18.dp)
                )
                Text(text = "Retrying...", modifier = Modifier.padding(start = 6.dp))
            } else {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Text(text = "Try Again", modifier = Modifier.padding(start = 6.dp))
            }
        }
    }
}