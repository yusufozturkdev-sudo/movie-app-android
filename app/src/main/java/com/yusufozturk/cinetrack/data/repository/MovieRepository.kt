package com.yusufozturk.cinetrack.data.repository

import com.yusufozturk.cinetrack.BuildConfig
import com.yusufozturk.cinetrack.data.api.RetrofitClient
import com.yusufozturk.cinetrack.data.model.CastMember
import com.yusufozturk.cinetrack.data.model.Movie
import com.yusufozturk.cinetrack.data.model.MovieDetail
import com.yusufozturk.cinetrack.data.model.PersonDetail

class MovieRepository {

    private val apiService = RetrofitClient.apiService
    private val apiKey = BuildConfig.TMDB_API_KEY

    suspend fun getPopularMovies(page: Int = 1): List<Movie> {
        return apiService.getPopularMovies(apiKey = apiKey, page = page).results
    }

    suspend fun searchMovies(query: String, page: Int = 1): List<Movie> {
        return apiService.searchMovies(apiKey = apiKey, query = query, page = page).results
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

    suspend fun getCast(movieId: Int): List<CastMember> {
        return apiService.getMovieCredits(movieId = movieId, apiKey = apiKey).cast
    }

    suspend fun getSimilarMovies(movieId: Int): List<Movie> {
        return apiService.getSimilarMovies(movieId = movieId, apiKey = apiKey).results
    }

    suspend fun getPersonDetails(personId: Int): PersonDetail {
        return apiService.getPersonDetails(personId = personId, apiKey = apiKey)
    }
}
