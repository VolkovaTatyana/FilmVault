package com.tmukas.filmvault.data.mapper

import com.tmukas.filmvault.data.local.entity.MovieEntity
import com.tmukas.filmvault.data.remote.dto.MovieDto
import com.tmukas.filmvault.domain.model.Movie

fun MovieEntity.toDomain(): Movie = Movie(
    id = id,
    title = title,
    overview = overview,
    posterPath = posterPath,
    backdropPath = backdropPath,
    releaseDate = releaseDate,
    voteAverage = voteAverage,
    voteCount = voteCount,
    popularity = popularity,
    genreIds = genreIds,
    originalLanguage = originalLanguage,
    isAdult = isAdult,
    isFavorite = isFavorite
)

fun List<MovieEntity>.toDomain(): List<Movie> = map { it.toDomain() }

fun MovieDto.toEntity(isFavorite: Boolean, pageIndex: Int): MovieEntity =
    MovieEntity(
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
        isAdult = adult ?: false,
        pageIndex = pageIndex,
        isFavorite = isFavorite
    )

fun List<MovieDto>.toEntities(favoriteIds: Set<Int>, pageIndex: Int): List<MovieEntity> =
    map { dto ->
        dto.toEntity(
            isFavorite = dto.id?.let { favoriteIds.contains(it) } ?: false,
            pageIndex = pageIndex
        )
    }