package com.tmukas.filmvault.domain.usecase

import com.tmukas.filmvault.domain.repository.MovieRepository

class RefreshMoviesUseCase(
    private val repository: MovieRepository
) {
    suspend operator fun invoke(): Result<Unit> {
        return repository.refreshMovies()
    }
}