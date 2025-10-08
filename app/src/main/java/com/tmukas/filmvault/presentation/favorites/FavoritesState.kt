package com.tmukas.filmvault.presentation.favorites

import com.tmukas.filmvault.domain.model.Movie
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

sealed class FavoritesState {
    data object Loading : FavoritesState()

    data object Empty : FavoritesState()

    data class Content(
        val favorites: ImmutableList<Movie> = persistentListOf()
    ) : FavoritesState()

    data class Error(
        val message: String,
        val favorites: ImmutableList<Movie> = persistentListOf()
    ) : FavoritesState()
}