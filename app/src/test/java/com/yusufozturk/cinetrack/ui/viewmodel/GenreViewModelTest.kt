package com.yusufozturk.cinetrack.ui.viewmodel

import com.yusufozturk.cinetrack.MainDispatcherRule
import com.yusufozturk.cinetrack.data.model.Movie
import com.yusufozturk.cinetrack.data.repository.MovieRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class GenreViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository: MovieRepository = mockk()

    private fun sampleMovie(id: Int) = Movie(
        id = id,
        title = "Movie $id",
        overview = "Overview $id",
        posterPath = null,
        backdropPath = null,
        releaseDate = "2026-01-01",
        voteAverage = 7.5,
        genreIds = listOf(28)
    )

    @Test
    fun `loadGenre success updates movies and clears loading and error`() = runTest {
        val movies = listOf(sampleMovie(1), sampleMovie(2))
        coEvery { repository.getMoviesByGenre(28) } returns movies

        val viewModel = GenreViewModel(repository)
        viewModel.loadGenre(28)
        advanceUntilIdle()

        assertEquals(movies, viewModel.movies.value)
        assertFalse(viewModel.isLoading.value)
        assertFalse(viewModel.hasError.value)
    }

    @Test
    fun `loadGenre failure sets hasError true and stops loading`() = runTest {
        coEvery { repository.getMoviesByGenre(28) } throws RuntimeException("network error")

        val viewModel = GenreViewModel(repository)
        viewModel.loadGenre(28)
        advanceUntilIdle()

        assertTrue(viewModel.hasError.value)
        assertFalse(viewModel.isLoading.value)
        assertTrue(viewModel.movies.value.isEmpty())
    }

    @Test
    fun `retry after failure can succeed and clears error`() = runTest {
        coEvery { repository.getMoviesByGenre(28) } throws RuntimeException("network error")

        val viewModel = GenreViewModel(repository)
        viewModel.loadGenre(28)
        advanceUntilIdle()
        assertTrue(viewModel.hasError.value)

        val movies = listOf(sampleMovie(1))
        coEvery { repository.getMoviesByGenre(28) } returns movies

        viewModel.retry()
        advanceUntilIdle()

        assertFalse(viewModel.hasError.value)
        assertEquals(movies, viewModel.movies.value)
    }

    @Test
    fun `loadGenre with same id twice does not call repository again`() = runTest {
        val movies = listOf(sampleMovie(1))
        coEvery { repository.getMoviesByGenre(28) } returns movies

        val viewModel = GenreViewModel(repository)
        viewModel.loadGenre(28)
        advanceUntilIdle()
        viewModel.loadGenre(28)
        advanceUntilIdle()

        coVerify(exactly = 1) { repository.getMoviesByGenre(28) }
    }
}
