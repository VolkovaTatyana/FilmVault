package com.tmukas.filmvault.data.repository

import assertk.assertThat
import assertk.assertions.*
import com.tmukas.filmvault.data.local.MovieDao
import com.tmukas.filmvault.data.local.entity.MovieEntity
import com.tmukas.filmvault.data.mapper.toDomain
import com.tmukas.filmvault.data.mapper.toDomainFromDto
import com.tmukas.filmvault.data.mapper.toEntity
import com.tmukas.filmvault.data.remote.api.MovieApiService
import com.tmukas.filmvault.data.remote.dto.MovieDto
import com.tmukas.filmvault.data.remote.dto.MoviesResponseDto
import com.tmukas.filmvault.domain.model.Movie
import com.tmukas.filmvault.domain.usecase.PageResult
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.io.IOException
import java.net.UnknownHostException

@DisplayName("MovieRepositoryImpl - Integration Tests")
class MovieRepositoryImplTest {

    private lateinit var repository: MovieRepositoryImpl
    private lateinit var movieDao: MovieDao
    private lateinit var apiService: MovieApiService

    private val sampleMovieDto = MovieDto(
        id = 1,
        title = "Test Movie",
        originalTitle = "Test Movie Original",
        overview = "Test overview",
        posterPath = "/test-poster.jpg",
        backdropPath = "/test-backdrop.jpg",
        releaseDate = "2024-01-01",
        adult = false,
        genreIds = listOf(28, 12),
        originalLanguage = "en",
        popularity = 85.5,
        voteAverage = 7.5,
        voteCount = 100,
        video = false
    )

    private val sampleMovieEntity = MovieEntity(
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
        isFavorite = false,
        pageIndex = 1
    )

    @BeforeEach
    fun setup() {
        movieDao = mockk()
        apiService = mockk()
        repository = MovieRepositoryImpl(movieDao, apiService)
    }

    @Nested
    @DisplayName("Data Observation")
    inner class DataObservation {

        @Test
        @DisplayName("Should observe movies from DAO correctly")
        fun shouldObserveMoviesFromDao() = runTest {
            // Given
            val entities = listOf(sampleMovieEntity)
            every { movieDao.observeMovies() } returns flowOf(entities)

            // When
            val result = repository.observeMovies().first()

            // Then
            assertThat(result).hasSize(1)
            assertThat(result.first().id).isEqualTo(1)
            assertThat(result.first().title).isEqualTo("Test Movie")
        }

        @Test
        @DisplayName("Should observe favorites from DAO correctly")
        fun shouldObserveFavoritesFromDao() = runTest {
            // Given
            val favoriteEntity = sampleMovieEntity.copy(isFavorite = true)
            every { movieDao.observeFavorites() } returns flowOf(listOf(favoriteEntity))

            // When
            val result = repository.observeFavorites().first()

            // Then
            assertThat(result).hasSize(1)
            assertThat(result.first().isFavorite).isTrue()
        }

        @Test
        @DisplayName("Should handle empty data streams")
        fun shouldHandleEmptyDataStreams() = runTest {
            // Given
            every { movieDao.observeMovies() } returns flowOf(emptyList())
            every { movieDao.observeFavorites() } returns flowOf(emptyList())

            // When
            val moviesResult = repository.observeMovies().first()
            val favoritesResult = repository.observeFavorites().first()

            // Then
            assertThat(moviesResult).isEmpty()
            assertThat(favoritesResult).isEmpty()
        }
    }

