package com.yusufozturk.cinetrack.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BookmarkRemove
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.yusufozturk.cinetrack.ui.components.GenrePill
import com.yusufozturk.cinetrack.ui.theme.FlicksRed
import com.yusufozturk.cinetrack.ui.theme.FlicksSurface
import com.yusufozturk.cinetrack.ui.theme.FlicksTextSecondary

data class WatchlistItem(
    val title: String,
    val posterPath: String,
    val rating: Double,
    val year: String,
    val genres: List<String>
)

// Not: Bu liste şimdilik örnek/statik veri. Bir sonraki oturumda Room veritabanına
// bağlayıp gerçek watchlist ekleme/çıkarma işlevini kuracağız.
private val dummyWatchlist = listOf(
    WatchlistItem("Neon Shadows", "/8Gxv8gSFCU0XGDykEGv7zR1n2ua.jpg", 8.4, "2024", listOf("Sci-Fi", "Thriller")),
    WatchlistItem("Void Horizon", "/kqjL17yufvn9OVLyXYpvtyrFfak.jpg", 9.1, "2023", listOf("Drama", "Space")),
    WatchlistItem("Whispering Pines", "/6oom5QYQ2yQTMJIbnvbkBL9cHo6.jpg", 7.8, "2024", listOf("Horror", "Mystery"))
)

@Composable
fun WatchlistScreen() {
    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            text = "CINETRACK",
            color = FlicksRed,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(16.dp)
        )
        Text(
            text = "Your Watchlist",
            color = Color.White,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        Text(
            text = "${dummyWatchlist.size} titles saved for later",
            color = FlicksTextSecondary,
            fontSize = 14.sp,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(dummyWatchlist) { item -> WatchlistCard(item) }
        }
    }
}

@Composable
private fun WatchlistCard(item: WatchlistItem) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(FlicksSurface)
            .padding(12.dp)
    ) {
        AsyncImage(
            model = "https://image.tmdb.org/t/p/w185${item.posterPath}",
            contentDescription = item.title,
            contentScale = ContentScale.Crop,
            modifier = Modifier.width(70.dp).height(100.dp).clip(RoundedCornerShape(8.dp))
        )

        Column(modifier = Modifier.padding(start = 12.dp).fillMaxWidth()) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(text = item.title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                IconButton(onClick = { }) {
                    Icon(Icons.Default.BookmarkRemove, contentDescription = "Remove", tint = FlicksRed)
                }
            }
            Text(text = "⭐ ${item.rating}   ${item.year}", color = FlicksTextSecondary, fontSize = 14.sp)
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.padding(top = 6.dp)) {
                item.genres.forEach { genre -> GenrePill(text = genre) }
            }
        }
    }
}