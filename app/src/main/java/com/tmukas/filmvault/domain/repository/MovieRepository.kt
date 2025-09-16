package com.tmukas.filmvault.domain.repository

import com.tmukas.filmvault.domain.model.Movie
import kotlinx.coroutines.flow.Flow

interface MovieRepository {
    fun observeMovies(): Flow<List<Movie>>
    fun observeFavorites(): Flow<List<Movie>>
    suspend fun requestNextPage(page: Int): Result<Unit>
    suspend fun refreshMovies(): Result<Unit>

    suspend fun addToFavorites(movieId: Int)
    suspend fun removeFromFavorites(movieId: Int)
}