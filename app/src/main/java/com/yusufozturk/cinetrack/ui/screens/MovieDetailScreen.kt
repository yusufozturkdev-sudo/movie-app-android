package com.yusufozturk.cinetrack.ui.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.yusufozturk.cinetrack.data.api.NetworkConstants
import com.yusufozturk.cinetrack.data.model.CastMember
import com.yusufozturk.cinetrack.data.model.GenreMapper
import com.yusufozturk.cinetrack.data.model.Movie
import com.yusufozturk.cinetrack.data.model.PersonDetail
import com.yusufozturk.cinetrack.data.model.RatingFormatter
import com.yusufozturk.cinetrack.data.repository.MovieRepository
import com.yusufozturk.cinetrack.ui.components.GenrePill
import com.yusufozturk.cinetrack.ui.components.ShimmerBox
import com.yusufozturk.cinetrack.ui.components.StarRatingBar
import com.yusufozturk.cinetrack.ui.theme.FlicksRed
import com.yusufozturk.cinetrack.ui.viewmodel.MovieDetailViewModel
import kotlinx.coroutines.launch

@Composable
fun MovieDetailScreen(
    movie: Movie?,
    isInWatchlist: Boolean,
    onToggleWatchlist: () -> Unit,
    isWatched: Boolean,
    onToggleWatched: () -> Unit,
    userRating: Int,
    onRateMovie: (Int) -> Unit,
    onGenreClick: (String) -> Unit,
    onMovieClick: (Movie) -> Unit,
    onBackClick: () -> Unit,
    viewModel: MovieDetailViewModel = viewModel()
) {
    if (movie == null) {
        Box(modifier = Modifier.fillMaxSize()) {
            Text(
                text = "Movie not found",
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(16.dp)
            )
        }
        return
    }

    val isLoadingDetails by viewModel.isLoadingDetails.collectAsState()
    val runtimeText by viewModel.runtimeText.collectAsState()
    val trailerKey by viewModel.trailerKey.collectAsState()
    val cast by viewModel.cast.collectAsState()
    val similarMovies by viewModel.similarMovies.collectAsState()
    val genres = GenreMapper.namesFor(movie.genreIds)
    val context = LocalContext.current

    var selectedCastMember by remember { mutableStateOf<CastMember?>(null) }

    LaunchedEffect(movie.id) {
        viewModel.loadDetails(movie.id)
    }

    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        // Backdrop bölümü — üzerindeki her şey koyu gradyanda, temadan bağımsız beyaz
        Box(modifier = Modifier.fillMaxWidth().height(320.dp)) {
            AsyncImage(
                model = NetworkConstants.backdropUrl(movie.backdropPath),
                contentDescription = movie.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Brush.verticalGradient(colors = listOf(Color.Transparent, Color.Black)))
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .windowInsetsPadding(WindowInsets.statusBars)
                    .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(
                    onClick = onBackClick,
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = Color.Black.copy(alpha = 0.4f)
                    )
                ) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                }
                IconButton(
                    onClick = {
                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(
                                Intent.EXTRA_TEXT,
                                "Check out \"${movie.title}\" on CineTrack! ⭐ ${RatingFormatter.format(movie.voteAverage)}"
                            )
                        }
                        context.startActivity(Intent.createChooser(shareIntent, "Share via"))
                    },
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = Color.Black.copy(alpha = 0.4f)
                    )
                ) {
                    Icon(Icons.Default.Share, contentDescription = "Share", tint = Color.White)
                }
            }
        }

        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = movie.title,
                color = MaterialTheme.colorScheme.onBackground,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )

            if (isLoadingDetails) {
                ShimmerBox(modifier = Modifier.padding(top = 6.dp).width(180.dp).height(16.dp))
            } else {
                val metaText = buildString {
                    append(movie.releaseDate?.take(4) ?: "")
                    runtimeText?.let { append("  •  $it") }
                    append("  •  ⭐ ${RatingFormatter.format(movie.voteAverage)}")
                }
                Text(
                    text = metaText,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            if (genres.isNotEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surface)
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

            if (isLoadingDetails) {
                Row(modifier = Modifier.padding(top = 16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ShimmerBox(modifier = Modifier.width(150.dp).height(40.dp))
                    ShimmerBox(modifier = Modifier.width(48.dp).height(40.dp))
                    ShimmerBox(modifier = Modifier.width(48.dp).height(40.dp))
                }
            } else {
                Row(modifier = Modifier.padding(top = 16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = {
                            val key = trailerKey
                            if (key != null) {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(NetworkConstants.youtubeUrl(key)))
                                context.startActivity(intent)
                            } else {
                                Toast.makeText(context, "Trailer not available", Toast.LENGTH_SHORT).show()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = FlicksRed,
                            contentColor = Color.White
                        )
                    ) {
                        Text(if (trailerKey != null) "▶ Watch Trailer" else "▶ Watch Now")
                    }
                    OutlinedIconButton(onClick = onToggleWatchlist) {
                        Icon(
                            imageVector = if (isInWatchlist) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                            contentDescription = "Add to or remove from watchlist",
                            tint = if (isInWatchlist) FlicksRed else MaterialTheme.colorScheme.onBackground
                        )
                    }
                    OutlinedIconButton(onClick = onToggleWatched) {
                        Icon(
                            imageVector = if (isWatched) Icons.Filled.CheckCircle else Icons.Outlined.CheckCircle,
                            contentDescription = "Mark as watched",
                            tint = if (isWatched) Color(0xFF4CAF50) else MaterialTheme.colorScheme.onBackground
                        )
                    }
                }
                if (isWatched) {
                    Text(
                        text = "✓ You've watched this movie",
                        color = Color(0xFF4CAF50),
                        fontSize = 12.sp,
                        modifier = Modifier.padding(top = 6.dp)
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(16.dp)
            ) {
                Text(
                    text = "Your Rating",
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = if (userRating > 0) "You rated this movie $userRating stars" else "Rate this movie",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
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
                color = MaterialTheme.colorScheme.onBackground,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 24.dp, bottom = 8.dp)
            )
            Text(
                text = movie.overview,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 14.sp
            )

            Text(
                text = "Top Cast",
                color = MaterialTheme.colorScheme.onBackground,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 24.dp, bottom = 12.dp)
            )
        }

        if (isLoadingDetails) {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(5) { CastMemberSkeleton() }
            }
        } else if (cast.isNotEmpty()) {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(cast) { member ->
                    CastMemberCard(member, onClick = { selectedCastMember = member })
                }
            }
        }

        Text(
            text = "Similar Movies",
            color = MaterialTheme.colorScheme.onBackground,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp).padding(top = 12.dp)
        )

        if (isLoadingDetails) {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(5) { ShimmerBox(modifier = Modifier.width(110.dp).height(160.dp)) }
            }
        } else if (similarMovies.isNotEmpty()) {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(similarMovies) { similarMovie ->
                    SimilarMovieCard(similarMovie, onClick = { onMovieClick(similarMovie) })
                }
            }
        }

        Box(modifier = Modifier.height(24.dp))
    }

    selectedCastMember?.let { member ->
        CastMemberDialog(member = member, onDismiss = { selectedCastMember = null })
    }
}

