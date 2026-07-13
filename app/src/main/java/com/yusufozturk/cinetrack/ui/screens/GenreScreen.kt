package com.yusufozturk.cinetrack.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.yusufozturk.cinetrack.data.api.NetworkConstants
import com.yusufozturk.cinetrack.data.model.Movie
import com.yusufozturk.cinetrack.ui.components.ErrorStateView
import com.yusufozturk.cinetrack.ui.components.RatingBadge
import com.yusufozturk.cinetrack.ui.components.ShimmerBox
import com.yusufozturk.cinetrack.ui.viewmodel.GenreViewModel

@Composable
fun GenreScreen(
    genreId: Int,
    genreName: String,
    onMovieClick: (Movie) -> Unit,
    onBackClick: () -> Unit,
    viewModel: GenreViewModel = viewModel()
) {
    val movies by viewModel.movies.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val hasError by viewModel.hasError.collectAsState()

    LaunchedEffect(genreId) {
        viewModel.loadGenre(genreId)
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.statusBars)
                .padding(horizontal = 4.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackClick) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
            }
            Text(
                text = genreName,
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 4.dp)
            )
        }

        // İlk yükleme skeleton'ı. Hata sonrası retry'da skeleton gösterilmiyor;
        // onun yerine hata ekranı kalıyor ve butondaki spinner dönüyor.
        if (isLoading && !hasError) {
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
            return
        }

        // Ağ hatası: "bu kategoride film yok" DEĞİL, gerçek hata ekranı
        if (hasError && movies.isEmpty()) {
            ErrorStateView(
                onRetry = { viewModel.retry() },
                isRetrying = isLoading
            )
            return
        }

        // Gerçekten boş kategori
        if (movies.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(text = "No movies found in this category", color = Color.Gray)
            }
            return
        }

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(movies) { movie ->
                GenreMovieCard(movie = movie, onClick = { onMovieClick(movie) })
            }
        }
    }
}

@Composable
private fun GenreMovieCard(movie: Movie, onClick: () -> Unit) {
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