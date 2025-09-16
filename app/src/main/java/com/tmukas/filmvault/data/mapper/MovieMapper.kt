package com.tmukas.filmvault.data.mapper

import com.tmukas.filmvault.data.remote.dto.MovieDto
import com.tmukas.filmvault.domain.model.Movie

fun MovieDto.toDomain(): Movie {
    return Movie(
        id = id ?: 0,
        title = title.orEmpty(),
        overview = overview.orEmpty(),
        posterPath = posterPath,
        backdropPath = backdropPath,
        releaseDate = releaseDate.orEmpty(),
        voteAverage = voteAverage ?: 0.0,
        voteCount = voteCount ?: 0,
        popularity = popularity ?: 0.0,
        genreIds = genreIds.orEmpty(),
        originalLanguage = originalLanguage.orEmpty(),
        isAdult = adult ?: false
    )
}

fun List<MovieDto>.toDomain(): List<Movie> {
    return this.map { it.toDomain() }
}