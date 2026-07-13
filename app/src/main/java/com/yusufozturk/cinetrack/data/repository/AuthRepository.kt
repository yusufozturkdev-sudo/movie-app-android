package com.yusufozturk.cinetrack.data.repository

import android.app.Application
import com.yusufozturk.cinetrack.BuildConfig
import com.yusufozturk.cinetrack.data.api.RetrofitClient
import com.yusufozturk.cinetrack.data.local.AuthPreferences
import com.yusufozturk.cinetrack.data.model.AccountResponse
import com.yusufozturk.cinetrack.data.model.CreateSessionBody
import com.yusufozturk.cinetrack.data.model.DeleteSessionBody
import com.yusufozturk.cinetrack.data.model.Movie
import com.yusufozturk.cinetrack.data.model.RateMovieBody
import com.yusufozturk.cinetrack.data.model.WatchlistRequestBody

class AuthRepository(application: Application) {

    private val apiService = RetrofitClient.apiService
    private val apiKey = BuildConfig.TMDB_API_KEY
    private val authPrefs = AuthPreferences(application)

    fun isLoggedIn(): Boolean = authPrefs.isLoggedIn()

    suspend fun loginWithCredentials(username: String, password: String) {
        val tokenResponse = apiService.createRequestToken(apiKey)

        val validated = apiService.validateWithLogin(
            apiKey = apiKey,
            username = username,
            password = password,
            requestToken = tokenResponse.requestToken
        )

        val session = apiService.createSession(
            apiKey = apiKey,
            body = CreateSessionBody(requestToken = validated.requestToken)
        )

        val account = apiService.getAccount(apiKey = apiKey, sessionId = session.sessionId)

        authPrefs.sessionId = session.sessionId
        authPrefs.accountId = account.id
    }

    suspend fun getAccountInfo(): AccountResponse? {
        val sessionId = authPrefs.sessionId ?: return null
        return apiService.getAccount(apiKey = apiKey, sessionId = sessionId)
    }

    suspend fun logout() {
        val sessionId = authPrefs.sessionId
        if (sessionId != null) {
            try {
                apiService.deleteSession(apiKey = apiKey, body = DeleteSessionBody(sessionId = sessionId))
            } catch (e: Exception) {
                // Sunucu tarafında silinemese bile yerel oturumu kapatmaya devam ediyoruz
            }
        }
        authPrefs.clear()
    }

    suspend fun getWatchlist(): List<Movie> {
        val sessionId = authPrefs.sessionId ?: return emptyList()
        val accountId = authPrefs.accountId
        if (accountId == -1) return emptyList()

        return apiService.getAccountWatchlist(
            accountId = accountId,
            apiKey = apiKey,
            sessionId = sessionId
        ).results
    }

    suspend fun toggleWatchlist(movie: Movie, addToWatchlist: Boolean) {
        val sessionId = authPrefs.sessionId ?: return
        val accountId = authPrefs.accountId
        if (accountId == -1) return

        apiService.updateWatchlist(
            accountId = accountId,
            apiKey = apiKey,
            sessionId = sessionId,
            body = WatchlistRequestBody(
                mediaType = "movie",
                mediaId = movie.id,
                watchlist = addToWatchlist
            )
        )
    }

    suspend fun getRatedMovies(): List<Movie> {
        val sessionId = authPrefs.sessionId ?: return emptyList()
        val accountId = authPrefs.accountId
        if (accountId == -1) return emptyList()

        return apiService.getRatedMovies(
            accountId = accountId,
            apiKey = apiKey,
            sessionId = sessionId
        ).results
    }

    suspend fun rateMovie(movieId: Int, tmdbValue: Double) {
        val sessionId = authPrefs.sessionId ?: return

        apiService.rateMovie(
            movieId = movieId,
            apiKey = apiKey,
            sessionId = sessionId,
            body = RateMovieBody(value = tmdbValue)
        )
    }
}
