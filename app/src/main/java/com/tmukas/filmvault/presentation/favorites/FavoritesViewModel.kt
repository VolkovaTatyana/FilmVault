package com.tmukas.filmvault.presentation.favorites

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tmukas.filmvault.domain.model.Movie
import com.tmukas.filmvault.domain.usecase.ObserveFavoritesUseCase
import com.tmukas.filmvault.domain.usecase.ToggleFavoriteUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
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

    private val _uiState = MutableStateFlow(FavoritesState())
    val uiState: StateFlow<FavoritesState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            observeFavoritesUseCase().collect { list: List<Movie> ->
                _uiState.value = FavoritesState(
                    favorites = list.toImmutableList(),
                    isEmpty = list.isEmpty()
                )
            }
        }
    }

    fun onClickFavorite(movie: Movie) {
        viewModelScope.launch {
            toggleFavorite(movie)
        }
    }
}