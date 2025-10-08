package com.tmukas.filmvault.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.tmukas.filmvault.data.local.entity.MovieEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MovieDao {

    @Query("SELECT * FROM movies ORDER BY releaseDate DESC")
    fun observeMovies(): Flow<List<MovieEntity>>

    @Query("SELECT * FROM movies WHERE isFavorite = 1 ORDER BY releaseDate DESC")
    fun observeFavorites(): Flow<List<MovieEntity>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun upsertAll(entities: List<MovieEntity>)

    @Query("UPDATE movies SET isFavorite = 1 WHERE id = :movieId")
    suspend fun addToFavorites(movieId: Int)

    @Query("UPDATE movies SET isFavorite = 0 WHERE id = :movieId")
    suspend fun removeFromFavorites(movieId: Int)

    @Query("DELETE FROM movies")
    suspend fun clearAll()

    @Query("SELECT id FROM movies WHERE isFavorite = 1")
    suspend fun getFavoriteIds(): List<Int>

    @Query("DELETE FROM movies WHERE isFavorite = 0 AND id NOT IN (:keepIds)")
    suspend fun deleteAllExcept(keepIds: List<Int>)
}