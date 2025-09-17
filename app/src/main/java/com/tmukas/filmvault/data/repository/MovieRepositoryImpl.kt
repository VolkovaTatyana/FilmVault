package com.tmukas.filmvault.data.repository

import com.tmukas.filmvault.BuildConfig
import com.tmukas.filmvault.data.local.MovieDao
import com.tmukas.filmvault.data.mapper.toDomain
import com.tmukas.filmvault.data.mapper.toDomainFromDto
import com.tmukas.filmvault.data.mapper.toEntity
import com.tmukas.filmvault.data.remote.api.MovieApiService
import com.tmukas.filmvault.domain.model.Movie
import com.tmukas.filmvault.domain.repository.MovieRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlin.coroutines.coroutineContext
import javax.inject.Inject

class MovieRepositoryImpl @Inject constructor(
    private val movieDao: MovieDao,
    private val apiService: MovieApiService
) : MovieRepository {

    override fun observeMovies(): Flow<List<Movie>> =
        movieDao.observeMovies().map { it.toDomain() }

    override fun observeFavorites(): Flow<List<Movie>> =
        movieDao.observeFavorites().map { it.toDomain() }

    override suspend fun requestNextPage(page: Int): Result<Unit> {
        return try {
            coroutineContext.ensureActive()
            val favoriteIds = movieDao.getFavoriteIds().toSet()
            val response = apiService.discoverMovies(apiKey = BuildConfig.API_KEY, page = page)
            val entities = response.results.orEmpty()
                .toDomainFromDto()
                .toEntity(favoriteIds, pageIndex = page)
            if (entities.isNotEmpty()) {
                movieDao.upsertAll(entities)
            }
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun refreshMovies(): Result<Unit> {
        return try {
            coroutineContext.ensureActive()
            val favoriteIds = movieDao.getFavoriteIds().toSet()
            movieDao.clearAll()
            val response = apiService.discoverMovies(apiKey = BuildConfig.API_KEY, page = 1)
            val entities = response.results.orEmpty()
                .toDomainFromDto()
                .toEntity(favoriteIds, pageIndex = 1)
            if (entities.isNotEmpty()) movieDao.upsertAll(entities)
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun addToFavorites(movieId: Int) {
        movieDao.addToFavorites(movieId)
    }

    override suspend fun removeFromFavorites(movieId: Int) {
        movieDao.removeFromFavorites(movieId)
    }
}