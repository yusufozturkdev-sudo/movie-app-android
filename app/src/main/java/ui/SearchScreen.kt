package com.yusufozturk.cinetrack.ui.screens

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.yusufozturk.cinetrack.BuildConfig
import com.yusufozturk.cinetrack.data.api.RetrofitClient
import com.yusufozturk.cinetrack.data.model.Movie
import com.yusufozturk.cinetrack.ui.components.RatingBadge
import com.yusufozturk.cinetrack.ui.theme.FlicksRed
import com.yusufozturk.cinetrack.ui.theme.FlicksSurface
import kotlinx.coroutines.delay

@Composable
fun SearchScreen(onMovieClick: (Movie) -> Unit) {
    var query by remember { mutableStateOf("") }
    var results by remember { mutableStateOf<List<Movie>>(emptyList()) }
    var isSearching by remember { mutableStateOf(false) }

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
            Log.e("SearchScreen", "Arama başarısız", e)
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
                Text(
                    text = "Film adı yazarak aramaya başla",
                    color = Color.Gray,
                    modifier = Modifier.padding(16.dp)
                )
            }
            results.isEmpty() && !isSearching -> {
                Text(
                    text = "Sonuç bulunamadı",
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