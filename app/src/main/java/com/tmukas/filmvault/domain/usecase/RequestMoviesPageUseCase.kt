package com.tmukas.filmvault.domain.usecase

import com.tmukas.filmvault.domain.repository.MovieRepository

data class PageResult(
    val hasMorePages: Boolean
)

class RequestMoviesPageUseCase(
    private val repository: MovieRepository
) {
    suspend operator fun invoke(page: Int): Result<PageResult> {
        return repository.requestNextPage(page)
    }
}