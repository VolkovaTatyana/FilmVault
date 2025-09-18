package com.tmukas.filmvault.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.tmukas.filmvault.data.local.entity.MovieEntity
import com.tmukas.filmvault.data.local.MovieDao
import com.tmukas.filmvault.data.local.Converters

@Database(
    entities = [MovieEntity::class],
    version = 2,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class FilmVaultDatabase : RoomDatabase() {
    abstract fun movieDao(): MovieDao
}