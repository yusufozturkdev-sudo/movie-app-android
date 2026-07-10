package com.yusufozturk.cinetrack.domain.usecase

import com.yusufozturk.cinetrack.data.model.Movie
import com.yusufozturk.cinetrack.data.repository.AuthRepository

class ToggleWatchlistUseCase(private val repository: AuthRepository) {

    suspend operator fun invoke(movie: Movie, currentWatchlist: List<Movie>) {
        val alreadyIn = currentWatchlist.any { it.id == movie.id }
        repository.toggleWatchlist(movie, addToWatchlist = !alreadyIn)
    }
}
