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
import org.junit.jupiter.api.Test

@DisplayName("RequestMoviesPageUseCase - Pagination Logic Tests")
class RequestMoviesPageUseCaseTest {

    private lateinit var repository: MovieRepository
    private lateinit var requestMoviesPageUseCase: RequestMoviesPageUseCase

    @BeforeEach
    fun setup() {
        repository = mockk()
        requestMoviesPageUseCase = RequestMoviesPageUseCase(repository)
    }

    @Test
    @DisplayName("Should return success with more pages available")
    fun shouldReturnSuccessWithMorePagesAvailable() = runTest {
        // Given
        val page = 1
        val expectedResult = Result.success(PageResult(hasMorePages = true))
        coEvery { repository.requestNextPage(page) } returns expectedResult

        // When
        val result = requestMoviesPageUseCase(page)

        // Then
        assertThat(result.isSuccess).isTrue()
        assertThat(result.getOrNull()).isNotNull()
        assertThat(result.getOrNull()!!.hasMorePages).isTrue()
        coVerify(exactly = 1) { repository.requestNextPage(page) }
    }

    @Test
    @DisplayName("Should return success with no more pages")
    fun shouldReturnSuccessWithNoMorePages() = runTest {
        // Given
        val lastPage = 500
        val expectedResult = Result.success(PageResult(hasMorePages = false))
        coEvery { repository.requestNextPage(lastPage) } returns expectedResult

        // When
        val result = requestMoviesPageUseCase(lastPage)

        // Then
        assertThat(result.isSuccess).isTrue()
        assertThat(result.getOrNull()!!.hasMorePages).isFalse()
        coVerify(exactly = 1) { repository.requestNextPage(lastPage) }
    }

    @Test
    @DisplayName("Should return failure when repository fails")
    fun shouldReturnFailureWhenRepositoryFails() = runTest {
        // Given
        val page = 1
        val exception = Exception("Network timeout")
        val expectedResult = Result.failure<PageResult>(exception)
        coEvery { repository.requestNextPage(page) } returns expectedResult

        // When
        val result = requestMoviesPageUseCase(page)

        // Then
        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()?.message).isEqualTo("Network timeout")
        coVerify(exactly = 1) { repository.requestNextPage(page) }
    }

    @Test
    @DisplayName("Should handle different page numbers correctly")
    fun shouldHandleDifferentPageNumbers() = runTest {
        // Given
        val testCases = mapOf(
            1 to true,      // First page - more pages available
            2 to true,      // Middle page - more pages available  
            10 to true,     // Another middle page
            100 to false    // Last page - no more pages
        )

        // Setup mocks for all test cases
        testCases.forEach { (page, hasMore) ->
            val expectedResult = Result.success(PageResult(hasMorePages = hasMore))
            coEvery { repository.requestNextPage(page) } returns expectedResult
        }

        // When & Then
        testCases.forEach { (page, expectedHasMore) ->
            val result = requestMoviesPageUseCase(page)

            assertThat(result.isSuccess).isTrue()
            assertThat(result.getOrNull()?.hasMorePages).isEqualTo(expectedHasMore)
        }

        // Verify all calls were made
        testCases.keys.forEach { page ->
            coVerify(exactly = 1) { repository.requestNextPage(page) }
        }
    }

    @Test
    @DisplayName("Should handle invalid page numbers gracefully")
    fun shouldHandleInvalidPageNumbers() = runTest {
        // Given
        val invalidPages = listOf(0, -1, -100)

        invalidPages.forEach { invalidPage ->
            val exception = IllegalArgumentException("Invalid page number: $invalidPage")
            val expectedResult = Result.failure<PageResult>(exception)
            coEvery { repository.requestNextPage(invalidPage) } returns expectedResult
        }

        // When & Then
        invalidPages.forEach { invalidPage ->
            val result = requestMoviesPageUseCase(invalidPage)

            assertThat(result.isFailure).isTrue()
            assertThat(result.exceptionOrNull()).isNotNull()
            assertThat(result.exceptionOrNull()?.message).isEqualTo("Invalid page number: $invalidPage")
        }
    }
}