package com.tmukas.filmvault.presentation.favorites

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tmukas.filmvault.domain.model.Movie
import com.tmukas.filmvault.domain.usecase.ObserveFavoritesUseCase
import com.tmukas.filmvault.domain.usecase.ToggleFavoriteUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class FavoritesViewModel @Inject constructor(
    private val observeFavoritesUseCase: ObserveFavoritesUseCase,
    private val toggleFavorite: ToggleFavoriteUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<FavoritesState>(FavoritesState.Loading)
    val uiState: StateFlow<FavoritesState> = _uiState.asStateFlow()

    init {
        observeFavorites()
    }

    private fun observeFavorites() {
        viewModelScope.launch {
            observeFavoritesUseCase().collect { favorites ->
                val immutableFavorites = favorites.toImmutableList()
                _uiState.value = when {
                    immutableFavorites.isEmpty() -> FavoritesState.Empty
                    else -> FavoritesState.Content(favorites = immutableFavorites)
                }
            }
        }
    }

    fun onClickFavorite(movie: Movie) {
        viewModelScope.launch {
            try {
                toggleFavorite(movie)
            } catch (e: Exception) {
                val currentState = _uiState.value
                val favorites = when (currentState) {
                    is FavoritesState.Content -> currentState.favorites
                    is FavoritesState.Error -> currentState.favorites
                    is FavoritesState.Loading -> persistentListOf()
                    is FavoritesState.Empty -> persistentListOf()
                }

                _uiState.value = FavoritesState.Error(
                    message = e.message ?: "Failed to toggle favorite",
                    favorites = favorites
                )
            }
        }
    }
}