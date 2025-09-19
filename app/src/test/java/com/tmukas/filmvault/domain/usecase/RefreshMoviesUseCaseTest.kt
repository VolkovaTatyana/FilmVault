package com.tmukas.filmvault.domain.usecase

import assertk.assertThat
import assertk.assertions.*
import com.tmukas.filmvault.domain.repository.MovieRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.io.IOException
import java.net.UnknownHostException

@DisplayName("RefreshMoviesUseCase - Data Refresh Logic")
class RefreshMoviesUseCaseTest {

    private lateinit var repository: MovieRepository
    private lateinit var refreshMoviesUseCase: RefreshMoviesUseCase

    @BeforeEach
    fun setup() {
        repository = mockk()
        refreshMoviesUseCase = RefreshMoviesUseCase(repository)
    }

    @Nested
    @DisplayName("Successful Refresh")
    inner class SuccessfulRefresh {

        @Test
        @DisplayName("Should successfully refresh movies")
        fun shouldSuccessfullyRefreshMovies() = runTest {
            // Given
            val expectedResult = Result.success(Unit)
            coEvery { repository.refreshMovies() } returns expectedResult

            // When
            val result = refreshMoviesUseCase()

            // Then
            assertThat(result).isSuccess()
            coVerify(exactly = 1) { repository.refreshMovies() }
        }

        @Test
        @DisplayName("Should return Unit on successful refresh")
        fun shouldReturnUnitOnSuccessfulRefresh() = runTest {
            // Given
            val expectedResult = Result.success(Unit)
            coEvery { repository.refreshMovies() } returns expectedResult

            // When
            val result = refreshMoviesUseCase()

            // Then
            result.onSuccess { value ->
                assertThat(value).isEqualTo(Unit)
            }
        }
    }

    @Nested
    @DisplayName("Network Error Handling")
    inner class NetworkErrorHandling {

        @Test
        @DisplayName("Should handle network timeout error")
        fun shouldHandleNetworkTimeoutError() = runTest {
            // Given
            val exception = java.net.SocketTimeoutException("Connection timeout")
            val expectedResult = Result.failure<Unit>(exception)
            coEvery { repository.refreshMovies() } returns expectedResult

            // When
            val result = refreshMoviesUseCase()

            // Then
            assertThat(result).isFailure()
            result.onFailure { error ->
                assertThat(error).isInstanceOf(java.net.SocketTimeoutException::class.java)
                assertThat(error.message).isEqualTo("Connection timeout")
            }
            coVerify(exactly = 1) { repository.refreshMovies() }
        }

        @Test
        @DisplayName("Should handle unknown host error")
        fun shouldHandleUnknownHostError() = runTest {
            // Given
            val exception = UnknownHostException("Unable to resolve host")
            val expectedResult = Result.failure<Unit>(exception)
            coEvery { repository.refreshMovies() } returns expectedResult

            // When
            val result = refreshMoviesUseCase()

            // Then
            assertThat(result).isFailure()
            result.onFailure { error ->
                assertThat(error).isInstanceOf(UnknownHostException::class.java)
                assertThat(error.message).isEqualTo("Unable to resolve host")
            }
        }

        @Test
        @DisplayName("Should handle generic IO exception")
        fun shouldHandleGenericIoException() = runTest {
            // Given
            val exception = IOException("Network error occurred")
            val expectedResult = Result.failure<Unit>(exception)
            coEvery { repository.refreshMovies() } returns expectedResult

            // When
            val result = refreshMoviesUseCase()

            // Then
            assertThat(result).isFailure()
            result.onFailure { error ->
                assertThat(error).isInstanceOf(IOException::class.java)
                assertThat(error.message).isEqualTo("Network error occurred")
            }
        }

        @Test
        @DisplayName("Should handle null error message gracefully")
        fun shouldHandleNullErrorMessageGracefully() = runTest {
            // Given
            val exception = IOException() // No message
            val expectedResult = Result.failure<Unit>(exception)
            coEvery { repository.refreshMovies() } returns expectedResult

            // When
            val result = refreshMoviesUseCase()

            // Then
            assertThat(result).isFailure()
            result.onFailure { error ->
                assertThat(error).isInstanceOf(IOException::class.java)
                assertThat(error.message).isNull()
            }
        }
    }