    @Nested
    @DisplayName("Page Request Logic")
    inner class PageRequestLogic {

        @Test
        @DisplayName("Should successfully request first page")
        fun shouldSuccessfullyRequestFirstPage() = runTest {
            // Given
            val response = MoviesResponseDto(
                page = 1,
                results = listOf(sampleMovieDto),
                totalPages = 10,
                totalResults = 200
            )
            coEvery { movieDao.getFavoriteIds() } returns emptyList()
            coEvery { apiService.discoverMovies(any(), any()) } returns response
            coEvery { movieDao.upsertAll(any()) } returns Unit

            // When
            val result = repository.requestNextPage(1)

            // Then
            assertThat(result).isSuccess()
            result.onSuccess { pageResult ->
                assertThat(pageResult.hasMorePages).isTrue()
            }
            coVerify { movieDao.upsertAll(any()) }
        }

        @Test
        @DisplayName("Should handle last page correctly")
        fun shouldHandleLastPageCorrectly() = runTest {
            // Given
            val response = MoviesResponseDto(
                page = 10,
                results = listOf(sampleMovieDto),
                totalPages = 10,
                totalResults = 200
            )
            coEvery { movieDao.getFavoriteIds() } returns emptyList()
            coEvery { apiService.discoverMovies(any(), any()) } returns response
            coEvery { movieDao.upsertAll(any()) } returns Unit

            // When
            val result = repository.requestNextPage(10)

            // Then
            assertThat(result).isSuccess()
            result.onSuccess { pageResult ->
                assertThat(pageResult.hasMorePages).isFalse()
            }
        }

        @Test
        @DisplayName("Should preserve favorite status during page load")
        fun shouldPreserveFavoriteStatusDuringPageLoad() = runTest {
            // Given
            val favoriteIds = setOf(1)
            val response = MoviesResponseDto(
                page = 1,
                results = listOf(sampleMovieDto),
                totalPages = 10,
                totalResults = 200
            )
            coEvery { movieDao.getFavoriteIds() } returns listOf(1)
            coEvery { apiService.discoverMovies(any(), any()) } returns response
            coEvery { movieDao.upsertAll(any()) } returns Unit

            // When
            val result = repository.requestNextPage(1)

            // Then
            assertThat(result).isSuccess()
            coVerify {
                movieDao.upsertAll(match { entities ->
                    entities.any { it.id == 1 && it.isFavorite }
                })
            }
        }

        @Test
        @DisplayName("Should handle empty API response")
        fun shouldHandleEmptyApiResponse() = runTest {
            // Given
            val response = MoviesResponseDto(
                page = 1,
                results = emptyList(),
                totalPages = 1,
                totalResults = 0
            )
            coEvery { movieDao.getFavoriteIds() } returns emptyList()
            coEvery { apiService.discoverMovies(any(), any()) } returns response

            // When
            val result = repository.requestNextPage(1)

            // Then
            assertThat(result).isSuccess()
            result.onSuccess { pageResult ->
                assertThat(pageResult.hasMorePages).isFalse()
            }
            coVerify(exactly = 0) { movieDao.upsertAll(any()) }
        }
    }

    @Nested
    @DisplayName("Network Error Handling")
    inner class NetworkErrorHandling {

        @Test
        @DisplayName("Should handle network timeout gracefully")
        fun shouldHandleNetworkTimeoutGracefully() = runTest {
            // Given
            val exception = java.net.SocketTimeoutException("Connection timeout")
            coEvery { movieDao.getFavoriteIds() } returns emptyList()
            coEvery { apiService.discoverMovies(any(), any()) } throws exception

            // When
            val result = repository.requestNextPage(1)

            // Then
            assertThat(result).isFailure()
            result.onFailure { error ->
                assertThat(error).isInstanceOf(java.net.SocketTimeoutException::class.java)
            }
        }

        @Test
        @DisplayName("Should handle unknown host exception")
        fun shouldHandleUnknownHostException() = runTest {
            // Given
            val exception = UnknownHostException("Unable to resolve host")
            coEvery { movieDao.getFavoriteIds() } returns emptyList()
            coEvery { apiService.discoverMovies(any(), any()) } throws exception

            // When
            val result = repository.requestNextPage(1)

            // Then
            assertThat(result).isFailure()
            result.onFailure { error ->
                assertThat(error).isInstanceOf(UnknownHostException::class.java)
            }
        }

        @Test
        @DisplayName("Should handle generic IO exception")
        fun shouldHandleGenericIoException() = runTest {
            // Given
            val exception = IOException("Network error")
            coEvery { movieDao.getFavoriteIds() } returns emptyList()
            coEvery { apiService.discoverMovies(any(), any()) } throws exception

            // When
            val result = repository.requestNextPage(1)

            // Then
            assertThat(result).isFailure()
            result.onFailure { error ->
                assertThat(error).isInstanceOf(IOException::class.java)
            }
        }
    }

