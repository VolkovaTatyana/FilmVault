package com.tmukas.filmvault.di

import com.tmukas.filmvault.domain.repository.MovieRepository
import com.tmukas.filmvault.domain.usecase.ObserveFavoritesUseCase
import com.tmukas.filmvault.domain.usecase.ObserveMoviesUseCase
import com.tmukas.filmvault.domain.usecase.RequestMoviesPageUseCase
import com.tmukas.filmvault.domain.usecase.ToggleFavoriteUseCase
import com.tmukas.filmvault.domain.usecase.RefreshMoviesUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object UseCaseModule {

    @Provides
    @Singleton
    fun provideObserveMoviesUseCase(repository: MovieRepository) = ObserveMoviesUseCase(repository)

    @Provides
    @Singleton
    fun provideObserveFavoritesUseCase(repository: MovieRepository) =
        ObserveFavoritesUseCase(repository)

    @Provides
    @Singleton
    fun provideRequestMoviesPageUseCase(repository: MovieRepository) =
        RequestMoviesPageUseCase(repository)

    @Provides
    @Singleton
    fun provideToggleFavoriteUseCase(repository: MovieRepository) =
        ToggleFavoriteUseCase(repository)

    @Provides
    @Singleton
    fun provideRefreshMoviesUseCase(repository: MovieRepository) =
        RefreshMoviesUseCase(repository)
}