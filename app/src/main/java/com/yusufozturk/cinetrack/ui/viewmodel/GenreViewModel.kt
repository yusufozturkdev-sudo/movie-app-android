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

    private val _hasError = MutableStateFlow(false)
    val hasError: StateFlow<Boolean> = _hasError.asStateFlow()

    private var loadedGenreId: Int? = null

    fun loadGenre(genreId: Int) {
        // Aynı kategori zaten başarıyla yüklendiyse tekrar çekme
        if (loadedGenreId == genreId && !_hasError.value) return
        loadedGenreId = genreId
        fetchGenre(genreId)
    }

    fun retry() {
        val genreId = loadedGenreId ?: return
        fetchGenre(genreId)
    }

    private fun fetchGenre(genreId: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            // _hasError bilerek burada sıfırlanmıyor: aksi halde Try Again'e basınca
            // hata ekranı bir an kaybolup skeleton görünüyor, sonra hata geri geliyordu.
            // Başarılı olursa aşağıda false yapılıyor.
            try {
                _movies.value = repository.getMoviesByGenre(genreId)
                _hasError.value = false
            } catch (e: Exception) {
                _hasError.value = true
            } finally {
                _isLoading.value = false
            }
        }
    }
}