    @Nested
    @DisplayName("Refresh Logic")
    inner class RefreshLogic {

        @Test
        @DisplayName("Should refresh movies successfully")
        fun shouldRefreshMoviesSuccessfully() = runTest {
            // Given
            val response = MoviesResponseDto(
                page = 1,
                results = listOf(sampleMovieDto),
                totalPages = 10,
                totalResults = 200
            )
            val favoriteIds = listOf(999)
            coEvery { movieDao.getFavoriteIds() } returns favoriteIds
            coEvery { apiService.discoverMovies(any(), 1) } returns response
            coEvery { movieDao.deleteAllExcept(any()) } returns Unit
            coEvery { movieDao.upsertAll(any()) } returns Unit

            // When
            val result = repository.refreshMovies()

            // Then
            assertThat(result).isSuccess()
            coVerify { movieDao.deleteAllExcept(match { it.contains(1) && it.contains(999) }) }
            coVerify { movieDao.upsertAll(any()) }
        }

        @Test
        @DisplayName("Should handle refresh failure")
        fun shouldHandleRefreshFailure() = runTest {
            // Given
            val exception = IOException("Network error")
            coEvery { movieDao.getFavoriteIds() } returns emptyList()
            coEvery { apiService.discoverMovies(any(), 1) } throws exception

            // When
            val result = repository.refreshMovies()

            // Then
            assertThat(result).isFailure()
            result.onFailure { error ->
                assertThat(error).isInstanceOf(IOException::class.java)
            }
        }

        @Test
        @DisplayName("Should preserve favorites during refresh")
        fun shouldPreserveFavoritesDuringRefresh() = runTest {
            // Given
            val favoriteIds = listOf(100, 200)
            val response = MoviesResponseDto(
                page = 1,
                results = listOf(sampleMovieDto),
                totalPages = 1,
                totalResults = 1
            )
            coEvery { movieDao.getFavoriteIds() } returns favoriteIds
            coEvery { apiService.discoverMovies(any(), 1) } returns response
            coEvery { movieDao.deleteAllExcept(any()) } returns Unit
            coEvery { movieDao.upsertAll(any()) } returns Unit

            // When
            val result = repository.refreshMovies()

            // Then
            assertThat(result).isSuccess()
            coVerify {
                movieDao.deleteAllExcept(match { keepIds ->
                    keepIds.containsAll(favoriteIds)
                })
            }
        }
    }

    @Nested
    @DisplayName("Favorite Operations")
    inner class FavoriteOperations {

        @Test
        @DisplayName("Should add movie to favorites")
        fun shouldAddMovieToFavorites() = runTest {
            // Given
            val movieId = 123
            coEvery { movieDao.addToFavorites(movieId) } returns Unit

            // When
            repository.addToFavorites(movieId)

            // Then
            coVerify { movieDao.addToFavorites(123) }
        }

        @Test
        @DisplayName("Should remove movie from favorites")
        fun shouldRemoveMovieFromFavorites() = runTest {
            // Given
            val movieId = 123
            coEvery { movieDao.removeFromFavorites(movieId) } returns Unit

            // When
            repository.removeFromFavorites(movieId)

            // Then
            coVerify { movieDao.removeFromFavorites(123) }
        }
    }
}