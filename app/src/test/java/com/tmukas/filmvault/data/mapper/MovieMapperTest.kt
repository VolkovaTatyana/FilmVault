package com.tmukas.filmvault.data.mapper

import assertk.assertThat
import assertk.assertions.*
import com.tmukas.filmvault.data.local.entity.MovieEntity
import com.tmukas.filmvault.data.remote.dto.MovieDto
import com.tmukas.filmvault.domain.model.Movie
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

@DisplayName("Movie Mapper - Data Transformation")
class MovieMapperTest {

    @Nested
    @DisplayName("DTO to Domain Mapping")
    inner class DtoToDomainMapping {

        @Test
        @DisplayName("Should map complete DTO to domain correctly")
        fun shouldMapCompleteDtoToDomainCorrectly() {
            // Given
            val dto = MovieDto(
                id = 123,
                title = "Test Movie",
                originalTitle = "Original Test Movie",
                overview = "This is a test movie overview",
                posterPath = "/poster.jpg",
                backdropPath = "/backdrop.jpg",
                releaseDate = "2024-01-15",
                adult = false,
                genreIds = listOf(28, 12, 16),
                originalLanguage = "en",
                popularity = 95.5,
                voteAverage = 8.7,
                voteCount = 1500,
                video = false
            )

            // When
            val domain = dto.toDomain()

            // Then
            assertThat(domain.id).isEqualTo(123)
            assertThat(domain.title).isEqualTo("Test Movie")
            assertThat(domain.overview).isEqualTo("This is a test movie overview")
            assertThat(domain.posterPath).isEqualTo("https://image.tmdb.org/t/p/w342/poster.jpg")
            assertThat(domain.backdropPath).isEqualTo("https://image.tmdb.org/t/p/w780/backdrop.jpg")
            assertThat(domain.releaseDate).isEqualTo("2024-01-15")
            assertThat(domain.voteAverage).isEqualTo(8.7)
            assertThat(domain.voteCount).isEqualTo(1500)
            assertThat(domain.popularity).isEqualTo(95.5)
            assertThat(domain.genreIds).containsExactly(*dto.genreIds!!.toTypedArray())
            assertThat(domain.originalLanguage).isEqualTo("en")
            assertThat(domain.isAdult).isFalse()
            assertThat(domain.isFavorite).isFalse() // Default value
        }

        @Test
        @DisplayName("Should handle null DTO values gracefully")
        fun shouldHandleNullDtoValuesGracefully() {
            // Given
            val dtoWithNulls = MovieDto(
                id = null,
                title = null,
                originalTitle = null,
                overview = null,
                posterPath = null,
                backdropPath = null,
                releaseDate = null,
                adult = null,
                genreIds = null,
                originalLanguage = null,
                popularity = null,
                voteAverage = null,
                voteCount = null,
                video = null
            )

            // When
            val domain = dtoWithNulls.toDomain()

            // Then
            assertThat(domain.id).isEqualTo(0) // Default fallback
            assertThat(domain.title).isEqualTo("")
            assertThat(domain.overview).isEqualTo("")
            assertThat(domain.posterPath).isNull()
            assertThat(domain.backdropPath).isNull()
            assertThat(domain.releaseDate).isEqualTo("")
            assertThat(domain.voteAverage).isEqualTo(0.0)
            assertThat(domain.voteCount).isEqualTo(0)
            assertThat(domain.popularity).isEqualTo(0.0)
            assertThat(domain.genreIds).isEmpty()
            assertThat(domain.originalLanguage).isEqualTo("")
            assertThat(domain.isAdult).isFalse()
        }

        @Test
        @DisplayName("Should handle empty DTO strings correctly")
        fun shouldHandleEmptyDtoStringsCorrectly() {
            // Given
            val dtoWithEmptyStrings = MovieDto(
                id = 456,
                title = "",
                originalTitle = "",
                overview = "",
                posterPath = "",
                backdropPath = "",
                releaseDate = "",
                adult = false,
                genreIds = emptyList(),
                originalLanguage = "",
                popularity = 0.0,
                voteAverage = 0.0,
                voteCount = 0,
                video = false
            )

            // When
            val domain = dtoWithEmptyStrings.toDomain()

            // Then
            assertThat(domain.id).isEqualTo(456)
            assertThat(domain.title).isEqualTo("")
            assertThat(domain.overview).isEqualTo("")
            assertThat(domain.posterPath).isNull() // Empty string becomes null after processing
            assertThat(domain.backdropPath).isNull() // Empty string becomes null after processing
            assertThat(domain.releaseDate).isEqualTo("")
            assertThat(domain.genreIds).isEmpty()
            assertThat(domain.originalLanguage).isEqualTo("")
        }
    }

