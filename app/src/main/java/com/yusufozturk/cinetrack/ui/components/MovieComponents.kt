package com.yusufozturk.cinetrack.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yusufozturk.cinetrack.data.model.RatingFormatter
import com.yusufozturk.cinetrack.ui.theme.FlicksSurface

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
    var modifier = Modifier
        .clip(RoundedCornerShape(6.dp))
        .background(FlicksSurface)

    if (onClick != null) {
        modifier = modifier.clickable { onClick() }
    }

    Box(modifier = modifier.padding(horizontal = 10.dp, vertical = 4.dp)) {
        Text(text = text, color = Color.White, fontSize = 12.sp)
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