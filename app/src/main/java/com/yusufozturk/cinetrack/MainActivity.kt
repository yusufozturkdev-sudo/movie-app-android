package com.yusufozturk.cinetrack

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.yusufozturk.cinetrack.data.model.GenreMapper
import com.yusufozturk.cinetrack.data.model.Movie
import com.yusufozturk.cinetrack.ui.screens.GenreScreen
import com.yusufozturk.cinetrack.ui.screens.HomeScreen
import com.yusufozturk.cinetrack.ui.screens.LoginScreen
import com.yusufozturk.cinetrack.ui.screens.MovieDetailScreen
import com.yusufozturk.cinetrack.ui.screens.ProfileScreen
import com.yusufozturk.cinetrack.ui.screens.SearchScreen
import com.yusufozturk.cinetrack.ui.screens.WatchlistScreen
import com.yusufozturk.cinetrack.ui.theme.CineTrackTheme
import com.yusufozturk.cinetrack.ui.viewmodel.MainViewModel

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CineTrackTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    FlicksApp()
                }
            }
        }
    }
}

@Composable
fun FlicksApp(viewModel: MainViewModel = viewModel()) {
    var selectedTab by remember { mutableIntStateOf(0) }
    var selectedMovie by remember { mutableStateOf<Movie?>(null) }
    var selectedGenre by remember { mutableStateOf<Pair<Int, String>?>(null) }

    val watchlist by viewModel.watchlist.collectAsState()
    val ratings by viewModel.ratings.collectAsState()
    val isLoggedIn by viewModel.isLoggedIn.collectAsState()
    val pendingRequestToken by viewModel.pendingRequestToken.collectAsState()

    fun requireLoginThenToggle(movie: Movie) {
        if (isLoggedIn) {
            viewModel.toggleWatchlist(movie)
        } else {
            viewModel.startLogin()
        }
    }

    fun handleBack() {
        when {
            pendingRequestToken != null -> viewModel.cancelLogin()
            selectedMovie != null -> selectedMovie = null
            selectedGenre != null -> selectedGenre = null
        }
    }

    BackHandler(enabled = pendingRequestToken != null || selectedMovie != null || selectedGenre != null) {
        handleBack()
    }

    pendingRequestToken?.let { token ->
        LoginScreen(
            requestToken = token,
            onRedirect = { approved -> viewModel.completeLogin(approved) },
            onCancel = { viewModel.cancelLogin() }
        )
        return
    }

    if (selectedMovie != null) {
        MovieDetailScreen(
            movie = selectedMovie,
            isInWatchlist = watchlist.any { it.id == selectedMovie?.id },
            onToggleWatchlist = { selectedMovie?.let { requireLoginThenToggle(it) } },
            userRating = selectedMovie?.let { ratings[it.id] } ?: 0,
            onRateMovie = { rating -> selectedMovie?.let { viewModel.rateMovie(it.id, rating) } },
            onGenreClick = { genreName ->
                val genreId = GenreMapper.idFor(genreName)
                if (genreId != null) {
                    selectedGenre = genreId to genreName
                    selectedMovie = null
                }
            },
            onBackClick = { handleBack() }
        )
        return
    }

    selectedGenre?.let { (genreId, genreName) ->
        GenreScreen(
            genreId = genreId,
            genreName = genreName,
            onMovieClick = { movie -> selectedMovie = movie },
            onBackClick = { handleBack() }
        )
        return
    }

    Scaffold(
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                    label = { Text("Home") }
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                    label = { Text("Search") }
                )
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    icon = { Icon(Icons.Default.Star, contentDescription = "Watchlist") },
                    label = { Text("Watchlist") }
                )
                NavigationBarItem(
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 },
                    icon = { Icon(Icons.Default.Person, contentDescription = "Profile") },
                    label = { Text("Profile") }
                )
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            when (selectedTab) {
                0 -> HomeScreen(
                    onMovieClick = { movie -> selectedMovie = movie },
                    watchlist = watchlist,
                    onToggleWatchlist = { movie -> requireLoginThenToggle(movie) }
                )
                1 -> SearchScreen(
                    onMovieClick = { movie -> selectedMovie = movie },
                    onGenreClick = { genreId, genreName -> selectedGenre = genreId to genreName }
                )
                2 -> WatchlistScreen(
                    watchlist = watchlist,
                    onRemove = { movie -> requireLoginThenToggle(movie) },
                    onMovieClick = { movie -> selectedMovie = movie }
                )
                else -> ProfileScreen(
                    watchlistCount = watchlist.size,
                    ratedCount = ratings.size,
                    isLoggedIn = isLoggedIn,
                    onLoginClick = { viewModel.startLogin() },
                    onLogoutClick = { viewModel.logout() }
                )
            }
        }
    }
}