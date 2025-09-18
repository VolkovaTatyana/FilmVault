package com.tmukas.filmvault.domain.model

import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isNull
import assertk.assertions.isTrue
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

@DisplayName("Movie Domain Model - Data Integrity Tests")
class MovieTest {

    @Nested
    @DisplayName("Movie Creation")
    inner class MovieCreation {

        @Test
        @DisplayName("Should create movie with correct properties")
        fun `movie should be created with correct properties`() {
            // Given
            val movie = Movie(
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
                isFavorite = true
            )

            // Then
            assertThat(movie.id).isEqualTo(1)
            assertThat(movie.title).isEqualTo("Test Movie")
            assertThat(movie.overview).isEqualTo("Test overview")
            assertThat(movie.posterPath).isEqualTo("/test-poster.jpg")
            assertThat(movie.backdropPath).isEqualTo("/test-backdrop.jpg")
            assertThat(movie.releaseDate).isEqualTo("2024-01-01")
            assertThat(movie.voteAverage).isEqualTo(7.5)
            assertThat(movie.voteCount).isEqualTo(100)
            assertThat(movie.popularity).isEqualTo(85.5)
            assertThat(movie.genreIds).containsExactly(28, 12)
            assertThat(movie.originalLanguage).isEqualTo("en")
            assertThat(movie.isAdult).isFalse()
            assertThat(movie.isFavorite).isTrue()
        }

        @Test
        @DisplayName("Should create movie with default favorite status false")
        fun `movie should have default favorite status false`() {
            // Given
            val movie = Movie(
                id = 1,
                title = "Test Movie",
                overview = "Test overview",
                posterPath = null,
                backdropPath = null,
                releaseDate = "2024-01-01",
                voteAverage = 7.5,
                voteCount = 100,
                popularity = 85.5,
                genreIds = emptyList(),
                originalLanguage = "en",
                isAdult = false
            )

            // Then
            assertThat(movie.isFavorite).isFalse()
            assertThat(movie.posterPath).isNull()
            assertThat(movie.backdropPath).isNull()
        }
    }

    @Nested
    @DisplayName("Movie Copy Operations")
    inner class MovieCopyOperations {

        @Test
        @DisplayName("Should copy movie with changed favorite status")
        fun `movie copy should work correctly`() {
            // Given
            val originalMovie = Movie(
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

            // When
            val favoriteMovie = originalMovie.copy(isFavorite = true)

            // Then
            assertThat(favoriteMovie.isFavorite).isTrue()
            assertThat(favoriteMovie.id).isEqualTo(originalMovie.id)
            assertThat(favoriteMovie.title).isEqualTo(originalMovie.title)
            assertThat(originalMovie.isFavorite).isFalse() // Original should remain unchanged
        }
    }
}