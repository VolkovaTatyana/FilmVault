package com.tmukas.filmvault.data.mapper

import com.tmukas.filmvault.data.local.entity.MovieEntity
import com.tmukas.filmvault.data.remote.dto.MovieDto
import com.tmukas.filmvault.domain.model.Movie

fun MovieDto.toDomain(): Movie {
    return Movie(
        id = id ?: 0,
        title = title.orEmpty(),
        overview = overview.orEmpty(),
        posterPath = posterPath.resolveImageUrl(POSTER_SIZE_DEFAULT),
        backdropPath = backdropPath.resolveImageUrl(BACKDROP_SIZE_DEFAULT),
        releaseDate = releaseDate.orEmpty(),
        voteAverage = voteAverage ?: 0.0,
        voteCount = voteCount ?: 0,
        popularity = popularity ?: 0.0,
        genreIds = genreIds.orEmpty(),
        originalLanguage = originalLanguage.orEmpty(),
        isAdult = adult ?: false
    )
}

fun List<MovieDto>.toDomainFromDto(): List<Movie> {
    return this.map { it.toDomain() }
}

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

fun Movie.toEntity(isFavorite: Boolean, pageIndex: Int): MovieEntity =
    MovieEntity(
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
        isFavorite = isFavorite,
        pageIndex = pageIndex
    )

fun List<Movie>.toEntity(favoriteIds: Set<Int>, pageIndex: Int): List<MovieEntity> =
    map { movie ->
        movie.toEntity(
            isFavorite = favoriteIds.contains(movie.id),
            pageIndex = pageIndex
        )
    }

private fun String?.resolveImageUrl(size: String): String? {
    if (this.isNullOrBlank()) return null
    if (startsWith(URL_SCHEME_HTTP_PREFIX, ignoreCase = true)) return this
    val clean = if (startsWith("/")) drop(1) else this
    return "$IMAGE_BASE_URL$size/$clean"
}

private const val POSTER_SIZE_DEFAULT: String = "w342"
private const val BACKDROP_SIZE_DEFAULT: String = "w780"
private const val IMAGE_BASE_URL: String = "https://image.tmdb.org/t/p/"
private const val URL_SCHEME_HTTP_PREFIX = "http"
