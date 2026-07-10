package com.yusufozturk.cinetrack.ui.screens

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.yusufozturk.cinetrack.BuildConfig
import com.yusufozturk.cinetrack.data.api.RetrofitClient
import com.yusufozturk.cinetrack.data.model.GenreMapper
import com.yusufozturk.cinetrack.data.model.Movie
import com.yusufozturk.cinetrack.data.model.RatingFormatter
import com.yusufozturk.cinetrack.ui.components.GenrePill
import com.yusufozturk.cinetrack.ui.components.StarRatingBar
import com.yusufozturk.cinetrack.ui.theme.FlicksRed
import com.yusufozturk.cinetrack.ui.theme.FlicksSurface
import com.yusufozturk.cinetrack.ui.theme.FlicksTextSecondary

@Composable
fun MovieDetailScreen(
    movie: Movie?,
    isInWatchlist: Boolean,
    onToggleWatchlist: () -> Unit,
    userRating: Int,
    onRateMovie: (Int) -> Unit,
    onGenreClick: (String) -> Unit,
    onBackClick: () -> Unit
) {
    if (movie == null) {
        Box(modifier = Modifier.fillMaxSize()) {
            Text(text = "Movie not found", color = Color.White, modifier = Modifier.padding(16.dp))
        }
        return
    }

    val genres = GenreMapper.namesFor(movie.genreIds)
    var runtimeText by remember { mutableStateOf<String?>(null) }
    var trailerKey by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current

    LaunchedEffect(movie.id) {
        try {
            val detail = RetrofitClient.apiService.getMovieDetails(
                movieId = movie.id,
                apiKey = BuildConfig.TMDB_API_KEY
            )
            runtimeText = detail.runtime?.let { RatingFormatter.formatRuntime(it) }
        } catch (e: Exception) {
            Log.e("MovieDetailScreen", "Failed to fetch movie details", e)
        }

        try {
            val videos = RetrofitClient.apiService.getMovieVideos(
                movieId = movie.id,
                apiKey = BuildConfig.TMDB_API_KEY
            )
            trailerKey = videos.results
                .filter { it.site == "YouTube" && it.type == "Trailer" }
                .let { trailers -> trailers.find { it.isOfficial } ?: trailers.firstOrNull() }
                ?.key
        } catch (e: Exception) {
            Log.e("MovieDetailScreen", "Failed to fetch trailer", e)
        }
    }

    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        Box(modifier = Modifier.fillMaxWidth().height(320.dp)) {
            AsyncImage(
                model = "https://image.tmdb.org/t/p/w780${movie.backdropPath}",
                contentDescription = movie.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Brush.verticalGradient(colors = listOf(Color.Transparent, Color.Black)))
            )
            IconButton(
                onClick = onBackClick,
                modifier = Modifier
                    .windowInsetsPadding(WindowInsets.statusBars)
                    .padding(8.dp),
                colors = IconButtonDefaults.iconButtonColors(
                    containerColor = Color.Black.copy(alpha = 0.4f)
                )
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White
                )
            }
        }

        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = movie.title, color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)

            val metaText = buildString {
                append(movie.releaseDate?.take(4) ?: "")
                runtimeText?.let { append("  •  $it") }
                append("  •  ⭐ ${RatingFormatter.format(movie.voteAverage)}")
            }
            Text(
                text = metaText,
                color = FlicksTextSecondary,
                fontSize = 14.sp,
                modifier = Modifier.padding(top = 4.dp)
            )

            if (genres.isNotEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(FlicksSurface)
                        .border(width = 1.dp, color = FlicksRed.copy(alpha = 0.4f), shape = RoundedCornerShape(12.dp))
                        .padding(10.dp)
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        genres.forEach { genre ->
                            GenrePill(text = genre, onClick = { onGenreClick(genre) })
                        }
                    }
                }
            }

            Row(modifier = Modifier.padding(top = 16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = {
                        val key = trailerKey
                        if (key != null) {
                            val intent = android.content.Intent(
                                android.content.Intent.ACTION_VIEW,
                                android.net.Uri.parse("https://www.youtube.com/watch?v=$key")
                            )
                            context.startActivity(intent)
                        } else {
                            Toast.makeText(context, "Trailer not available", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = FlicksRed)
                ) {
                    Text(if (trailerKey != null) "▶ Watch Trailer" else "▶ Watch Now")
                }
                OutlinedIconButton(onClick = onToggleWatchlist) {
                    Icon(
                        imageVector = if (isInWatchlist) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                        contentDescription = "Add to or remove from watchlist",
                        tint = if (isInWatchlist) FlicksRed else Color.White
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(FlicksSurface)
                    .padding(16.dp)
            ) {
                Text(
                    text = "Your Rating",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = if (userRating > 0) "You rated this movie $userRating stars" else "Rate this movie",
                    color = FlicksTextSecondary,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(top = 2.dp, bottom = 8.dp)
                )
                StarRatingBar(
                    rating = userRating,
                    onRatingChanged = { newRating -> onRateMovie(newRating) }
                )
            }

            Text(
                text = "Synopsis",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 24.dp, bottom = 8.dp)
            )
            Text(text = movie.overview, color = FlicksTextSecondary, fontSize = 14.sp)
        }
    }
}