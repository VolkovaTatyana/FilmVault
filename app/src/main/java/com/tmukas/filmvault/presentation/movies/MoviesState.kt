package com.tmukas.filmvault.presentation.movies

import com.tmukas.filmvault.domain.model.Movie
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

sealed class MoviesState {
    data object Loading : MoviesState()

    data class Content(
        val movies: ImmutableList<Pair<String, ImmutableList<Movie>>> = persistentListOf(),
        val nextPageToLoad: Int = 1,
        val canLoadMore: Boolean = true
    ) : MoviesState()

    data class Refreshing(
        val movies: ImmutableList<Pair<String, ImmutableList<Movie>>> = persistentListOf(),
        val nextPageToLoad: Int = 1,
        val canLoadMore: Boolean = true
    ) : MoviesState()

    data class LoadingMore(
        val movies: ImmutableList<Pair<String, ImmutableList<Movie>>> = persistentListOf(),
        val nextPageToLoad: Int = 1,
        val canLoadMore: Boolean = true
    ) : MoviesState()

    data class Error(
        val message: String,
        val movies: ImmutableList<Pair<String, ImmutableList<Movie>>> = persistentListOf(),
        val nextPageToLoad: Int = 1,
        val canLoadMore: Boolean = true
    ) : MoviesState()
}