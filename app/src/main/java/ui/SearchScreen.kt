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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.yusufozturk.cinetrack.BuildConfig
import com.yusufozturk.cinetrack.data.api.RetrofitClient
import com.yusufozturk.cinetrack.data.model.GenreMapper
import com.yusufozturk.cinetrack.data.model.Movie
import com.yusufozturk.cinetrack.ui.components.RatingBadge
import com.yusufozturk.cinetrack.ui.components.ShimmerBox
import com.yusufozturk.cinetrack.ui.theme.FlicksRed
import com.yusufozturk.cinetrack.ui.theme.FlicksSurface
import com.yusufozturk.cinetrack.ui.theme.FlicksTextSecondary
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay

private data class GenreHighlight(val id: Int, val name: String, val imageUrl: String?)

private val featuredGenres = listOf(28 to "Action", 18 to "Drama", 878 to "Sci-Fi", 27 to "Horror")
private val extraGenres = listOf("Comedy", "Romance", "Documentary", "Thriller", "Animation", "Family")

@Composable
fun SearchScreen(onMovieClick: (Movie) -> Unit, onGenreClick: (Int, String) -> Unit) {
    var query by remember { mutableStateOf("") }
    var results by remember { mutableStateOf<List<Movie>>(emptyList()) }
    var isSearching by remember { mutableStateOf(false) }

    var genreHighlights by remember { mutableStateOf<List<GenreHighlight>>(emptyList()) }
    var trendingMovies by remember { mutableStateOf<List<Movie>>(emptyList()) }
    var isLoadingExplore by remember { mutableStateOf(true) }

    // Sadece ilk açılışta bir kere: kategori kapak resimleri + trend olan filmler
    LaunchedEffect(Unit) {
        try {
            coroutineScope {
                val highlightsDeferred = featuredGenres.map { (id, name) ->
                    async {
                        val image = try {
                            RetrofitClient.apiService.discoverMoviesByGenre(
                                apiKey = BuildConfig.TMDB_API_KEY,
                                genreId = id
                            ).results.firstOrNull()?.backdropPath
                        } catch (e: Exception) {
                            null
                        }
                        GenreHighlight(id, name, image?.let { "https://image.tmdb.org/t/p/w500$it" })
                    }
                }
                val trendingDeferred = async {
                    try {
                        RetrofitClient.apiService.getTrendingMovies(BuildConfig.TMDB_API_KEY).results
                    } catch (e: Exception) {
                        emptyList()
                    }
                }
                genreHighlights = highlightsDeferred.awaitAll()
                trendingMovies = trendingDeferred.await()
            }
        } catch (e: Exception) {
            Log.e("SearchScreen", "Failed to load explore content", e)
        } finally {
            isLoadingExplore = false
        }
    }

    LaunchedEffect(query) {
        if (query.isBlank()) {
            results = emptyList()
            return@LaunchedEffect
        }
        delay(400)
        isSearching = true
        try {
            val response = RetrofitClient.apiService.searchMovies(
                apiKey = BuildConfig.TMDB_API_KEY,
                query = query
            )
            results = response.results
        } catch (e: Exception) {
            Log.e("SearchScreen", "Search failed", e)
        } finally {
            isSearching = false
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            text = "CINETRACK",
            color = FlicksRed,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(16.dp)
        )

        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            placeholder = { Text("Search for movies, TV shows...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            trailingIcon = {
                if (query.isNotEmpty()) {
                    IconButton(onClick = { query = "" }) {
                        Icon(Icons.Default.Clear, contentDescription = "Clear")
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedContainerColor = FlicksSurface,
                focusedContainerColor = FlicksSurface
            ),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search)
        )

        Spacer(modifier = Modifier.height(16.dp))

        when {
            query.isBlank() -> {
                ExploreContent(
                    isLoading = isLoadingExplore,
                    genreHighlights = genreHighlights,
                    trendingMovies = trendingMovies,
                    onGenreClick = onGenreClick,
                    onMovieClick = onMovieClick
                )
            }
            isSearching -> SearchSkeleton()
            results.isEmpty() -> {
                Text(
                    text = "No results found",
                    color = Color.Gray,
                    modifier = Modifier.padding(16.dp)
                )
            }
            else -> {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(results) { movie ->
                        SearchResultCard(movie = movie, onClick = { onMovieClick(movie) })
                    }
                }
            }
        }
    }
}

