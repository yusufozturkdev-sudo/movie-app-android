package com.yusufozturk.cinetrack.ui.screens

import android.widget.Toast
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.yusufozturk.cinetrack.data.api.NetworkConstants
import com.yusufozturk.cinetrack.data.model.GenreMapper
import com.yusufozturk.cinetrack.data.model.Movie
import com.yusufozturk.cinetrack.data.model.RatingFormatter
import com.yusufozturk.cinetrack.ui.components.ErrorStateView
import com.yusufozturk.cinetrack.ui.components.GenrePill
import com.yusufozturk.cinetrack.ui.components.ShimmerBox
import com.yusufozturk.cinetrack.ui.theme.FlicksRed
import com.yusufozturk.cinetrack.ui.theme.FlicksTextSecondary
import com.yusufozturk.cinetrack.ui.viewmodel.HomeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onMovieClick: (Movie) -> Unit,
    watchlist: List<Movie>,
    onToggleWatchlist: (Movie) -> Unit,
    viewModel: HomeViewModel = viewModel()
) {
    val movies by viewModel.movies.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    val hasError by viewModel.hasError.collectAsState()
    val context = LocalContext.current

    if (isLoading) {
        HomeScreenSkeleton()
        return
    }

    if (hasError && movies.isEmpty()) {
        ErrorStateView(onRetry = { viewModel.loadFirstPage() })
        return
    }

    if (movies.isEmpty()) return

    val heroMovie = movies.first()
    val nowPlayingMovies = movies.drop(1)
    val heroGenres = GenreMapper.namesFor(heroMovie.genreIds)
    val heroInWatchlist = watchlist.any { it.id == heroMovie.id }

    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = { viewModel.refresh() },
        modifier = Modifier.fillMaxSize()
    ) {
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
                    model = NetworkConstants.backdropUrl(heroMovie.backdropPath),
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
                        text = "⭐ ${RatingFormatter.format(heroMovie.voteAverage)}  •  ${heroMovie.releaseDate?.take(4) ?: ""}",
                        color = FlicksTextSecondary,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                    Row(modifier = Modifier.padding(top = 12.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = {
                                Toast.makeText(context, "Playback feature coming soon", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = FlicksRed)
                        ) {
                            Text("▶ Play")
                        }
                        OutlinedButton(onClick = { onToggleWatchlist(heroMovie) }) {
                            Text(if (heroInWatchlist) "✓ In My List" else "+ My List")
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
                        model = NetworkConstants.posterUrl(movie.posterPath),
                        contentDescription = movie.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .width(110.dp)
                            .height(160.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { onMovieClick(movie) }
                    )
                }

                item {
                    LaunchedEffect(Unit) { viewModel.loadMore() }
                    Box(
                        modifier = Modifier.width(60.dp).height(160.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = FlicksRed, modifier = Modifier.size(24.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun HomeScreenSkeleton() {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        ShimmerBox(modifier = Modifier.fillMaxWidth().height(400.dp))
        Spacer(modifier = Modifier.height(16.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            repeat(3) {
                ShimmerBox(modifier = Modifier.width(110.dp).height(160.dp))
            }
        }
    }
}