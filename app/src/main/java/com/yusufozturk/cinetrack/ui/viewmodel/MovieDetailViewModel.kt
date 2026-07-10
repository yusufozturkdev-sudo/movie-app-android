package com.yusufozturk.cinetrack.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yusufozturk.cinetrack.data.model.CastMember
import com.yusufozturk.cinetrack.data.model.Movie
import com.yusufozturk.cinetrack.data.repository.MovieRepository
import com.yusufozturk.cinetrack.domain.usecase.GetMovieDetailUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MovieDetailViewModel : ViewModel() {

    private val getMovieDetailUseCase = GetMovieDetailUseCase(MovieRepository())

    private val _isLoadingDetails = MutableStateFlow(true)
    val isLoadingDetails: StateFlow<Boolean> = _isLoadingDetails.asStateFlow()

    private val _runtimeText = MutableStateFlow<String?>(null)
    val runtimeText: StateFlow<String?> = _runtimeText.asStateFlow()

    private val _trailerKey = MutableStateFlow<String?>(null)
    val trailerKey: StateFlow<String?> = _trailerKey.asStateFlow()

    private val _cast = MutableStateFlow<List<CastMember>>(emptyList())
    val cast: StateFlow<List<CastMember>> = _cast.asStateFlow()

    private val _similarMovies = MutableStateFlow<List<Movie>>(emptyList())
    val similarMovies: StateFlow<List<Movie>> = _similarMovies.asStateFlow()

    private var loadedMovieId: Int? = null

    fun loadDetails(movieId: Int) {
        if (loadedMovieId == movieId) return
        loadedMovieId = movieId

        _isLoadingDetails.value = true
        _runtimeText.value = null
        _trailerKey.value = null
        _cast.value = emptyList()
        _similarMovies.value = emptyList()

        viewModelScope.launch {
            try {
                val result = getMovieDetailUseCase(movieId)
                _runtimeText.value = result.runtimeText
                _trailerKey.value = result.trailerKey
                _cast.value = result.cast
                _similarMovies.value = result.similarMovies
            } catch (e: Exception) {
                // Hata yönetimi ileride eklenebilir
            } finally {
                _isLoadingDetails.value = false
            }
        }
    }
}