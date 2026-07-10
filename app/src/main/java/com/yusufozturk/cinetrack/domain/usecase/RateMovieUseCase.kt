package com.yusufozturk.cinetrack.domain.usecase

import com.yusufozturk.cinetrack.data.repository.AuthRepository

class RateMovieUseCase(private val repository: AuthRepository) {

    suspend operator fun invoke(movieId: Int, starRating: Int) {
        require(starRating in 1..5) { "Star rating must be between 1 and 5" }
        val tmdbValue = (starRating * 2).toDouble()
        repository.rateMovie(movieId, tmdbValue)
    }
}
