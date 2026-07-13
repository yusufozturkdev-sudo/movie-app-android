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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BookmarkRemove
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.yusufozturk.cinetrack.data.api.NetworkConstants
import com.yusufozturk.cinetrack.data.model.GenreMapper
import com.yusufozturk.cinetrack.data.model.Movie
import com.yusufozturk.cinetrack.data.model.RatingFormatter
import com.yusufozturk.cinetrack.ui.components.ErrorStateView
import com.yusufozturk.cinetrack.ui.components.GenrePill
import com.yusufozturk.cinetrack.ui.components.ShimmerBox
import com.yusufozturk.cinetrack.ui.theme.FlicksRed
import com.yusufozturk.cinetrack.ui.theme.FlicksSurface
import com.yusufozturk.cinetrack.ui.theme.FlicksTextSecondary

@Composable
fun WatchlistScreen(
    watchlist: List<Movie>,
    isLoggedIn: Boolean,
    isLoading: Boolean,
    hasError: Boolean,
    onRetry: () -> Unit,
    onLoginClick: () -> Unit,
    onRemove: (Movie) -> Unit,
    onMovieClick: (Movie) -> Unit
) {
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
            text = if (isLoggedIn) "${watchlist.size} titles saved for later" else "Sign in to see your list",
            color = FlicksTextSecondary,
            fontSize = 14.sp,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        // 1. Giriş yapılmamış: hata değil, giriş çağrısı
        if (!isLoggedIn) {
            Column(
                modifier = Modifier.fillMaxSize().padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Sign in to your TMDB account to sync and view your watchlist.",
                    color = FlicksTextSecondary,
                    textAlign = TextAlign.Center
                )
                Button(
                    onClick = onLoginClick,
                    colors = ButtonDefaults.buttonColors(containerColor = FlicksRed),
                    modifier = Modifier.padding(top = 16.dp)
                ) {
                    Text("Sign In")
                }
            }
            return
        }

        // 2. İlk yükleme (hata yokken) skeleton
        if (isLoading && !hasError && watchlist.isEmpty()) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(5) {
                    ShimmerBox(modifier = Modifier.fillMaxWidth().height(124.dp))
                }
            }
            return
        }

        // 3. Ağ hatası: "liste boş" DEĞİL, gerçek hata ekranı
        if (hasError && watchlist.isEmpty()) {
            ErrorStateView(
                message = "Couldn't load your watchlist. Check your connection and try again.",
                onRetry = onRetry,
                isRetrying = isLoading
            )
            return
        }

        // 4. Gerçekten boş liste
        if (watchlist.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = "Your watchlist is empty.\nTap the bookmark icon on a movie's detail page to add it.",
                    color = FlicksTextSecondary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(24.dp)
                )
            }
            return
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(watchlist) { movie ->
                WatchlistCard(
                    movie = movie,
                    onClick = { onMovieClick(movie) },
                    onRemove = { onRemove(movie) }
                )
            }
        }
    }
}

@Composable
private fun WatchlistCard(movie: Movie, onClick: () -> Unit, onRemove: () -> Unit) {
    val genres = GenreMapper.namesFor(movie.genreIds)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(FlicksSurface)
            .clickable { onClick() }
            .padding(12.dp)
    ) {
        AsyncImage(
            model = NetworkConstants.posterUrl(movie.posterPath),
            contentDescription = movie.title,
            contentScale = ContentScale.Crop,
            modifier = Modifier.width(70.dp).height(100.dp).clip(RoundedCornerShape(8.dp))
        )

        Column(modifier = Modifier.padding(start = 12.dp).fillMaxWidth()) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(text = movie.title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                IconButton(onClick = onRemove) {
                    Icon(Icons.Default.BookmarkRemove, contentDescription = "Remove", tint = FlicksRed)
                }
            }
            Text(
                text = "⭐ ${RatingFormatter.format(movie.voteAverage)}   ${movie.releaseDate?.take(4) ?: ""}",
                color = FlicksTextSecondary,
                fontSize = 14.sp
            )
            if (genres.isNotEmpty()) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.padding(top = 6.dp)) {
                    genres.take(2).forEach { genre -> GenrePill(text = genre) }
                }
            }
        }
    }
}