@Composable
private fun ExploreContent(
    isLoading: Boolean,
    genreHighlights: List<GenreHighlight>,
    trendingMovies: List<Movie>,
    onGenreClick: (Int, String) -> Unit,
    onMovieClick: (Movie) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        item {
            Text(
                text = "Explore Categories",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }

        item {
            if (isLoading) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    repeat(2) { ShimmerBox(modifier = Modifier.weight(1f).height(90.dp)) }
                }
            } else {
                genreHighlights.chunked(2).forEach { rowGenres ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        rowGenres.forEach { genre ->
                            GenreHighlightCard(
                                genre = genre,
                                modifier = Modifier.weight(1f),
                                onClick = { onGenreClick(genre.id, genre.name) }
                            )
                        }
                        if (rowGenres.size == 1) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }

        item {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(extraGenres) { genreName ->
                    val genreId = GenreMapper.idFor(genreName)
                    AssistChip(
                        onClick = { if (genreId != null) onGenreClick(genreId, genreName) },
                        label = { Text(genreName) }
                    )
                }
            }
        }

        item {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.TrendingUp,
                    contentDescription = null,
                    tint = FlicksRed
                )
                Text(
                    text = "Trending Searches",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 6.dp)
                )
            }
        }

        if (isLoading) {
            items(4) {
                ShimmerBox(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                        .height(70.dp)
                )
            }
        } else {
            items(trendingMovies.take(10)) { movie ->
                TrendingMovieRow(movie = movie, onClick = { onMovieClick(movie) })
            }
        }
    }
}

@Composable
private fun GenreHighlightCard(genre: GenreHighlight, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Box(
        modifier = modifier
            .height(90.dp)
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
    ) {
        if (genre.imageUrl != null) {
            AsyncImage(
                model = genre.imageUrl,
                contentDescription = genre.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Box(modifier = Modifier.fillMaxSize().background(FlicksSurface))
        }
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.8f))))
        )
        Text(
            text = genre.name,
            color = Color.White,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.align(Alignment.BottomStart).padding(10.dp)
        )
    }
}

@Composable
private fun TrendingMovieRow(movie: Movie, onClick: () -> Unit) {
    val genres = GenreMapper.namesFor(movie.genreIds)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = "https://image.tmdb.org/t/p/w185${movie.posterPath}",
            contentDescription = movie.title,
            contentScale = ContentScale.Crop,
            modifier = Modifier.width(50.dp).height(70.dp).clip(RoundedCornerShape(6.dp))
        )
        Column(modifier = Modifier.padding(start = 12.dp)) {
            Text(text = movie.title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
            val subtitle = buildString {
                append(movie.releaseDate?.take(4) ?: "")
                genres.firstOrNull()?.let { append("  •  $it") }
            }
            Text(text = subtitle, color = FlicksTextSecondary, fontSize = 13.sp)
        }
    }
}

@Composable
private fun SearchSkeleton() {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        items(6) {
            ShimmerBox(modifier = Modifier.fillMaxWidth().height(220.dp))
        }
    }
}

@Composable
private fun SearchResultCard(movie: Movie, onClick: () -> Unit) {
    Column(modifier = Modifier.clickable { onClick() }) {
        Box {
            AsyncImage(
                model = "https://image.tmdb.org/t/p/w342${movie.posterPath}",
                contentDescription = movie.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxWidth().height(220.dp).clip(RoundedCornerShape(8.dp))
            )
            Box(modifier = Modifier.align(Alignment.TopEnd).padding(6.dp)) {
                RatingBadge(rating = movie.voteAverage)
            }
        }
        Text(
            text = movie.title,
            color = Color.White,
            maxLines = 1,
            modifier = Modifier.padding(top = 6.dp)
        )
        Text(
            text = movie.releaseDate?.take(4) ?: "",
            color = Color.Gray,
            fontSize = 12.sp
        )
    }
}