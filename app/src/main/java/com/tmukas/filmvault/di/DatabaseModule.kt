package com.tmukas.filmvault.di

import android.content.Context
import androidx.room.Room
import com.tmukas.filmvault.data.local.FilmVaultDatabase
import com.tmukas.filmvault.data.local.MovieDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): FilmVaultDatabase =
        Room.databaseBuilder(
            context = context,
            klass = FilmVaultDatabase::class.java,
            name = "film_vault.db"
        )
            // NOTE: Allowed only because this is a test task; for production,
            // add proper migrations instead of destructive fallback.
            .fallbackToDestructiveMigration(true)
            .build()

    @Provides
    @Singleton
    fun provideMovieDao(db: FilmVaultDatabase): MovieDao = db.movieDao()
}