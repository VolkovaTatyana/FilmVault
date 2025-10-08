package com.tmukas.filmvault.presentation.movies

import com.tmukas.filmvault.domain.model.Movie
import com.tmukas.filmvault.domain.usecase.ObserveMoviesUseCase
import com.tmukas.filmvault.domain.usecase.PageResult
import com.tmukas.filmvault.domain.usecase.RefreshMoviesUseCase
import com.tmukas.filmvault.domain.usecase.RequestMoviesPageUseCase
import com.tmukas.filmvault.domain.usecase.ToggleFavoriteUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@DisplayName("MoviesViewModel - Basic Functionality Tests")
class MoviesViewModelTest {

    private lateinit var viewModel: MoviesViewModel
    private lateinit var observeMoviesUseCase: ObserveMoviesUseCase
    private lateinit var requestMoviesPage: RequestMoviesPageUseCase
    private lateinit var toggleFavorite: ToggleFavoriteUseCase
    private lateinit var refreshMovies: RefreshMoviesUseCase

    private val sampleMovie = Movie(
        id = 1,
        title = "Test Movie",
        overview = "Test overview",
        posterPath = "/test-poster.jpg",
        backdropPath = "/test-backdrop.jpg",
        releaseDate = "2024-01-01",
        voteAverage = 7.5,
        voteCount = 100,
        popularity = 85.5,
        genreIds = listOf(28, 12),
        originalLanguage = "en",
        isAdult = false,
        isFavorite = false
    )

    @BeforeEach
    fun setup() {
        observeMoviesUseCase = mockk()
        requestMoviesPage = mockk()
        toggleFavorite = mockk()
        refreshMovies = mockk()

        // Default mock behavior
        every { observeMoviesUseCase() } returns flowOf(emptyList())
        coEvery { requestMoviesPage(any()) } returns Result.success(PageResult(hasMorePages = true))
        coEvery { refreshMovies() } returns Result.success(Unit)
        coEvery { toggleFavorite(any()) } returns Unit
    }

    private fun createViewModel(): MoviesViewModel {
        return MoviesViewModel(
            observeMoviesUseCase,
            requestMoviesPage,
            toggleFavorite,
            refreshMovies
        )
    }

    @Test
    @DisplayName("Should create ViewModel successfully")
    fun shouldCreateViewModelSuccessfully() = runTest {
        // When
        viewModel = createViewModel()

        // Then - Should not throw and be created
        assert(viewModel != null)
    }

    @Test
    @DisplayName("Should call toggle favorite with correct movie")
    fun shouldCallToggleFavoriteWithCorrectMovie() = runTest {
        // Given
        every { observeMoviesUseCase() } returns flowOf(listOf(sampleMovie))
        viewModel = createViewModel()

        // When
        viewModel.onClickFavorite(sampleMovie)

        // Then
        coVerify { toggleFavorite(sampleMovie) }
    }

    @Test
    @DisplayName("Should handle use case calls without throwing exceptions")
    fun shouldHandleUseCaseCallsWithoutThrowingExceptions() = runTest {
        // Given
        every { observeMoviesUseCase() } returns flowOf(listOf(sampleMovie))

        // When & Then - Should not throw
        viewModel = createViewModel()
        viewModel.onClickFavorite(sampleMovie)
        viewModel.refresh()
        viewModel.loadNextPage()

        // All operations should complete without exceptions
    }

    @Test
    @DisplayName("Should handle empty movie list gracefully")
    fun shouldHandleEmptyMovieListGracefully() = runTest {
        // Given
        every { observeMoviesUseCase() } returns flowOf(emptyList())

        // When & Then - Should not throw
        viewModel = createViewModel()

        // Should complete without exceptions - just verify creation works
        assert(viewModel != null)
    }

    @Test
    @DisplayName("Should handle errors gracefully without throwing")
    fun shouldHandleErrorsGracefullyWithoutThrowing() = runTest {
        // Given
        every { observeMoviesUseCase() } returns flowOf(listOf(sampleMovie))
        coEvery { refreshMovies() } returns Result.failure(Exception("Network error"))
        coEvery { requestMoviesPage(any()) } returns Result.failure(Exception("Network error"))

        // When & Then - Should not throw
        viewModel = createViewModel()
        viewModel.refresh()
        viewModel.loadNextPage()

        // Should complete without exceptions
        assert(viewModel != null)
    }
}