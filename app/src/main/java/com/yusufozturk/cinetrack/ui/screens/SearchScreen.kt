package com.yusufozturk.cinetrack.ui.screens

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
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.AssistChip
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.yusufozturk.cinetrack.data.api.NetworkConstants
import com.yusufozturk.cinetrack.data.model.GenreMapper
import com.yusufozturk.cinetrack.data.model.Movie
import com.yusufozturk.cinetrack.ui.components.ErrorStateView
import com.yusufozturk.cinetrack.ui.components.RatingBadge
import com.yusufozturk.cinetrack.ui.components.ShimmerBox
import com.yusufozturk.cinetrack.ui.theme.FlicksRed
import com.yusufozturk.cinetrack.ui.viewmodel.GenreHighlight
import com.yusufozturk.cinetrack.ui.viewmodel.SearchViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    onMovieClick: (Movie) -> Unit,
    onGenreClick: (Int, String) -> Unit,
    onSeeAllCategoriesClick: () -> Unit,
    viewModel: SearchViewModel = viewModel()
) {
    val query by viewModel.query.collectAsState()
    val results by viewModel.results.collectAsState()
    val isSearching by viewModel.isSearching.collectAsState()
    val isRefreshingResults by viewModel.isRefreshingResults.collectAsState()
    val genreHighlights by viewModel.genreHighlights.collectAsState()
    val trendingMovies by viewModel.trendingMovies.collectAsState()
    val isLoadingExplore by viewModel.isLoadingExplore.collectAsState()
    val searchHistory by viewModel.searchHistory.collectAsState()
    val hasExploreError by viewModel.hasExploreError.collectAsState()
    val hasSearchError by viewModel.hasSearchError.collectAsState()

    val extraGenres = listOf("Comedy", "Romance", "Documentary", "Thriller", "Animation", "Family")

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
            onValueChange = { viewModel.onQueryChanged(it) },
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            placeholder = { Text("Search for movies, TV shows...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            trailingIcon = {
                if (query.isNotEmpty()) {
                    IconButton(onClick = { viewModel.clearQuery() }) {
                        Icon(Icons.Default.Clear, contentDescription = "Clear")
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                focusedContainerColor = MaterialTheme.colorScheme.surface
            ),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search)
        )

        Spacer(modifier = Modifier.height(16.dp))

        when {
            query.isBlank() -> {
                if (hasExploreError && trendingMovies.isEmpty()) {
                    ErrorStateView(
                        onRetry = { viewModel.loadExploreContent() },
                        isRetrying = isLoadingExplore
                    )
                } else {
                    ExploreContent(
                        isLoading = isLoadingExplore,
                        genreHighlights = genreHighlights,
                        trendingMovies = trendingMovies,
                        extraGenres = extraGenres,
                        searchHistory = searchHistory,
                        onGenreClick = onGenreClick,
                        onMovieClick = onMovieClick,
                        onSeeAllCategoriesClick = onSeeAllCategoriesClick,
                        onHistoryItemClick = { term -> viewModel.searchFromHistory(term) },
                        onClearHistory = { viewModel.clearHistory() }
                    )
                }
            }

            isSearching -> SearchSkeleton()

            hasSearchError && results.isEmpty() -> {
                ErrorStateView(
                    message = "Search failed. Check your connection and try again.",
                    onRetry = { viewModel.retrySearch() },
                    isRetrying = isSearching
                )
            }

            results.isEmpty() -> {
                Text(
                    text = "No results found",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(16.dp)
                )
            }

            else -> {
                PullToRefreshBox(
                    isRefreshing = isRefreshingResults,
                    onRefresh = { viewModel.refreshResults() },
                    modifier = Modifier.fillMaxSize()
                ) {
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

                        item(span = { GridItemSpan(maxLineSpan) }) {
                            LaunchedEffect(Unit) { viewModel.loadMoreResults() }
                            Box(
                                modifier = Modifier.fillMaxWidth().height(60.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(color = FlicksRed)
                            }
                        }
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
    extraGenres: List<String>,
    searchHistory: List<String>,
    onGenreClick: (Int, String) -> Unit,
    onMovieClick: (Movie) -> Unit,
    onSeeAllCategoriesClick: () -> Unit,
    onHistoryItemClick: (String) -> Unit,
    onClearHistory: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        if (searchHistory.isNotEmpty()) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.History,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Recent Searches",
                            color = MaterialTheme.colorScheme.onBackground,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                    Text(
                        text = "Clear",
                        color = FlicksRed,
                        fontSize = 13.sp,
                        modifier = Modifier.clickable { onClearHistory() }
                    )
                }
            }
            item {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(searchHistory) { term ->
                        AssistChip(
                            onClick = { onHistoryItemClick(term) },
                            label = { Text(term) },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.History,
                                    contentDescription = null,
                                    modifier = Modifier.height(16.dp)
                                )
                            }
                        )
                    }
                }
            }
            item { Spacer(modifier = Modifier.height(8.dp)) }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Explore Categories",
                    color = MaterialTheme.colorScheme.onBackground,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "See All",
                    color = FlicksRed,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable { onSeeAllCategoriesClick() }
                )
            }
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
                    color = MaterialTheme.colorScheme.onBackground,
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
            Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surfaceVariant))
        }
        // Görsel üzerindeki koyu gradyan — metin her iki temada da beyaz kalır
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
            model = NetworkConstants.profileUrl(movie.posterPath),
            contentDescription = movie.title,
            contentScale = ContentScale.Crop,
            modifier = Modifier.width(50.dp).height(70.dp).clip(RoundedCornerShape(6.dp))
        )
        Column(modifier = Modifier.padding(start = 12.dp)) {
            Text(
                text = movie.title,
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
            )
            val subtitle = buildString {
                append(movie.releaseDate?.take(4) ?: "")
                genres.firstOrNull()?.let { append("  •  $it") }
            }
            Text(
                text = subtitle,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 13.sp
            )
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
                model = NetworkConstants.posterUrl(movie.posterPath),
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
            color = MaterialTheme.colorScheme.onBackground,
            maxLines = 1,
            modifier = Modifier.padding(top = 6.dp)
        )
        Text(
            text = movie.releaseDate?.take(4) ?: "",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 12.sp
        )
    }
}