    @Nested
    @DisplayName("Domain to Entity Mapping")
    inner class DomainToEntityMapping {

        @Test
        @DisplayName("Should map domain to entity with favorite status")
        fun shouldMapDomainToEntityWithFavoriteStatus() {
            // Given
            val domainMovies = listOf(
                Movie(
                    id = 1,
                    title = "Movie 1",
                    overview = "Overview 1",
                    posterPath = "/poster1.jpg",
                    backdropPath = "/backdrop1.jpg",
                    releaseDate = "2024-01-01",
                    voteAverage = 7.5,
                    voteCount = 100,
                    popularity = 85.5,
                    genreIds = listOf(28),
                    originalLanguage = "en",
                    isAdult = false,
                    isFavorite = false
                ),
                Movie(
                    id = 2,
                    title = "Movie 2",
                    overview = "Overview 2",
                    posterPath = "/poster2.jpg",
                    backdropPath = "/backdrop2.jpg",
                    releaseDate = "2024-02-01",
                    voteAverage = 8.0,
                    voteCount = 200,
                    popularity = 90.0,
                    genreIds = listOf(12, 16),
                    originalLanguage = "es",
                    isAdult = false,
                    isFavorite = false
                )
            )
            val favoriteIds = setOf(2) // Only movie 2 is favorite
            val pageIndex = 1

            // When
            val entities = domainMovies.toEntity(favoriteIds, pageIndex)

            // Then
            assertThat(entities).hasSize(2)

            val entity1 = entities.first { it.id == 1 }
            assertThat(entity1.isFavorite).isFalse()
            assertThat(entity1.pageIndex).isEqualTo(1)
            assertThat(entity1.title).isEqualTo("Movie 1")

            val entity2 = entities.first { it.id == 2 }
            assertThat(entity2.isFavorite).isTrue() // Should be marked as favorite
            assertThat(entity2.pageIndex).isEqualTo(1)
            assertThat(entity2.title).isEqualTo("Movie 2")
        }

        @Test
        @DisplayName("Should preserve all movie data during mapping")
        fun shouldPreserveAllMovieDataDuringMapping() {
            // Given
            val domainMovie = Movie(
                id = 123,
                title = "Complete Movie",
                overview = "Complete Overview",
                posterPath = "/complete_poster.jpg",
                backdropPath = "/complete_backdrop.jpg",
                releaseDate = "2024-03-15",
                voteAverage = 9.2,
                voteCount = 5000,
                popularity = 98.7,
                genreIds = listOf(28, 12, 16, 35),
                originalLanguage = "fr",
                isAdult = true,
                isFavorite = true
            )

            // When
            val entity = listOf(domainMovie).toEntity(emptySet(), 3).first()

            // Then
            assertThat(entity.id).isEqualTo(123)
            assertThat(entity.title).isEqualTo("Complete Movie")
            assertThat(entity.overview).isEqualTo("Complete Overview")
            assertThat(entity.posterPath).isEqualTo("/complete_poster.jpg")
            assertThat(entity.backdropPath).isEqualTo("/complete_backdrop.jpg")
            assertThat(entity.releaseDate).isEqualTo("2024-03-15")
            assertThat(entity.voteAverage).isEqualTo(9.2)
            assertThat(entity.voteCount).isEqualTo(5000)
            assertThat(entity.popularity).isEqualTo(98.7)
            assertThat(entity.genreIds).containsExactly(*domainMovie.genreIds.toTypedArray())
            assertThat(entity.originalLanguage).isEqualTo("fr")
            assertThat(entity.isAdult).isTrue()
            assertThat(entity.pageIndex).isEqualTo(3)
            assertThat(entity.isFavorite).isFalse() // Should be false since not in favoriteIds
        }

        @Test
        @DisplayName("Should handle empty domain list")
        fun shouldHandleEmptyDomainList() {
            // Given
            val emptyList = emptyList<Movie>()
            val favoriteIds = setOf<Int>()
            val pageIndex = 1

            // When
            val entities = emptyList.toEntity(favoriteIds, pageIndex)

            // Then
            assertThat(entities).isEmpty()
        }
    }

