package com.tmukas.filmvault.presentation.favorites

import com.tmukas.filmvault.domain.model.Movie
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

data class FavoritesState(
    val favorites: ImmutableList<Movie> = persistentListOf(),
    val isEmpty: Boolean = true
)