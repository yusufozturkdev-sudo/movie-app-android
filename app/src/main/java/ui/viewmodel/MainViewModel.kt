package com.yusufozturk.cinetrack.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.yusufozturk.cinetrack.BuildConfig
import com.yusufozturk.cinetrack.data.api.RetrofitClient
import com.yusufozturk.cinetrack.data.local.AuthPreferences
import com.yusufozturk.cinetrack.data.local.DatabaseProvider
import com.yusufozturk.cinetrack.data.model.CreateSessionBody
import com.yusufozturk.cinetrack.data.model.DeleteSessionBody
import com.yusufozturk.cinetrack.data.model.Movie
import com.yusufozturk.cinetrack.data.model.RateMovieBody
import com.yusufozturk.cinetrack.data.model.WatchlistRequestBody
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

class MainViewModel(application: Application) : AndroidViewModel(application) {

    // Not: Room veritabanı hâlâ mevcut ama puanlama artık TMDB'ye bağlı olduğu için
    // burada aktif kullanılmıyor; ileride offline-first bir önbellek katmanı olarak
    // değerlendirilebilir.
    private val database = DatabaseProvider.getDatabase(application)
    private val authPrefs = AuthPreferences(application)

    private val _isLoggedIn = MutableStateFlow(authPrefs.isLoggedIn())
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    private val _pendingRequestToken = MutableStateFlow<String?>(null)
    val pendingRequestToken: StateFlow<String?> = _pendingRequestToken.asStateFlow()

    private val _watchlist = MutableStateFlow<List<Movie>>(emptyList())
    val watchlist: StateFlow<List<Movie>> = _watchlist.asStateFlow()

    private val _ratings = MutableStateFlow<Map<Int, Int>>(emptyMap())
    val ratings: StateFlow<Map<Int, Int>> = _ratings.asStateFlow()

    init {
        if (authPrefs.isLoggedIn()) {
            refreshWatchlist()
            refreshRatings()
        }
    }

    fun startLogin() {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.apiService.createRequestToken(BuildConfig.TMDB_API_KEY)
                _pendingRequestToken.value = response.requestToken
            } catch (e: Exception) {
                // Hata yönetimi ileride eklenebilir
            }
        }
    }

    fun cancelLogin() {
        _pendingRequestToken.value = null
    }

    fun completeLogin(approved: Boolean) {
        val token = _pendingRequestToken.value
        _pendingRequestToken.value = null
        if (!approved || token == null) return

        viewModelScope.launch {
            try {
                val session = RetrofitClient.apiService.createSession(
                    apiKey = BuildConfig.TMDB_API_KEY,
                    body = CreateSessionBody(requestToken = token)
                )
                val account = RetrofitClient.apiService.getAccount(
                    apiKey = BuildConfig.TMDB_API_KEY,
                    sessionId = session.sessionId
                )
                authPrefs.sessionId = session.sessionId
                authPrefs.accountId = account.id
                _isLoggedIn.value = true
                refreshWatchlist()
                refreshRatings()
            } catch (e: Exception) {
                // Hata yönetimi ileride eklenebilir
            }
        }
    }

    fun logout() { //ekranları mvvm'e uyarla, native login sayfayı yaz, service repository usecase kavramlarını araştır
        val sessionId = authPrefs.sessionId
        viewModelScope.launch {
            if (sessionId != null) {
                try {
                    RetrofitClient.apiService.deleteSession(
                        apiKey = BuildConfig.TMDB_API_KEY,
                        body = DeleteSessionBody(sessionId = sessionId)
                    )
                } catch (e: Exception) {
                    // Sunucu tarafında silinemese bile yerel oturumu kapatmaya devam ediyoruz
                }
            }
            authPrefs.clear()
            _isLoggedIn.value = false
            _watchlist.value = emptyList()
            _ratings.value = emptyMap()
        }
    }

    fun refreshWatchlist() {
        val sessionId = authPrefs.sessionId ?: return
        val accountId = authPrefs.accountId
        if (accountId == -1) return

        viewModelScope.launch {
            try {
                val response = RetrofitClient.apiService.getAccountWatchlist(
                    accountId = accountId,
                    apiKey = BuildConfig.TMDB_API_KEY,
                    sessionId = sessionId
                )
                _watchlist.value = response.results
            } catch (e: Exception) {
                // Hata yönetimi ileride eklenebilir
            }
        }
    }

    fun refreshRatings() {
        val sessionId = authPrefs.sessionId ?: return
        val accountId = authPrefs.accountId
        if (accountId == -1) return

        viewModelScope.launch {
            try {
                val response = RetrofitClient.apiService.getRatedMovies(
                    accountId = accountId,
                    apiKey = BuildConfig.TMDB_API_KEY,
                    sessionId = sessionId
                )
                // TMDB puanları 0.5-10 arası veriyor, bizim 5 yıldızlı sistemimize çeviriyoruz
                _ratings.value = response.results
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
        val sessionId = authPrefs.sessionId ?: return
        val accountId = authPrefs.accountId
        if (accountId == -1) return

        val alreadyIn = _watchlist.value.any { it.id == movie.id }

        viewModelScope.launch {
            try {
                RetrofitClient.apiService.updateWatchlist(
                    accountId = accountId,
                    apiKey = BuildConfig.TMDB_API_KEY,
                    sessionId = sessionId,
                    body = WatchlistRequestBody(
                        mediaType = "movie",
                        mediaId = movie.id,
                        watchlist = !alreadyIn
                    )
                )
                refreshWatchlist()
            } catch (e: Exception) {
                // Hata yönetimi ileride eklenebilir
            }
        }
    }

    fun rateMovie(movieId: Int, starRating: Int) {
        val sessionId = authPrefs.sessionId ?: return

        viewModelScope.launch {
            try {
                // Bizim 1-5 yıldızımızı TMDB'nin 0.5-10 skalasına çeviriyoruz
                val tmdbValue = (starRating * 2).toDouble()
                RetrofitClient.apiService.rateMovie(
                    movieId = movieId,
                    apiKey = BuildConfig.TMDB_API_KEY,
                    sessionId = sessionId,
                    body = RateMovieBody(value = tmdbValue)
                )
                refreshRatings()
            } catch (e: Exception) {
                // Hata yönetimi ileride eklenebilir
            }
        }
    }
}