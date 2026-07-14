package com.yusufozturk.cinetrack.ui.viewmodel

import android.app.Application
import com.yusufozturk.cinetrack.MainDispatcherRule
import com.yusufozturk.cinetrack.data.local.WatchedPreferences
import com.yusufozturk.cinetrack.data.model.Movie
import com.yusufozturk.cinetrack.data.repository.AuthRepository
import com.yusufozturk.cinetrack.domain.usecase.GetRatedMoviesUseCase
import com.yusufozturk.cinetrack.domain.usecase.RateMovieUseCase
import com.yusufozturk.cinetrack.domain.usecase.ToggleWatchlistUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
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
class MainViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var authRepository: AuthRepository
    private lateinit var watchedPreferences: WatchedPreferences
    private lateinit var toggleWatchlistUseCase: ToggleWatchlistUseCase
    private lateinit var rateMovieUseCase: RateMovieUseCase
    private lateinit var getRatedMoviesUseCase: GetRatedMoviesUseCase
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
        authRepository = mockk()
        watchedPreferences = mockk(relaxed = true)
        toggleWatchlistUseCase = mockk()
        rateMovieUseCase = mockk()
        getRatedMoviesUseCase = mockk()
        application = mockk(relaxed = true)

        // init bloğu her testte tetiklendiği için varsayılan davranışları burada tanımlıyoruz
        every { authRepository.isLoggedIn() } returns false
        every { watchedPreferences.getWatchedIds() } returns emptySet()
    }

    private fun createViewModel() = MainViewModel(
        authRepository,
        watchedPreferences,
        toggleWatchlistUseCase,
        rateMovieUseCase,
        getRatedMoviesUseCase,
        application
    )

    @Test
    fun `giris yapilmamis durumda init sirasinda watchlist ve ratings cekilmez`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        assertFalse(viewModel.isLoggedIn.value)
        assertTrue(viewModel.watchlist.value.isEmpty())
        coVerify(exactly = 0) { authRepository.getWatchlist() }
    }

    @Test
    fun `giris yapilmis durumda init sirasinda watchlist ve ratings otomatik cekilir`() = runTest {
        every { authRepository.isLoggedIn() } returns true
        coEvery { authRepository.getWatchlist() } returns listOf(movie(1, "Batman"))
        coEvery { getRatedMoviesUseCase() } returns mapOf(1 to 4)

        val viewModel = createViewModel()
        advanceUntilIdle()

        assertTrue(viewModel.isLoggedIn.value)
        assertEquals(1, viewModel.watchlist.value.size)
        assertEquals(4, viewModel.ratings.value[1])
    }

    @Test
    fun `requestLogin login ekranini acar ve hatayi temizler`() {
        val viewModel = createViewModel()

        viewModel.requestLogin()

        assertTrue(viewModel.showLoginScreen.value)
        assertNull(viewModel.loginError.value)
    }

    @Test
    fun `dismissLogin login ekranini kapatir`() {
        val viewModel = createViewModel()

        viewModel.requestLogin()
        viewModel.dismissLogin()

        assertFalse(viewModel.showLoginScreen.value)
    }

    @Test
    fun `login basarili olunca isLoggedIn true olur ve watchlist yenilenir`() = runTest {
        coEvery { authRepository.loginWithCredentials("user", "pass") } returns Unit
        coEvery { authRepository.getWatchlist() } returns listOf(movie(1, "Batman"))
        coEvery { getRatedMoviesUseCase() } returns emptyMap()

        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.login("user", "pass")
        advanceUntilIdle()

        assertTrue(viewModel.isLoggedIn.value)
        assertFalse(viewModel.showLoginScreen.value)
        assertFalse(viewModel.isLoggingIn.value)
        assertEquals(1, viewModel.watchlist.value.size)
    }

    @Test
    fun `login basarisiz olunca loginError set edilir`() = runTest {
        coEvery { authRepository.loginWithCredentials("user", "wrong") } throws RuntimeException("401")

        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.login("user", "wrong")
        advanceUntilIdle()

        assertFalse(viewModel.isLoggedIn.value)
        assertEquals("Login failed. Check your username and password.", viewModel.loginError.value)
        assertFalse(viewModel.isLoggingIn.value)
    }

    @Test
    fun `logout state i temizler`() = runTest {
        every { authRepository.isLoggedIn() } returns true
        coEvery { authRepository.getWatchlist() } returns listOf(movie(1, "Batman"))
        coEvery { getRatedMoviesUseCase() } returns mapOf(1 to 5)
        coEvery { authRepository.logout() } returns Unit

        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.logout()
        advanceUntilIdle()

        assertFalse(viewModel.isLoggedIn.value)
        assertTrue(viewModel.watchlist.value.isEmpty())
        assertTrue(viewModel.ratings.value.isEmpty())
    }

    @Test
    fun `refreshWatchlist hata verince hasWatchlistError true olur`() = runTest {
        coEvery { authRepository.getWatchlist() } throws RuntimeException("network")

        val viewModel = createViewModel()
        viewModel.refreshWatchlist()
        advanceUntilIdle()

        assertTrue(viewModel.hasWatchlistError.value)
        assertFalse(viewModel.isLoadingWatchlist.value)
    }

    @Test
    fun `toggleWatchlist basarili olunca watchlist yenilenir`() = runTest {
        val batman = movie(1, "Batman")
        coEvery { toggleWatchlistUseCase(batman, any()) } returns Unit
        coEvery { authRepository.getWatchlist() } returns listOf(batman)

        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.toggleWatchlist(batman)
        advanceUntilIdle()

        coVerify(exactly = 1) { toggleWatchlistUseCase(batman, any()) }
        assertEquals(1, viewModel.watchlist.value.size)
    }

    @Test
    fun `rateMovie basarili olunca ratings yenilenir`() = runTest {
        coEvery { rateMovieUseCase(1, 5) } returns Unit
        coEvery { getRatedMoviesUseCase() } returns mapOf(1 to 5)

        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.rateMovie(1, 5)
        advanceUntilIdle()

        coVerify(exactly = 1) { rateMovieUseCase(1, 5) }
        assertEquals(5, viewModel.ratings.value[1])
    }

    @Test
    fun `toggleWatched watchedIds i gunceller`() {
        every { watchedPreferences.getWatchedIds() } returnsMany listOf(emptySet(), setOf(1))

        val viewModel = createViewModel()
        viewModel.toggleWatched(1)

        assertEquals(setOf(1), viewModel.watchedIds.value)
        coVerify(exactly = 0) { authRepository.getWatchlist() }
    }
}