    @Nested
    @DisplayName("Entity to Domain Mapping")
    inner class EntityToDomainMapping {

        @Test
        @DisplayName("Should map entity list to domain correctly")
        fun shouldMapEntityListToDomainCorrectly() {
            // Given
            val entities = listOf(
                MovieEntity(
                    id = 1,
                    title = "Entity Movie 1",
                    overview = "Entity Overview 1",
                    posterPath = "/entity_poster1.jpg",
                    backdropPath = "/entity_backdrop1.jpg",
                    releaseDate = "2024-04-01",
                    voteAverage = 6.5,
                    voteCount = 50,
                    popularity = 75.0,
                    genreIds = listOf(28),
                    originalLanguage = "en",
                    isAdult = false,
                    isFavorite = true,
                    pageIndex = 1
                ),
                MovieEntity(
                    id = 2,
                    title = "Entity Movie 2",
                    overview = "Entity Overview 2",
                    posterPath = "/entity_poster2.jpg",
                    backdropPath = "/entity_backdrop2.jpg",
                    releaseDate = "2024-05-01",
                    voteAverage = 7.8,
                    voteCount = 150,
                    popularity = 88.3,
                    genreIds = listOf(12, 35),
                    originalLanguage = "de",
                    isAdult = true,
                    isFavorite = false,
                    pageIndex = 2
                )
            )

            // When
            val domainMovies = entities.toDomain()

            // Then
            assertThat(domainMovies).hasSize(2)

            val domain1 = domainMovies.first { it.id == 1 }
            assertThat(domain1.title).isEqualTo("Entity Movie 1")
            assertThat(domain1.isFavorite).isTrue()
            assertThat(domain1.genreIds).containsExactly(*entities[0].genreIds.toTypedArray())

            val domain2 = domainMovies.first { it.id == 2 }
            assertThat(domain2.title).isEqualTo("Entity Movie 2")
            assertThat(domain2.isFavorite).isFalse()
            assertThat(domain2.isAdult).isTrue()
            assertThat(domain2.originalLanguage).isEqualTo("de")
            assertThat(domain2.genreIds).containsExactly(*entities[1].genreIds.toTypedArray())
        }

        @Test
        @DisplayName("Should handle empty entity list")
        fun shouldHandleEmptyEntityList() {
            // Given
            val emptyEntities = emptyList<MovieEntity>()

            // When
            val domainMovies = emptyEntities.toDomain()

            // Then
            assertThat(domainMovies).isEmpty()
        }

        @Test
        @DisplayName("Should preserve all entity fields in domain")
        fun shouldPreserveAllEntityFieldsInDomain() {
            // Given
            val entity = MovieEntity(
                id = 999,
                title = "Preservation Test Movie",
                overview = "Testing field preservation",
                posterPath = "/preservation_poster.jpg",
                backdropPath = "/preservation_backdrop.jpg",
                releaseDate = "2024-12-31",
                voteAverage = 10.0,
                voteCount = 10000,
                popularity = 100.0,
                genreIds = listOf(1, 2, 3, 4, 5),
                originalLanguage = "ja",
                isAdult = false,
                isFavorite = true,
                pageIndex = 10
            )

            // When
            val domain = listOf(entity).toDomain().first()

            // Then
            assertThat(domain.id).isEqualTo(999)
            assertThat(domain.title).isEqualTo("Preservation Test Movie")
            assertThat(domain.overview).isEqualTo("Testing field preservation")
            assertThat(domain.posterPath).isEqualTo("/preservation_poster.jpg")
            assertThat(domain.backdropPath).isEqualTo("/preservation_backdrop.jpg")
            assertThat(domain.releaseDate).isEqualTo("2024-12-31")
            assertThat(domain.voteAverage).isEqualTo(10.0)
            assertThat(domain.voteCount).isEqualTo(10000)
            assertThat(domain.popularity).isEqualTo(100.0)
            assertThat(domain.genreIds).containsExactly(*entity.genreIds.toTypedArray())
            assertThat(domain.originalLanguage).isEqualTo("ja")
            assertThat(domain.isAdult).isFalse()
            assertThat(domain.isFavorite).isTrue()
            // Note: pageIndex is not mapped to domain as it's repository-specific
        }
    }

    @Nested
    @DisplayName("Chain Mapping Consistency")
    inner class ChainMappingConsistency {

        @Test
        @DisplayName("Should maintain data consistency through DTO -> Domain -> Entity chain")
        fun shouldMaintainDataConsistencyThroughChain() {
            // Given
            val originalDto = MovieDto(
                id = 777,
                title = "Chain Test Movie",
                originalTitle = "Original Chain Test Movie",
                overview = "Testing mapping chain consistency",
                posterPath = "/chain_poster.jpg",
                backdropPath = "/chain_backdrop.jpg",
                releaseDate = "2024-06-15",
                adult = true,
                genreIds = listOf(18, 53),
                originalLanguage = "it",
                popularity = 67.8,
                voteAverage = 7.2,
                voteCount = 890,
                video = false
            )

            // When - Chain: DTO -> Domain -> Entity -> Domain
            val domainFromDto = originalDto.toDomain()
            val entityFromDomain = listOf(domainFromDto).toEntity(setOf(777), 5).first()
            val finalDomain = listOf(entityFromDomain).toDomain().first()

            // Then - Key fields should be preserved
            assertThat(finalDomain.id).isEqualTo(originalDto.id)
            assertThat(finalDomain.title).isEqualTo(originalDto.title)
            assertThat(finalDomain.overview).isEqualTo(originalDto.overview)
            assertThat(finalDomain.posterPath).isEqualTo("https://image.tmdb.org/t/p/w342/chain_poster.jpg")
            assertThat(finalDomain.backdropPath).isEqualTo("https://image.tmdb.org/t/p/w780/chain_backdrop.jpg")
            assertThat(finalDomain.releaseDate).isEqualTo(originalDto.releaseDate)
            assertThat(finalDomain.voteAverage).isEqualTo(originalDto.voteAverage)
            assertThat(finalDomain.voteCount).isEqualTo(originalDto.voteCount)
            assertThat(finalDomain.popularity).isEqualTo(originalDto.popularity)
            assertThat(finalDomain.genreIds).containsExactly(*originalDto.genreIds!!.toTypedArray())
            assertThat(finalDomain.originalLanguage).isEqualTo(originalDto.originalLanguage)
            assertThat(finalDomain.isAdult).isEqualTo(originalDto.adult)
            assertThat(finalDomain.isFavorite).isTrue() // Should be true because 777 is in favoriteIds
        }
    }
}