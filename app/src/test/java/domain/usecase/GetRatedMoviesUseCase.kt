package com.yusufozturk.cinetrack.domain.usecase

import com.yusufozturk.cinetrack.data.repository.AuthRepository
import kotlin.math.roundToInt

class GetRatedMoviesUseCase(private val repository: AuthRepository) {

    suspend operator fun invoke(): Map<Int, Int> {
        return repository.getRatedMovies()
            .mapNotNull { movie ->
                // TMDB'nin 0.5-10 puanını bizim 1-5 yıldızımıza çeviriyoruz
                val tmdbRating = movie.rating ?: return@mapNotNull null
                movie.id to (tmdbRating / 2.0).roundToInt().coerceIn(1, 5)
            }
            .toMap()
    }
}