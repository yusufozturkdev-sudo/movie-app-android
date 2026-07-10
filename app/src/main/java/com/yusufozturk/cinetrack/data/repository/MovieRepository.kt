package com.yusufozturk.cinetrack.data.repository

import com.yusufozturk.cinetrack.BuildConfig
import com.yusufozturk.cinetrack.data.api.RetrofitClient
import com.yusufozturk.cinetrack.data.model.Movie
import com.yusufozturk.cinetrack.data.model.MovieDetail

class MovieRepository {

    private val apiService = RetrofitClient.apiService
    private val apiKey = BuildConfig.TMDB_API_KEY

    suspend fun getPopularMovies(): List<Movie> {
        return apiService.getPopularMovies(apiKey = apiKey).results
    }

    suspend fun searchMovies(query: String): List<Movie> {
        return apiService.searchMovies(apiKey = apiKey, query = query).results
    }

    suspend fun getMoviesByGenre(genreId: Int): List<Movie> {
        return apiService.discoverMoviesByGenre(apiKey = apiKey, genreId = genreId).results
    }

    suspend fun getTrendingMovies(): List<Movie> {
        return apiService.getTrendingMovies(apiKey = apiKey).results
    }

    suspend fun getMovieDetails(movieId: Int): MovieDetail {
        return apiService.getMovieDetails(movieId = movieId, apiKey = apiKey)
    }

    suspend fun getTrailerKey(movieId: Int): String? {
        val videos = apiService.getMovieVideos(movieId = movieId, apiKey = apiKey)
        return videos.results
            .filter { it.site == "YouTube" && it.type == "Trailer" }
            .let { trailers -> trailers.find { it.isOfficial } ?: trailers.firstOrNull() }
            ?.key
    }
}