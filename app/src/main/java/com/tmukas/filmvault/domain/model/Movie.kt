package com.tmukas.filmvault.domain.model

data class Movie(
    val id: Int,
    val title: String,
    val overview: String,
    val posterPath: String?,
    val backdropPath: String?,
    val releaseDate: String,
    val voteAverage: Double,
    val voteCount: Int,
    val popularity: Double,
    val genreIds: List<Int>,
    val originalLanguage: String,
    val isAdult: Boolean,
    val isFavorite: Boolean = false
)