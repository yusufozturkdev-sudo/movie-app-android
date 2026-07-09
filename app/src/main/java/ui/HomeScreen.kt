package com.yusufozturk.cinetrack.ui.screens

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.yusufozturk.cinetrack.BuildConfig
import com.yusufozturk.cinetrack.data.api.RetrofitClient
import com.yusufozturk.cinetrack.data.model.GenreMapper
import com.yusufozturk.cinetrack.data.model.Movie
import com.yusufozturk.cinetrack.ui.components.GenrePill
import com.yusufozturk.cinetrack.ui.theme.FlicksRed
import com.yusufozturk.cinetrack.ui.theme.FlicksTextSecondary

@Composable
fun HomeScreen(onMovieClick: (Movie) -> Unit) {
    var movies by remember { mutableStateOf<List<Movie>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        try {
            val response = RetrofitClient.apiService.getPopularMovies(apiKey = BuildConfig.TMDB_API_KEY)
            movies = response.results
        } catch (e: Exception) {
            Log.e("HomeScreen", "Film listesi çekilemedi", e)
        } finally {
            isLoading = false
        }
    }

    if (isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = FlicksRed)
        }
        return
    }

    if (movies.isEmpty()) return

    val heroMovie = movies.first()
    val nowPlayingMovies = movies.drop(1)
    val heroGenres = GenreMapper.namesFor(heroMovie.genreIds)

    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            text = "CINETRACK",
            color = FlicksRed,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(16.dp)
        )

        Box(modifier = Modifier.fillMaxWidth().height(440.dp)) {
            AsyncImage(
                model = "https://image.tmdb.org/t/p/w780${heroMovie.backdropPath}",
                contentDescription = heroMovie.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize().clickable { onMovieClick(heroMovie) }
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Brush.verticalGradient(colors = listOf(Color.Transparent, Color.Black)))
            )
            Column(modifier = Modifier.align(Alignment.BottomStart).padding(16.dp)) {
                if (heroGenres.isNotEmpty()) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        heroGenres.take(2).forEach { genre -> GenrePill(text = genre.uppercase()) }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
                Text(text = heroMovie.title, color = Color.White, fontSize = 26.sp, fontWeight = FontWeight.Bold)
                Text(
                    text = "⭐ ${heroMovie.voteAverage}  •  ${heroMovie.releaseDate?.take(4) ?: ""}",
                    color = FlicksTextSecondary,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(top = 4.dp)
                )
                Row(modifier = Modifier.padding(top = 12.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = { onMovieClick(heroMovie) },
                        colors = ButtonDefaults.buttonColors(containerColor = FlicksRed)
                    ) {
                        Text("▶ Play")
                    }
                    OutlinedButton(onClick = { }) {
                        Text("+ My List")
                    }
                }
            }
        }

        Text(
            text = "Now Playing",
            color = Color.White,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(16.dp)
        )

        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(nowPlayingMovies) { movie ->
                AsyncImage(
                    model = "https://image.tmdb.org/t/p/w342${movie.posterPath}",
                    contentDescription = movie.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .width(110.dp)
                        .height(160.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { onMovieClick(movie) }
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}