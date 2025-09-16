package com.tmukas.filmvault.domain.usecase

import com.tmukas.filmvault.domain.repository.MovieRepository

class RequestMoviesPageUseCase(
    private val repository: MovieRepository
) {
    suspend operator fun invoke(page: Int): Result<Unit> {
        return repository.requestNextPage(page)
    }
}