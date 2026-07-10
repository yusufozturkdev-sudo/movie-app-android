package com.yusufozturk.cinetrack.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.yusufozturk.cinetrack.data.local.WatchedPreferences
import com.yusufozturk.cinetrack.data.model.Movie
import com.yusufozturk.cinetrack.data.repository.AuthRepository
import com.yusufozturk.cinetrack.domain.usecase.GetRatedMoviesUseCase
import com.yusufozturk.cinetrack.domain.usecase.RateMovieUseCase
import com.yusufozturk.cinetrack.domain.usecase.ToggleWatchlistUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val authRepository = AuthRepository(application)
    private val watchedPreferences = WatchedPreferences(application)

    private val toggleWatchlistUseCase = ToggleWatchlistUseCase(authRepository)
    private val rateMovieUseCase = RateMovieUseCase(authRepository)
    private val getRatedMoviesUseCase = GetRatedMoviesUseCase(authRepository)

    private val _isLoggedIn = MutableStateFlow(authRepository.isLoggedIn())
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    private val _showLoginScreen = MutableStateFlow(false)
    val showLoginScreen: StateFlow<Boolean> = _showLoginScreen.asStateFlow()

    private val _isLoggingIn = MutableStateFlow(false)
    val isLoggingIn: StateFlow<Boolean> = _isLoggingIn.asStateFlow()

    private val _loginError = MutableStateFlow<String?>(null)
    val loginError: StateFlow<String?> = _loginError.asStateFlow()

    private val _watchlist = MutableStateFlow<List<Movie>>(emptyList())
    val watchlist: StateFlow<List<Movie>> = _watchlist.asStateFlow()

    private val _ratings = MutableStateFlow<Map<Int, Int>>(emptyMap())
    val ratings: StateFlow<Map<Int, Int>> = _ratings.asStateFlow()

    private val _watchedIds = MutableStateFlow<Set<Int>>(watchedPreferences.getWatchedIds())
    val watchedIds: StateFlow<Set<Int>> = _watchedIds.asStateFlow()

    init {
        if (authRepository.isLoggedIn()) {
            refreshWatchlist()
            refreshRatings()
        }
    }

    fun requestLogin() {
        _loginError.value = null
        _showLoginScreen.value = true
    }

    fun dismissLogin() {
        _showLoginScreen.value = false
        _loginError.value = null
    }

    fun login(username: String, password: String) {
        viewModelScope.launch {
            _isLoggingIn.value = true
            _loginError.value = null
            try {
                authRepository.loginWithCredentials(username, password)
                _isLoggedIn.value = true
                _showLoginScreen.value = false
                refreshWatchlist()
                refreshRatings()
            } catch (e: Exception) {
                _loginError.value = "Login failed. Check your username and password."
            } finally {
                _isLoggingIn.value = false
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
            _isLoggedIn.value = false
            _watchlist.value = emptyList()
            _ratings.value = emptyMap()
        }
    }

    fun refreshWatchlist() {
        viewModelScope.launch {
            try {
                _watchlist.value = authRepository.getWatchlist()
            } catch (e: Exception) {
                // Hata yönetimi ileride eklenebilir
            }
        }
    }

    fun refreshRatings() {
        viewModelScope.launch {
            try {
                _ratings.value = getRatedMoviesUseCase()
            } catch (e: Exception) {
                // Hata yönetimi ileride eklenebilir
            }
        }
    }

    fun toggleWatchlist(movie: Movie) {
        viewModelScope.launch {
            try {
                toggleWatchlistUseCase(movie, _watchlist.value)
                refreshWatchlist()
            } catch (e: Exception) {
                // Hata yönetimi ileride eklenebilir
            }
        }
    }

    fun rateMovie(movieId: Int, starRating: Int) {
        viewModelScope.launch {
            try {
                rateMovieUseCase(movieId, starRating)
                refreshRatings()
            } catch (e: Exception) {
                // Hata yönetimi ileride eklenebilir
            }
        }
    }

    fun toggleWatched(movieId: Int) {
        watchedPreferences.toggle(movieId)
        _watchedIds.value = watchedPreferences.getWatchedIds()
    }
}