package com.yusufozturk.cinetrack.ui.viewmodel

import com.yusufozturk.cinetrack.MainDispatcherRule
import com.yusufozturk.cinetrack.data.model.Movie
import com.yusufozturk.cinetrack.data.repository.MovieRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository: MovieRepository = mockk()

    private fun movie(id: Int) = Movie(
        id = id,
        title = "Movie $id",
        overview = "overview $id",
        posterPath = null,
        backdropPath = null,
        releaseDate = "2024-01-01",
        voteAverage = 7.5
    )

    @Test
    fun `loadFirstPage basarili olunca filmler yuklenir`() = runTest {
        val movies = listOf(movie(1), movie(2))
        coEvery { repository.getPopularMovies(page = 1) } returns movies

        val viewModel = HomeViewModel(repository)
        advanceUntilIdle()

        assertEquals(movies, viewModel.movies.value)
        assertFalse(viewModel.isLoading.value)
        assertFalse(viewModel.hasError.value)
    }

    @Test
    fun `loadFirstPage hata verince hasError true olur`() = runTest {
        coEvery { repository.getPopularMovies(page = 1) } throws RuntimeException("network error")

        val viewModel = HomeViewModel(repository)
        advanceUntilIdle()

        assertTrue(viewModel.movies.value.isEmpty())
        assertTrue(viewModel.hasError.value)
        assertFalse(viewModel.isLoading.value)
    }

    @Test
    fun `refresh basarili olunca liste guncellenir`() = runTest {
        val ilkListe = listOf(movie(1), movie(2))
        coEvery { repository.getPopularMovies(page = 1) } returns ilkListe
        val viewModel = HomeViewModel(repository)
        advanceUntilIdle()

        val yeniListe = listOf(movie(3), movie(4))
        coEvery { repository.getPopularMovies(page = 1) } returns yeniListe
        viewModel.refresh()
        advanceUntilIdle()

        assertEquals(yeniListe, viewModel.movies.value)
        assertFalse(viewModel.isRefreshing.value)
        assertFalse(viewModel.hasError.value)
    }

    @Test
    fun `refresh hata verince eski liste korunur ve sessiz gecilir`() = runTest {
        val ilkListe = listOf(movie(1), movie(2))
        coEvery { repository.getPopularMovies(page = 1) } returns ilkListe
        val viewModel = HomeViewModel(repository)
        advanceUntilIdle()

        coEvery { repository.getPopularMovies(page = 1) } throws RuntimeException("network error")
        viewModel.refresh()
        advanceUntilIdle()

        assertEquals(ilkListe, viewModel.movies.value)
        assertFalse(viewModel.isRefreshing.value)
        assertFalse(viewModel.hasError.value)
    }

    @Test
    fun `loadMore ikinci sayfayi mevcut listeye ekler`() = runTest {
        val sayfa1 = listOf(movie(1), movie(2))
        coEvery { repository.getPopularMovies(page = 1) } returns sayfa1
        val viewModel = HomeViewModel(repository)
        advanceUntilIdle()

        val sayfa2 = listOf(movie(3), movie(4))
        coEvery { repository.getPopularMovies(page = 2) } returns sayfa2
        viewModel.loadMore()
        advanceUntilIdle()

        assertEquals(sayfa1 + sayfa2, viewModel.movies.value)
    }

    @Test
    fun `tekrar tekrar loadMore cagrilinca sayfa numarasi dogru artar`() = runTest {
        val sayfa1 = listOf(movie(1))
        coEvery { repository.getPopularMovies(page = 1) } returns sayfa1
        val viewModel = HomeViewModel(repository)
        advanceUntilIdle()

        val sayfa2 = listOf(movie(2))
        coEvery { repository.getPopularMovies(page = 2) } returns sayfa2
        viewModel.loadMore()
        advanceUntilIdle()

        val sayfa3 = listOf(movie(3))
        coEvery { repository.getPopularMovies(page = 3) } returns sayfa3
        viewModel.loadMore()
        advanceUntilIdle()

        assertEquals(sayfa1 + sayfa2 + sayfa3, viewModel.movies.value)
        coVerify(exactly = 1) { repository.getPopularMovies(page = 2) }
        coVerify(exactly = 1) { repository.getPopularMovies(page = 3) }
    }

    @Test
    fun `bos sayfa gelince loadMore daha fazla istek atmaz`() = runTest {
        val sayfa1 = listOf(movie(1))
        coEvery { repository.getPopularMovies(page = 1) } returns sayfa1
        val viewModel = HomeViewModel(repository)
        advanceUntilIdle()

        coEvery { repository.getPopularMovies(page = 2) } returns emptyList()
        viewModel.loadMore()
        advanceUntilIdle()
        viewModel.loadMore()
        advanceUntilIdle()

        assertEquals(sayfa1, viewModel.movies.value)
        coVerify(exactly = 0) { repository.getPopularMovies(page = 3) }
    }
}
