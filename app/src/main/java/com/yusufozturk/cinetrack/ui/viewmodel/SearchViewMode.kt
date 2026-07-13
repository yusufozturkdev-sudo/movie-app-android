package com.yusufozturk.cinetrack.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.yusufozturk.cinetrack.data.local.SearchHistoryPreferences
import com.yusufozturk.cinetrack.data.model.Movie
import com.yusufozturk.cinetrack.data.repository.MovieRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class GenreHighlight(val id: Int, val name: String, val imageUrl: String?)

private val featuredGenres = listOf(28 to "Action", 18 to "Drama", 878 to "Sci-Fi", 27 to "Horror")

class SearchViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = MovieRepository()
    private val historyPreferences = SearchHistoryPreferences(application)

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    private val _results = MutableStateFlow<List<Movie>>(emptyList())
    val results: StateFlow<List<Movie>> = _results.asStateFlow()

    private val _isSearching = MutableStateFlow(false)
    val isSearching: StateFlow<Boolean> = _isSearching.asStateFlow()

    private val _isRefreshingResults = MutableStateFlow(false)
    val isRefreshingResults: StateFlow<Boolean> = _isRefreshingResults.asStateFlow()

    private val _genreHighlights = MutableStateFlow<List<GenreHighlight>>(emptyList())
    val genreHighlights: StateFlow<List<GenreHighlight>> = _genreHighlights.asStateFlow()

    private val _trendingMovies = MutableStateFlow<List<Movie>>(emptyList())
    val trendingMovies: StateFlow<List<Movie>> = _trendingMovies.asStateFlow()

    private val _isLoadingExplore = MutableStateFlow(true)
    val isLoadingExplore: StateFlow<Boolean> = _isLoadingExplore.asStateFlow()

    private val _searchHistory = MutableStateFlow(historyPreferences.getHistory())
    val searchHistory: StateFlow<List<String>> = _searchHistory.asStateFlow()

    private var currentPage = 1
    private var canLoadMore = true
    private var isLoadingMore = false
    private var searchJob: kotlinx.coroutines.Job? = null

    init {
        loadExploreContent()
    }

    private fun loadExploreContent() {
        viewModelScope.launch {
            try {
                coroutineScope {
                    val highlightsDeferred = featuredGenres.map { (id, name) ->
                        async {
                            val image = try {
                                repository.getMoviesByGenre(id).firstOrNull()?.backdropPath
                            } catch (e: Exception) {
                                null
                            }
                            GenreHighlight(id, name, image?.let { "https://image.tmdb.org/t/p/w500$it" })
                        }
                    }
                    val trendingDeferred = async {
                        try {
                            repository.getTrendingMovies()
                        } catch (e: Exception) {
                            emptyList()
                        }
                    }
                    _genreHighlights.value = highlightsDeferred.awaitAll()
                    _trendingMovies.value = trendingDeferred.await()
                }
            } catch (e: Exception) {
                // Hata yönetimi ileride eklenebilir
            } finally {
                _isLoadingExplore.value = false
            }
        }
    }

    private fun runSearch(searchQuery: String, isFinal: Boolean) {
        viewModelScope.launch {
            _isSearching.value = true
            try {
                _results.value = repository.searchMovies(searchQuery, page = 1)
                currentPage = 1
                canLoadMore = true
                if (isFinal) {
                    historyPreferences.addSearch(searchQuery)
                    _searchHistory.value = historyPreferences.getHistory()
                }
            } catch (e: Exception) {
                // Hata yönetimi ileride eklenebilir
            } finally {
                _isSearching.value = false
            }
        }
    }

    fun onQueryChanged(newQuery: String) {
        _query.value = newQuery
        searchJob?.cancel()
        if (newQuery.isBlank()) {
            _results.value = emptyList()
            return
        }
        searchJob = viewModelScope.launch {
            delay(400)
            runSearch(newQuery, isFinal = true)
        }
    }

    fun searchFromHistory(term: String) {
        _query.value = term
        searchJob?.cancel()
        runSearch(term, isFinal = true)
    }

    fun clearQuery() {
        _query.value = ""
        _results.value = emptyList()
    }

    fun clearHistory() {
        historyPreferences.clearHistory()
        _searchHistory.value = emptyList()
    }

    fun refreshResults() {
        val currentQuery = _query.value
        if (currentQuery.isBlank()) return
        viewModelScope.launch {
            _isRefreshingResults.value = true
            try {
                _results.value = repository.searchMovies(currentQuery, page = 1)
                currentPage = 1
                canLoadMore = true
            } catch (e: Exception) {
                // Hata yönetimi ileride eklenebilir
            } finally {
                _isRefreshingResults.value = false
            }
        }
    }

    fun loadMoreResults() {
        val currentQuery = _query.value
        if (isLoadingMore || !canLoadMore || currentQuery.isBlank()) return
        viewModelScope.launch {
            isLoadingMore = true
            try {
                val nextPage = currentPage + 1
                val newResults = repository.searchMovies(currentQuery, page = nextPage)
                if (newResults.isEmpty()) {
                    canLoadMore = false
                } else {
                    _results.value = _results.value + newResults
                    currentPage = nextPage
                }
            } catch (e: Exception) {
                // Hata yönetimi ileride eklenebilir
            } finally {
                isLoadingMore = false
            }
        }
    }
}