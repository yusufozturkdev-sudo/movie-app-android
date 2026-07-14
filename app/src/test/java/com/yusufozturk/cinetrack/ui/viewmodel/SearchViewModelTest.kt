package com.yusufozturk.cinetrack.ui.viewmodel

import com.yusufozturk.cinetrack.MainDispatcherRule

import android.app.Application
import com.yusufozturk.cinetrack.data.local.SearchHistoryPreferences
import com.yusufozturk.cinetrack.data.model.Movie
import com.yusufozturk.cinetrack.data.repository.MovieRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class SearchViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var repository: MovieRepository
    private lateinit var historyPreferences: SearchHistoryPreferences
    private lateinit var application: Application

    private fun movie(id: Int, title: String) = Movie(
        id = id,
        title = title,
        overview = "",
        posterPath = null,
        backdropPath = null,
        releaseDate = null,
        voteAverage = 0.0,
        genreIds = emptyList()
    )

    @Before
    fun setup() {
        repository = mockk()
        historyPreferences = mockk(relaxed = true)
        application = mockk(relaxed = true)

        // init { loadExploreContent() } her testte tetiklendigi icin
        // varsayilan davranislari burada tanimliyoruz
        coEvery { repository.getMoviesByGenre(any()) } returns emptyList()
        coEvery { repository.getTrendingMovies() } returns listOf(movie(1, "trend1"))
        every { historyPreferences.getHistory() } returns emptyList()
    }

    private fun createViewModel() =
        SearchViewModel(repository, historyPreferences, application)

    @Test
    fun `loadExploreContent basarili olunca trending ve genre highlights dolar`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        assertEquals(1, viewModel.trendingMovies.value.size)
        assertFalse(viewModel.hasExploreError.value)
        assertFalse(viewModel.isLoadingExplore.value)
    }

    @Test
    fun `loadExploreContent trending hata verince hasExploreError true olur`() = runTest {
        coEvery { repository.getTrendingMovies() } throws RuntimeException("network error")

        val viewModel = createViewModel()
        advanceUntilIdle()

        assertTrue(viewModel.hasExploreError.value)
        assertFalse(viewModel.isLoadingExplore.value)
    }

    @Test
    fun `onQueryChanged 400ms dolmadan arama tetiklenmez`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        coEvery { repository.searchMovies("batman", page = 1) } returns listOf(movie(1, "Batman"))

        viewModel.onQueryChanged("batman")
        advanceTimeBy(200)

        assertTrue(viewModel.results.value.isEmpty())
        coVerify(exactly = 0) { repository.searchMovies(any(), any()) }
    }

    @Test
    fun `onQueryChanged 400ms sonra arama tetiklenir ve gecmise eklenir`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        coEvery { repository.searchMovies("batman", page = 1) } returns listOf(movie(1, "Batman"))

        viewModel.onQueryChanged("batman")
        advanceUntilIdle()

        assertEquals(1, viewModel.results.value.size)
        coVerify(exactly = 1) { historyPreferences.addSearch("batman") }
    }

    @Test
    fun `hizli yazarken onceki debounce iptal edilir, sadece son sorgu aranir`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        coEvery { repository.searchMovies("bat", page = 1) } returns emptyList()
        coEvery { repository.searchMovies("batman", page = 1) } returns listOf(movie(1, "Batman"))

        viewModel.onQueryChanged("bat")
        advanceTimeBy(200)
        viewModel.onQueryChanged("batman")
        advanceUntilIdle()

        coVerify(exactly = 0) { repository.searchMovies("bat", page = 1) }
        coVerify(exactly = 1) { repository.searchMovies("batman", page = 1) }
    }

    @Test
    fun `onQueryChanged bos sorgu ile sonuclari hemen temizler`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        coEvery { repository.searchMovies("batman", page = 1) } returns listOf(movie(1, "Batman"))
        viewModel.onQueryChanged("batman")
        advanceUntilIdle()

        viewModel.onQueryChanged("")
        advanceUntilIdle()

        assertTrue(viewModel.results.value.isEmpty())
    }

    @Test
    fun `retrySearch son sorguyu tekrar arar ama gecmise tekrar eklemez`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        coEvery { repository.searchMovies("batman", page = 1) } returns listOf(movie(1, "Batman"))
        viewModel.onQueryChanged("batman")
        advanceUntilIdle()

        viewModel.retrySearch()
        advanceUntilIdle()

        coVerify(exactly = 1) { historyPreferences.addSearch("batman") }
        coVerify(exactly = 2) { repository.searchMovies("batman", page = 1) }
    }

    @Test
    fun `searchFromHistory bekleyen debounce'u iptal edip hemen arar`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        coEvery { repository.searchMovies("bat", page = 1) } returns emptyList()
        coEvery { repository.searchMovies("batman", page = 1) } returns listOf(movie(1, "Batman"))

        viewModel.onQueryChanged("bat")
        viewModel.searchFromHistory("batman")
        advanceUntilIdle()

        assertEquals("batman", viewModel.query.value)
        coVerify(exactly = 0) { repository.searchMovies("bat", page = 1) }
        coVerify(exactly = 1) { repository.searchMovies("batman", page = 1) }
    }

    @Test
    fun `clearQuery sorguyu ve sonuclari sifirlar`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        coEvery { repository.searchMovies("batman", page = 1) } returns listOf(movie(1, "Batman"))
        viewModel.onQueryChanged("batman")
        advanceUntilIdle()

        viewModel.clearQuery()

        assertEquals("", viewModel.query.value)
        assertTrue(viewModel.results.value.isEmpty())
    }

    @Test
    fun `clearHistory gecmisi temizler`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.clearHistory()

        assertTrue(viewModel.searchHistory.value.isEmpty())
        coVerify(exactly = 1) { historyPreferences.clearHistory() }
    }

    @Test
    fun `loadMoreResults ikinci sayfayi mevcut listeye ekler`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        coEvery { repository.searchMovies("batman", page = 1) } returns listOf(movie(1, "Batman"))
        coEvery { repository.searchMovies("batman", page = 2) } returns listOf(movie(2, "Batman Returns"))

        viewModel.onQueryChanged("batman")
        advanceUntilIdle()
        viewModel.loadMoreResults()
        advanceUntilIdle()

        assertEquals(2, viewModel.results.value.size)
    }

    @Test
    fun `loadMoreResults bos sayfa gelince daha fazla istek atmaz`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        coEvery { repository.searchMovies("batman", page = 1) } returns listOf(movie(1, "Batman"))
        coEvery { repository.searchMovies("batman", page = 2) } returns emptyList()

        viewModel.onQueryChanged("batman")
        advanceUntilIdle()
        viewModel.loadMoreResults()
        advanceUntilIdle()
        viewModel.loadMoreResults()
        advanceUntilIdle()

        coVerify(exactly = 1) { repository.searchMovies("batman", page = 2) }
    }
}
