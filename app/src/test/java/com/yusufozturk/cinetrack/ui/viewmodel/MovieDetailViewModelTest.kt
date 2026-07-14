package com.yusufozturk.cinetrack.ui.viewmodel

import com.yusufozturk.cinetrack.MainDispatcherRule
import com.yusufozturk.cinetrack.data.model.CastMember
import com.yusufozturk.cinetrack.data.model.Movie
import com.yusufozturk.cinetrack.domain.usecase.GetMovieDetailUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class MovieDetailViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var getMovieDetailUseCase: GetMovieDetailUseCase

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

    private fun result(
        runtimeText: String? = "2s 15dk",
        trailerKey: String? = "abc123",
        cast: List<CastMember> = listOf(mockk(relaxed = true)),
        similarMovies: List<Movie> = listOf(movie(1, "Similar 1"))
    ) = GetMovieDetailUseCase.Result(
        runtimeText = runtimeText,
        trailerKey = trailerKey,
        cast = cast,
        similarMovies = similarMovies
    )

    @Before
    fun setup() {
        getMovieDetailUseCase = mockk()
    }

    private fun createViewModel() = MovieDetailViewModel(getMovieDetailUseCase)

    @Test
    fun `loadDetails basarili olunca tum alanlar dolar`() = runTest {
        coEvery { getMovieDetailUseCase(1) } returns result(
            runtimeText = "2s 15dk",
            trailerKey = "abc123",
            similarMovies = listOf(movie(1, "Similar 1"), movie(2, "Similar 2"))
        )

        val viewModel = createViewModel()
        viewModel.loadDetails(1)
        advanceUntilIdle()

        assertEquals("2s 15dk", viewModel.runtimeText.value)
        assertEquals("abc123", viewModel.trailerKey.value)
        assertEquals(1, viewModel.cast.value.size)
        assertEquals(2, viewModel.similarMovies.value.size)
        assertFalse(viewModel.isLoadingDetails.value)
    }

    @Test
    fun `loadDetails baslarken isLoadingDetails true olur`() = runTest {
        coEvery { getMovieDetailUseCase(1) } returns result()

        val viewModel = createViewModel()
        viewModel.loadDetails(1)

        assertTrue(viewModel.isLoadingDetails.value)

        advanceUntilIdle()
        assertFalse(viewModel.isLoadingDetails.value)
    }

    @Test
    fun `ayni movieId ile tekrar cagirinca useCase tekrar cagrilmaz`() = runTest {
        coEvery { getMovieDetailUseCase(1) } returns result()

        val viewModel = createViewModel()
        viewModel.loadDetails(1)
        advanceUntilIdle()
        viewModel.loadDetails(1)
        advanceUntilIdle()

        coVerify(exactly = 1) { getMovieDetailUseCase(1) }
    }

    @Test
    fun `farkli movieId ile tekrar cagirinca useCase tekrar cagrilir ve state yenilenir`() = runTest {
        coEvery { getMovieDetailUseCase(1) } returns result(runtimeText = "1s 40dk", trailerKey = "first")
        coEvery { getMovieDetailUseCase(2) } returns result(runtimeText = "2s 05dk", trailerKey = "second")

        val viewModel = createViewModel()
        viewModel.loadDetails(1)
        advanceUntilIdle()
        assertEquals("first", viewModel.trailerKey.value)

        viewModel.loadDetails(2)
        advanceUntilIdle()

        assertEquals("second", viewModel.trailerKey.value)
        assertEquals("2s 05dk", viewModel.runtimeText.value)
        coVerify(exactly = 1) { getMovieDetailUseCase(1) }
        coVerify(exactly = 1) { getMovieDetailUseCase(2) }
    }

    @Test
    fun `useCase beklenmedik hata firlatirsa isLoadingDetails yine false olur`() = runTest {
        coEvery { getMovieDetailUseCase(1) } throws RuntimeException("unexpected")

        val viewModel = createViewModel()
        viewModel.loadDetails(1)
        advanceUntilIdle()

        assertFalse(viewModel.isLoadingDetails.value)
        assertNull(viewModel.trailerKey.value)
        assertTrue(viewModel.cast.value.isEmpty())
    }
}

