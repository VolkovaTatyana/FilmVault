package com.tmukas.filmvault.presentation.movies

import com.tmukas.filmvault.domain.model.Movie
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

data class MoviesState(
    val movies: ImmutableList<Pair<String, ImmutableList<Movie>>> = persistentListOf(),
    val isLoadingInitial: Boolean = false,
    val isRefreshing: Boolean = false,
    val isLoadingMore: Boolean = false,
    val nextPageToLoad: Int = 1,
    val canLoadMore: Boolean = true,
    val error: String? = null
)