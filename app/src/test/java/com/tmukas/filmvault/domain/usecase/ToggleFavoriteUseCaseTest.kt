package com.tmukas.filmvault.domain.usecase

import assertk.assertThat
import assertk.assertions.isEqualTo
import com.tmukas.filmvault.domain.model.Movie
import com.tmukas.filmvault.domain.repository.MovieRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@DisplayName("ToggleFavoriteUseCase - Core Business Logic Tests")
class ToggleFavoriteUseCaseTest {

    private lateinit var repository: MovieRepository
    private lateinit var toggleFavoriteUseCase: ToggleFavoriteUseCase

    @BeforeEach
    fun setup() {
        repository = mockk()
        toggleFavoriteUseCase = ToggleFavoriteUseCase(repository)
    }

    @Test
    @DisplayName("Should add movie to favorites when movie is not favorite")
    fun shouldAddToFavoritesWhenNotFavorite() = runTest {
        // Given
        val movie = createTestMovie(isFavorite = false)
        coEvery { repository.addToFavorites(movie.id) } returns Unit

        // When
        toggleFavoriteUseCase(movie)

        // Then - Verify correct repository method called
        coVerify(exactly = 1) { repository.addToFavorites(movie.id) }
        coVerify(exactly = 0) { repository.removeFromFavorites(any()) }

        // Assert the business logic decision
        assertThat(movie.isFavorite).isEqualTo(false) // Input condition
    }

    @Test
    @DisplayName("Should remove movie from favorites when movie is favorite")
    fun shouldRemoveFromFavoritesWhenFavorite() = runTest {
        // Given
        val movie = createTestMovie(isFavorite = true)
        coEvery { repository.removeFromFavorites(movie.id) } returns Unit

        // When
        toggleFavoriteUseCase(movie)

        // Then - Verify correct repository method called
        coVerify(exactly = 1) { repository.removeFromFavorites(movie.id) }
        coVerify(exactly = 0) { repository.addToFavorites(any()) }

        // Assert the business logic decision
        assertThat(movie.isFavorite).isEqualTo(true) // Input condition
    }

    private fun createTestMovie(
        id: Int = 1,
        isFavorite: Boolean
    ) = Movie(
        id = id,
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
        isFavorite = isFavorite
    )
}