    @Nested
    @DisplayName("Use Case Contract")
    inner class UseCaseContract {

        @Test
        @DisplayName("Should delegate to repository refreshMovies method")
        fun shouldDelegateToRepositoryRefreshMoviesMethod() = runTest {
            // Given
            val expectedResult = Result.success(Unit)
            coEvery { repository.refreshMovies() } returns expectedResult

            // When
            refreshMoviesUseCase()

            // Then
            coVerify(exactly = 1) { repository.refreshMovies() }
        }

        @Test
        @DisplayName("Should return exactly what repository returns")
        fun shouldReturnExactlyWhatRepositoryReturns() = runTest {
            // Given - Success case
            val successResult = Result.success(Unit)
            coEvery { repository.refreshMovies() } returns successResult

            // When
            val result1 = refreshMoviesUseCase()

            // Then
            assertThat(result1).isEqualTo(successResult)

            // Given - Failure case
            val failureException = RuntimeException("Repository error")
            val failureResult = Result.failure<Unit>(failureException)
            coEvery { repository.refreshMovies() } returns failureResult

            // When
            val result2 = refreshMoviesUseCase()

            // Then
            assertThat(result2).isFailure()
            result2.onFailure { error ->
                assertThat(error).isEqualTo(failureException)
            }
        }

        @Test
        @DisplayName("Should not modify repository result")
        fun shouldNotModifyRepositoryResult() = runTest {
            // Given
            val originalException = IllegalStateException("Original error")
            val repositoryResult = Result.failure<Unit>(originalException)
            coEvery { repository.refreshMovies() } returns repositoryResult

            // When
            val useCaseResult = refreshMoviesUseCase()

            // Then - Should be the exact same result
            assertThat(useCaseResult).isFailure()
            assertThat(useCaseResult).isEqualTo(repositoryResult)
            useCaseResult.onFailure { error ->
                assertThat(error).isEqualTo(originalException)
                assertThat(error.message).isEqualTo("Original error")
            }
        }
    }

    @Nested
    @DisplayName("Multiple Invocations")
    inner class MultipleInvocations {

        @Test
        @DisplayName("Should handle multiple successful invocations")
        fun shouldHandleMultipleSuccessfulInvocations() = runTest {
            // Given
            val successResult = Result.success(Unit)
            coEvery { repository.refreshMovies() } returns successResult

            // When
            val result1 = refreshMoviesUseCase()
            val result2 = refreshMoviesUseCase()
            val result3 = refreshMoviesUseCase()

            // Then
            assertThat(result1).isSuccess()
            assertThat(result2).isSuccess()
            assertThat(result3).isSuccess()
            coVerify(exactly = 3) { repository.refreshMovies() }
        }

        @Test
        @DisplayName("Should handle mixed success and failure invocations")
        fun shouldHandleMixedSuccessAndFailureInvocations() = runTest {
            // Given - First call succeeds, second fails, third succeeds
            val successResult = Result.success(Unit)
            val failureResult = Result.failure<Unit>(IOException("Network error"))

            coEvery { repository.refreshMovies() } returnsMany listOf(
                successResult,
                failureResult,
                successResult
            )

            // When
            val result1 = refreshMoviesUseCase()
            val result2 = refreshMoviesUseCase()
            val result3 = refreshMoviesUseCase()

            // Then
            assertThat(result1).isSuccess()
            assertThat(result2).isFailure()
            assertThat(result3).isSuccess()
            coVerify(exactly = 3) { repository.refreshMovies() }
        }
    }
}