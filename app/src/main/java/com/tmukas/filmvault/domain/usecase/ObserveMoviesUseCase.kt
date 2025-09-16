package com.tmukas.filmvault.domain.usecase

import com.tmukas.filmvault.domain.model.Movie
import com.tmukas.filmvault.domain.repository.MovieRepository
import kotlinx.coroutines.flow.Flow

class ObserveMoviesUseCase(
    private val repository: MovieRepository
) {
    operator fun invoke(): Flow<List<Movie>> = repository.observeMovies()
}