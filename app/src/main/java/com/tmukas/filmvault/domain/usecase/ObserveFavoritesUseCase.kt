package com.tmukas.filmvault.domain.usecase

import com.tmukas.filmvault.domain.model.Movie
import com.tmukas.filmvault.domain.repository.MovieRepository
import kotlinx.coroutines.flow.Flow

class ObserveFavoritesUseCase(
    private val repository: MovieRepository
) {
    operator fun invoke(): Flow<List<Movie>> {
        return repository.observeFavorites()
    }
}