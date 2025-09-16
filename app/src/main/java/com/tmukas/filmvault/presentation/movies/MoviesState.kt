package com.tmukas.filmvault.presentation.movies

import com.tmukas.filmvault.domain.model.Movie

data class MoviesState(
    val movies: List<Movie> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)