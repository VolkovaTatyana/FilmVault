package com.tmukas.filmvault.domain.usecase

import com.tmukas.filmvault.domain.model.Movie
import com.tmukas.filmvault.domain.repository.MovieRepository

class ToggleFavoriteUseCase(
    private val repository: MovieRepository
) {
    suspend operator fun invoke(movie: Movie) {
        if (movie.isFavorite) {
            repository.removeFromFavorites(movie.id)
        } else {
            repository.addToFavorites(movie.id)
        }
    }
}