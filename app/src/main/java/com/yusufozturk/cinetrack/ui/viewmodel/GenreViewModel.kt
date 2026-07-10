package com.yusufozturk.cinetrack.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yusufozturk.cinetrack.data.model.Movie
import com.yusufozturk.cinetrack.data.repository.MovieRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class GenreViewModel : ViewModel() {

    private val repository = MovieRepository()

    private val _movies = MutableStateFlow<List<Movie>>(emptyList())
    val movies: StateFlow<List<Movie>> = _movies.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private var loadedGenreId: Int? = null

    fun loadGenre(genreId: Int) {
        if (loadedGenreId == genreId) return
        loadedGenreId = genreId
        viewModelScope.launch {
            _isLoading.value = true
            try {
                _movies.value = repository.getMoviesByGenre(genreId)
            } catch (e: Exception) {
                // Hata yönetimi ileride eklenebilir
            } finally {
                _isLoading.value = false
            }
        }
    }
}