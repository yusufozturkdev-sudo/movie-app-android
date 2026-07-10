package com.yusufozturk.cinetrack.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.yusufozturk.cinetrack.data.model.Movie
import com.yusufozturk.cinetrack.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val authRepository = AuthRepository(application)

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
                _ratings.value = authRepository.getRatedMovies()
                    .mapNotNull { movie ->
                        val tmdbRating = movie.rating ?: return@mapNotNull null
                        movie.id to (tmdbRating / 2.0).roundToInt().coerceIn(1, 5)
                    }
                    .toMap()
            } catch (e: Exception) {
                // Hata yönetimi ileride eklenebilir
            }
        }
    }

    fun toggleWatchlist(movie: Movie) {
        val alreadyIn = _watchlist.value.any { it.id == movie.id }

        viewModelScope.launch {
            try {
                authRepository.toggleWatchlist(movie, addToWatchlist = !alreadyIn)
                refreshWatchlist()
            } catch (e: Exception) {
                // Hata yönetimi ileride eklenebilir
            }
        }
    }

    fun rateMovie(movieId: Int, starRating: Int) {
        viewModelScope.launch {
            try {
                val tmdbValue = (starRating * 2).toDouble()
                authRepository.rateMovie(movieId, tmdbValue)
                refreshRatings()
            } catch (e: Exception) {
                // Hata yönetimi ileride eklenebilir
            }
        }
    }
}