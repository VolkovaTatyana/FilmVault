package com.tmukas.filmvault.presentation.movies

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tmukas.filmvault.domain.model.Movie
import com.tmukas.filmvault.domain.usecase.ObserveMoviesUseCase
import com.tmukas.filmvault.domain.usecase.RequestMoviesPageUseCase
import com.tmukas.filmvault.domain.usecase.ToggleFavoriteUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class MoviesViewModel @Inject constructor(
    private val observeMoviesUseCase: ObserveMoviesUseCase,
    private val requestMoviesPage: RequestMoviesPageUseCase,
    private val toggleFavorite: ToggleFavoriteUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(MoviesState())
    val uiState: StateFlow<MoviesState> = _uiState.asStateFlow()

    init {
        observeMovies()
        // Trigger first page load so DB gets populated for inspection
        loadNextPage(page = 1)
    }

    private fun observeMovies() {
        viewModelScope.launch {
            observeMoviesUseCase().collect { movies ->
                _uiState.value = _uiState.value.copy(movies = movies)
            }
        }
    }

    fun loadNextPage(page: Int) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                error = null
            )

            try {
                requestMoviesPage(page).getOrThrow()
                _uiState.value = _uiState.value.copy(isLoading = false)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Unknown error"
                )
            }
        }
    }

    fun onClickFavorite(movie: Movie) {
        viewModelScope.launch {
            try {
                toggleFavorite(movie)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Failed to toggle favorite"
                )
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}
