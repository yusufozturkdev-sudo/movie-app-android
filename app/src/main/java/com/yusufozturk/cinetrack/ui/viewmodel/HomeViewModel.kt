package com.yusufozturk.cinetrack.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yusufozturk.cinetrack.data.model.Movie
import com.yusufozturk.cinetrack.data.repository.MovieRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HomeViewModel : ViewModel() {

    private val repository = MovieRepository()

    private val _movies = MutableStateFlow<List<Movie>>(emptyList())
    val movies: StateFlow<List<Movie>> = _movies.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    private val _hasError = MutableStateFlow(false)
    val hasError: StateFlow<Boolean> = _hasError.asStateFlow()

    private var currentPage = 1
    private var canLoadMore = true
    private var isLoadingMore = false

    init {
        loadFirstPage()
    }

    fun loadFirstPage() {
        viewModelScope.launch {
            _isLoading.value = true
            _hasError.value = false
            try {
                _movies.value = repository.getPopularMovies(page = 1)
                currentPage = 1
                canLoadMore = true
            } catch (e: Exception) {
                _hasError.value = true
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun refresh() {
        viewModelScope.launch {
            _isRefreshing.value = true
            try {
                _movies.value = repository.getPopularMovies(page = 1)
                currentPage = 1
                canLoadMore = true
                _hasError.value = false
            } catch (e: Exception) {
                // Zaten liste doluysa hatayı sessizce yut, kullanıcı eski veriyi görmeye devam etsin
            } finally {
                _isRefreshing.value = false
            }
        }
    }

    fun loadMore() {
        if (isLoadingMore || !canLoadMore) return
        viewModelScope.launch {
            isLoadingMore = true
            try {
                val nextPage = currentPage + 1
                val newMovies = repository.getPopularMovies(page = nextPage)
                if (newMovies.isEmpty()) {
                    canLoadMore = false
                } else {
                    _movies.value = _movies.value + newMovies
                    currentPage = nextPage
                }
            } catch (e: Exception) {
                // Sayfalama hatası sessiz geçilir, ana liste zaten yüklü
            } finally {
                isLoadingMore = false
            }
        }
    }
}