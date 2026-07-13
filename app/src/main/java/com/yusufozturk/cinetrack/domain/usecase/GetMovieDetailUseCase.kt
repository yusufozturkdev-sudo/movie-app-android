package com.yusufozturk.cinetrack.domain.usecase

import com.yusufozturk.cinetrack.data.model.CastMember
import com.yusufozturk.cinetrack.data.model.Movie
import com.yusufozturk.cinetrack.data.model.RatingFormatter
import com.yusufozturk.cinetrack.data.repository.MovieRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

class GetMovieDetailUseCase(private val repository: MovieRepository) {

    data class Result(
        val runtimeText: String?,
        val trailerKey: String?,
        val cast: List<CastMember>,
        val similarMovies: List<Movie>
    )

    suspend operator fun invoke(movieId: Int): Result = coroutineScope {
        val detailDeferred = async {
            try {
                repository.getMovieDetails(movieId).runtime?.let { RatingFormatter.formatRuntime(it) }
            } catch (e: Exception) {
                null
            }
        }
        val trailerDeferred = async {
            try {
                repository.getTrailerKey(movieId)
            } catch (e: Exception) {
                null
            }
        }
        val castDeferred = async {
            try {
                repository.getCast(movieId).take(10)
            } catch (e: Exception) {
                emptyList()
            }
        }
        val similarDeferred = async {
            try {
                repository.getSimilarMovies(movieId).take(10)
            } catch (e: Exception) {
                emptyList()
            }
        }

        Result(
            runtimeText = detailDeferred.await(),
            trailerKey = trailerDeferred.await(),
            cast = castDeferred.await(),
            similarMovies = similarDeferred.await()
        )
    }
}
