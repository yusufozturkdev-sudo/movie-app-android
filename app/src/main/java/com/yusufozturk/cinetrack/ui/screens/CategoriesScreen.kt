package com.yusufozturk.cinetrack.ui.screens

import androidx.compose.foundation.layout.size
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Cottage
import androidx.compose.material.icons.filled.EmojiEmotions
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LocalMovies
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yusufozturk.cinetrack.data.model.GenreMapper

private data class CategoryStyle(val icon: ImageVector, val colors: List<Color>)

private val categoryStyles = mapOf(
    "Action" to CategoryStyle(Icons.Default.Bolt, listOf(Color(0xFFE63946), Color(0xFF8B1E2B))),
    "Adventure" to CategoryStyle(Icons.Default.Explore, listOf(Color(0xFFE9762B), Color(0xFF8A3E10))),
    "Animation" to CategoryStyle(Icons.Default.AutoAwesome, listOf(Color(0xFF8E6FE0), Color(0xFF4A3491))),
    "Comedy" to CategoryStyle(Icons.Default.EmojiEmotions, listOf(Color(0xFFFFC145), Color(0xFFB8790A))),
    "Crime" to CategoryStyle(Icons.Default.Gavel, listOf(Color(0xFF455A64), Color(0xFF1C2B32))),
    "Documentary" to CategoryStyle(Icons.Default.MenuBook, listOf(Color(0xFF2A9D8F), Color(0xFF11534A))),
    "Drama" to CategoryStyle(Icons.Default.Psychology, listOf(Color(0xFF9C4DCC), Color(0xFF541B75))),
    "Family" to CategoryStyle(Icons.Default.Cottage, listOf(Color(0xFF457B9D), Color(0xFF1E3A54))),
    "Fantasy" to CategoryStyle(Icons.Default.AutoAwesome, listOf(Color(0xFF6A4C93), Color(0xFF33244A))),
    "History" to CategoryStyle(Icons.Default.History, listOf(Color(0xFFB08968), Color(0xFF5C4326))),
    "Horror" to CategoryStyle(Icons.Default.Warning, listOf(Color(0xFF8B0000), Color(0xFF2B0000))),
    "Music" to CategoryStyle(Icons.Default.MusicNote, listOf(Color(0xFFEF476F), Color(0xFF8C1F3F))),
    "Mystery" to CategoryStyle(Icons.Default.Search, listOf(Color(0xFF3A3A5C), Color(0xFF17172B))),
    "Romance" to CategoryStyle(Icons.Default.Favorite, listOf(Color(0xFFE84A5F), Color(0xFF8C1F30))),
    "Sci-Fi" to CategoryStyle(Icons.Default.LocalMovies, listOf(Color(0xFF06AED5), Color(0xFF0A4E5E))),
    "TV Movie" to CategoryStyle(Icons.Default.Tv, listOf(Color(0xFF5C6BC0), Color(0xFF2C356E))),
    "Thriller" to CategoryStyle(Icons.Default.Visibility, listOf(Color(0xFF212121), Color(0xFF000000))),
    "War" to CategoryStyle(Icons.Default.Groups, listOf(Color(0xFF6D6875), Color(0xFF352F38))),
    "Western" to CategoryStyle(Icons.Default.LocalMovies, listOf(Color(0xFFB5651D), Color(0xFF5C300C)))
)

private val defaultStyle = CategoryStyle(Icons.Default.LocalMovies, listOf(Color(0xFF6C757D), Color(0xFF343A40)))

@Composable
fun CategoriesScreen(onGenreClick: (Int, String) -> Unit, onBackClick: () -> Unit) {
    val genres = GenreMapper.allGenres()

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.statusBars)
                .padding(horizontal = 4.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackClick) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.onBackground)
            }
            Column(modifier = Modifier.padding(start = 4.dp)) {
                Text(
                    text = "All Categories",
                    color = MaterialTheme.colorScheme.onBackground,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${genres.size} genres to explore",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp
                )
            }
        }

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(genres) { (id, name) ->
                val style = categoryStyles[name] ?: defaultStyle
                CategoryTile(name = name, style = style, onClick = { onGenreClick(id, name) })
            }
        }
    }
}

@Composable
private fun CategoryTile(name: String, style: CategoryStyle, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1.5f)
            .clip(RoundedCornerShape(18.dp))
            .background(
                Brush.linearGradient(
                    colors = style.colors,
                    start = androidx.compose.ui.geometry.Offset(0f, 0f),
                    end = androidx.compose.ui.geometry.Offset(300f, 300f)
                )
            )
            .clickable { onClick() }
    ) {
        // Arka planda büyük, yarı saydam dekoratif ikon
        Icon(
            imageVector = style.icon,
            contentDescription = null,
            tint = Color.White.copy(alpha = 0.15f),
            modifier = Modifier
                .size(72.dp)
                .align(Alignment.TopEnd)
                .offset(x = 14.dp, y = (-10).dp)
        )

        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(14.dp)
        ) {
            Icon(
                imageVector = style.icon,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(22.dp)
            )
            Text(
                text = name,
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 6.dp)
            )
        }
    }
}