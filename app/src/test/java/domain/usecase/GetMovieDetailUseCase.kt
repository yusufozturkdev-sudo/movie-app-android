package com.yusufozturk.cinetrack.domain.usecase

import com.yusufozturk.cinetrack.data.model.RatingFormatter
import com.yusufozturk.cinetrack.data.repository.MovieRepository

class GetMovieDetailUseCase(private val repository: MovieRepository) {

    data class Result(
        val runtimeText: String?,
        val trailerKey: String?
    )

    suspend operator fun invoke(movieId: Int): Result {
        val runtimeText = try {
            repository.getMovieDetails(movieId).runtime?.let { RatingFormatter.formatRuntime(it) }
        } catch (e: Exception) {
            null
        }

        val trailerKey = try {
            repository.getTrailerKey(movieId)
        } catch (e: Exception) {
            null
        }

        return Result(runtimeText = runtimeText, trailerKey = trailerKey)
    }
}