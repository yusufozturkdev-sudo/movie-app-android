package com.yusufozturk.cinetrack.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.yusufozturk.cinetrack.data.api.NetworkConstants
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

    private val _hasExploreError = MutableStateFlow(false)
    val hasExploreError: StateFlow<Boolean> = _hasExploreError.asStateFlow()

    private val _hasSearchError = MutableStateFlow(false)
    val hasSearchError: StateFlow<Boolean> = _hasSearchError.asStateFlow()

    private var currentPage = 1
    private var canLoadMore = true
    private var isLoadingMore = false
    private var searchJob: kotlinx.coroutines.Job? = null

    init {
        loadExploreContent()
    }

    fun loadExploreContent() {
        viewModelScope.launch {
            _isLoadingExplore.value = true
            fun loadExploreContent() {
                viewModelScope.launch {
                    _isLoadingExplore.value = true
                    // _hasExploreError BİLEREK burada sıfırlanmıyor:
                    // aksi halde Try Again'e basınca hata ekranı bir an kaybolup
                    // içerik görünüyor, sonra hata geri geliyordu (titreme).
                    // Başarılı olursa aşağıda false yapılıyor.
                    try {
                        coroutineScope {
                            val highlightsDeferred = featuredGenres.map { (id, name) ->
                                async {
                                    // Tek bir kategorinin görseli gelmezse sorun değil, sadece görsel boş kalır
                                    val image = try {
                                        repository.getMoviesByGenre(id).firstOrNull()?.backdropPath
                                    } catch (e: Exception) {
                                        null
                                    }
                                    GenreHighlight(id, name, image?.let { NetworkConstants.backdropUrl(it) })
                                }
                            }
                            // Trending BİLEREK korumasız bırakıldı: burası patlarsa gerçekten
                            // ağ sorunu var demektir ve hata ekranı gösterilmeli
                            val trendingDeferred = async { repository.getTrendingMovies() }

                            _genreHighlights.value = highlightsDeferred.awaitAll()
                            _trendingMovies.value = trendingDeferred.await()
                        }
                        _hasExploreError.value = false
                    } catch (e: Exception) {
                        _hasExploreError.value = true
                    } finally {
                        _isLoadingExplore.value = false
                    }
                }
            }
            try {
                coroutineScope {
                    val highlightsDeferred = featuredGenres.map { (id, name) ->
                        async {
                            // Tek bir kategorinin görseli gelmezse sorun değil, sadece görsel boş kalır
                            val image = try {
                                repository.getMoviesByGenre(id).firstOrNull()?.backdropPath
                            } catch (e: Exception) {
                                null
                            }
                            GenreHighlight(id, name, image?.let { NetworkConstants.backdropUrl(it) })
                        }
                    }
                    // Trending BİLEREK korumasız bırakıldı: burası patlarsa gerçekten
                    // ağ sorunu var demektir ve hata ekranı gösterilmeli
                    val trendingDeferred = async { repository.getTrendingMovies() }

                    _genreHighlights.value = highlightsDeferred.awaitAll()
                    _trendingMovies.value = trendingDeferred.await()
                }
            } catch (e: Exception) {
                _hasExploreError.value = true
            } finally {
                _isLoadingExplore.value = false
            }
        }
    }

    private fun runSearch(searchQuery: String, isFinal: Boolean) {
        viewModelScope.launch {
            _isSearching.value = true
            _hasSearchError.value = false
            try {
                _results.value = repository.searchMovies(searchQuery, page = 1)
                currentPage = 1
                canLoadMore = true
                if (isFinal) {
                    historyPreferences.addSearch(searchQuery)
                    _searchHistory.value = historyPreferences.getHistory()
                }
            } catch (e: Exception) {
                _hasSearchError.value = true
            } finally {
                _isSearching.value = false
            }
        }
    }

    fun retrySearch() {
        val currentQuery = _query.value
        if (currentQuery.isBlank()) return
        // isFinal = false: terim geçmişe zaten eklendi, tekrar eklemeye gerek yok
        runSearch(currentQuery, isFinal = false)
    }

    fun onQueryChanged(newQuery: String) {
        _query.value = newQuery
        searchJob?.cancel()
        if (newQuery.isBlank()) {
            _results.value = emptyList()
            _hasSearchError.value = false
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
        _hasSearchError.value = false
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
                _hasSearchError.value = false
            } catch (e: Exception) {
                // Refresh hatası sessiz geçilir, kullanıcı eski sonuçları görmeye devam eder
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
                // Sayfalama hatası sessiz geçilir, ana liste zaten yüklü
            } finally {
                isLoadingMore = false
            }
        }
    }
}
