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
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.viewmodel.compose.viewModel
import com.yusufozturk.cinetrack.data.model.GenreMapper
import com.yusufozturk.cinetrack.data.model.Movie
import com.yusufozturk.cinetrack.ui.screens.AccountSettingsScreen
import com.yusufozturk.cinetrack.ui.screens.CategoriesScreen
import com.yusufozturk.cinetrack.ui.screens.GenreScreen
import com.yusufozturk.cinetrack.ui.screens.HomeScreen
import com.yusufozturk.cinetrack.ui.screens.LoginScreen
import com.yusufozturk.cinetrack.ui.screens.MovieDetailScreen
import com.yusufozturk.cinetrack.ui.screens.NotificationSettingsScreen
import com.yusufozturk.cinetrack.ui.screens.ProfileScreen
import com.yusufozturk.cinetrack.ui.screens.SearchScreen
import com.yusufozturk.cinetrack.ui.screens.WatchlistScreen
import com.yusufozturk.cinetrack.ui.theme.CineTrackTheme
import com.yusufozturk.cinetrack.ui.viewmodel.MainViewModel

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
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

    val movieStack = remember { mutableStateListOf<Movie>() }
    val selectedMovie: Movie? = movieStack.lastOrNull()

    var selectedGenre by remember { mutableStateOf<Pair<Int, String>?>(null) }
    var showAllCategories by remember { mutableStateOf(false) }
    var showAccountSettings by remember { mutableStateOf(false) }
    var showNotificationSettings by remember { mutableStateOf(false) }

    val watchlist by viewModel.watchlist.collectAsState()
    val ratings by viewModel.ratings.collectAsState()
    val watchedIds by viewModel.watchedIds.collectAsState()
    val isLoggedIn by viewModel.isLoggedIn.collectAsState()
    val showLoginScreen by viewModel.showLoginScreen.collectAsState()
    val isLoggingIn by viewModel.isLoggingIn.collectAsState()
    val loginError by viewModel.loginError.collectAsState()

    fun requireLoginThenToggle(movie: Movie) {
        if (isLoggedIn) {
            viewModel.toggleWatchlist(movie)
        } else {
            viewModel.requestLogin()
        }
    }

    fun requireLoginThenRate(movieId: Int, rating: Int) {
        if (isLoggedIn) {
            viewModel.rateMovie(movieId, rating)
        } else {
            viewModel.requestLogin()
        }
    }

    fun pushMovie(movie: Movie) {
        movieStack.add(movie)
    }

    fun handleBack() {
        when {
            showLoginScreen -> viewModel.dismissLogin()
            showAccountSettings -> showAccountSettings = false
            showNotificationSettings -> showNotificationSettings = false
            movieStack.isNotEmpty() -> movieStack.removeAt(movieStack.lastIndex)
            selectedGenre != null -> selectedGenre = null
            showAllCategories -> showAllCategories = false
        }
    }

    BackHandler(
        enabled = showLoginScreen || showAccountSettings || showNotificationSettings ||
                movieStack.isNotEmpty() || selectedGenre != null || showAllCategories
    ) {
        handleBack()
    }

    if (showLoginScreen) {
        LoginScreen(
            isLoading = isLoggingIn,
            errorMessage = loginError,
            onLogin = { username, password -> viewModel.login(username, password) },
            onCancel = { viewModel.dismissLogin() }
        )
        return
    }

    if (showAccountSettings) {
        AccountSettingsScreen(
            onLogout = { viewModel.logout() },
            onBackClick = { handleBack() }
        )
        return
    }

    if (showNotificationSettings) {
        NotificationSettingsScreen(onBackClick = { handleBack() })
        return
    }

    if (selectedMovie != null) {
        MovieDetailScreen(
            movie = selectedMovie,
            isInWatchlist = watchlist.any { it.id == selectedMovie.id },
            onToggleWatchlist = { requireLoginThenToggle(selectedMovie) },
            isWatched = watchedIds.contains(selectedMovie.id),
            onToggleWatched = { viewModel.toggleWatched(selectedMovie.id) },
            userRating = ratings[selectedMovie.id] ?: 0,
            onRateMovie = { rating -> requireLoginThenRate(selectedMovie.id, rating) },
            onGenreClick = { genreName ->
                val genreId = GenreMapper.idFor(genreName)
                if (genreId != null) {
                    selectedGenre = genreId to genreName
                    movieStack.clear()
                }
            },
            onMovieClick = { movie -> pushMovie(movie) },
            onBackClick = { handleBack() }
        )
        return
    }

    selectedGenre?.let { (genreId, genreName) ->
        GenreScreen(
            genreId = genreId,
            genreName = genreName,
            onMovieClick = { movie -> pushMovie(movie) },
            onBackClick = { handleBack() }
        )
        return
    }

    if (showAllCategories) {
        CategoriesScreen(
            onGenreClick = { genreId, genreName ->
                selectedGenre = genreId to genreName
                showAllCategories = false
            },
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
                    onMovieClick = { movie -> pushMovie(movie) },
                    watchlist = watchlist,
                    onToggleWatchlist = { movie -> requireLoginThenToggle(movie) }
                )
                1 -> SearchScreen(
                    onMovieClick = { movie -> pushMovie(movie) },
                    onGenreClick = { genreId, genreName -> selectedGenre = genreId to genreName },
                    onSeeAllCategoriesClick = { showAllCategories = true }
                )
                2 -> WatchlistScreen(
                    watchlist = watchlist,
                    onRemove = { movie -> requireLoginThenToggle(movie) },
                    onMovieClick = { movie -> pushMovie(movie) }
                )
                else -> ProfileScreen(
                    watchlistCount = watchlist.size,
                    ratedCount = ratings.size,
                    watchedCount = watchedIds.size,
                    isLoggedIn = isLoggedIn,
                    onLoginClick = { viewModel.requestLogin() },
                    onLogoutClick = { viewModel.logout() },
                    onAccountSettingsClick = {
                        if (isLoggedIn) showAccountSettings = true else viewModel.requestLogin()
                    },
                    onNotificationsClick = { showNotificationSettings = true }
                )
            }
        }
    }
}