@Composable
private fun CastMemberDialog(member: CastMember, onDismiss: () -> Unit) {
    val movieRepository = remember { MovieRepository() }
    var personDetail by remember { mutableStateOf<PersonDetail?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(member.id) {
        coroutineScope.launch {
            try {
                personDetail = movieRepository.getPersonDetails(member.id)
            } catch (e: Exception) {
                // Hata yönetimi ileride eklenebilir
            } finally {
                isLoading = false
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close", color = FlicksRed)
            }
        },
        containerColor = MaterialTheme.colorScheme.surface,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (member.profilePath != null) {
                    AsyncImage(
                        model = NetworkConstants.profileUrl(member.profilePath),
                        contentDescription = member.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.width(48.dp).height(48.dp).clip(CircleShape)
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .width(48.dp)
                            .height(48.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Person,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Column(modifier = Modifier.padding(start = 12.dp)) {
                    Text(
                        text = member.name,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Text(
                        text = "as ${member.character}",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 12.sp
                    )
                }
            }
        },
        text = {
            Column {
                if (isLoading) {
                    ShimmerBox(modifier = Modifier.fillMaxWidth().height(60.dp))
                } else {
                    val detail = personDetail
                    if (detail != null) {
                        detail.knownForDepartment?.let {
                            Text(
                                text = "Known for: $it",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 12.sp
                            )
                        }
                        detail.birthday?.let {
                            Text(
                                text = "Born: $it${detail.placeOfBirth?.let { place -> " · $place" } ?: ""}",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = detail.biography.ifBlank { "No biography available." },
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 13.sp,
                            maxLines = 8,
                            overflow = TextOverflow.Ellipsis
                        )
                    } else {
                        Text(
                            text = "Biography not available.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }
    )
}

@Composable
private fun CastMemberSkeleton() {
    Column(
        modifier = Modifier.width(84.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        ShimmerBox(modifier = Modifier.width(70.dp).height(70.dp))
        Spacer(modifier = Modifier.height(6.dp))
        ShimmerBox(modifier = Modifier.width(60.dp).height(10.dp))
    }
}

@Composable
private fun CastMemberCard(member: CastMember, onClick: () -> Unit) {
    Column(
        modifier = Modifier.width(84.dp).clickable { onClick() },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (member.profilePath != null) {
            AsyncImage(
                model = NetworkConstants.profileUrl(member.profilePath),
                contentDescription = member.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier.width(70.dp).height(70.dp).clip(CircleShape)
            )
        } else {
            Box(
                modifier = Modifier
                    .width(70.dp)
                    .height(70.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Person,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Text(
            text = member.name,
            color = MaterialTheme.colorScheme.onBackground,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            lineHeight = 13.sp,
            modifier = Modifier.padding(top = 6.dp)
        )
        Text(
            text = member.character,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 10.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}

@Composable
private fun SimilarMovieCard(movie: Movie, onClick: () -> Unit) {
    Column(modifier = Modifier.width(110.dp).clickable { onClick() }) {
        AsyncImage(
            model = NetworkConstants.posterUrl(movie.posterPath),
            contentDescription = movie.title,
            contentScale = ContentScale.Crop,
            modifier = Modifier.width(110.dp).height(160.dp).clip(RoundedCornerShape(8.dp))
        )
        Text(
            text = movie.title,
            color = MaterialTheme.colorScheme.onBackground,
            fontSize = 13.sp,
            maxLines = 1,
            modifier = Modifier.padding(top = 6.dp)
        )
    }
}