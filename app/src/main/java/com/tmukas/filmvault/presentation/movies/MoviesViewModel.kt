package com.tmukas.filmvault.presentation.movies

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tmukas.filmvault.domain.model.Movie
import com.tmukas.filmvault.domain.usecase.ObserveMoviesUseCase
import com.tmukas.filmvault.domain.usecase.RefreshMoviesUseCase
import com.tmukas.filmvault.domain.usecase.RequestMoviesPageUseCase
import com.tmukas.filmvault.domain.usecase.ToggleFavoriteUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class MoviesViewModel @Inject constructor(
    private val observeMoviesUseCase: ObserveMoviesUseCase,
    private val requestMoviesPage: RequestMoviesPageUseCase,
    private val toggleFavorite: ToggleFavoriteUseCase,
    private val refreshMovies: RefreshMoviesUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(MoviesState())
    val uiState: StateFlow<MoviesState> = _uiState.asStateFlow()

    init {
        observeMovies()
        // Trigger first page load so DB gets populated for inspection
        loadNextPage()
    }

    private fun observeMovies() {
        viewModelScope.launch {
            observeMoviesUseCase().collect { movies ->
                _uiState.value = _uiState.value.copy(movies = movies.toUiMovieList())
            }
        }
    }

    fun loadNextPage() {
        viewModelScope.launch {
            val state = _uiState.value
            if (!state.canLoadMore || state.isLoadingInitial || state.isLoadingMore || state.isRefreshing) return@launch
            val isFirstPage = state.nextPageToLoad == 1
            _uiState.value = state.copy(
                isLoadingInitial = isFirstPage,
                isLoadingMore = !isFirstPage,
                error = null
            )
            val page = state.nextPageToLoad
            try {
                requestMoviesPage(page).getOrThrow()
                _uiState.value = _uiState.value.copy(
                    isLoadingInitial = false,
                    isLoadingMore = false,
                    nextPageToLoad = page + 1
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoadingInitial = false,
                    isLoadingMore = false,
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

    fun refresh() {
        viewModelScope.launch {
            val state = _uiState.value
            if (state.isRefreshing || state.isLoadingInitial || state.isLoadingMore) return@launch
            _uiState.value = state.copy(isRefreshing = true, error = null)
            try {
                refreshMovies().getOrThrow()
                _uiState.value = _uiState.value.copy(
                    isRefreshing = false,
                    nextPageToLoad = 2,
                    canLoadMore = true
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isRefreshing = false,
                    error = e.message ?: "Failed to refresh"
                )
            }
        }
    }

    private fun List<Movie>.toUiMovieList(): ImmutableList<Pair<String, ImmutableList<Movie>>> {
        val input = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val output = SimpleDateFormat("MMMM yyyy", Locale.ENGLISH)
        val map = LinkedHashMap<String, MutableList<Movie>>()
        for (m in this) {
            val label = try {
                val date = input.parse(m.releaseDate)
                if (date != null) {
                    val s = output.format(date)
                    s.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ENGLISH) else it.toString() }
                } else {
                    "Unknown"
                }
            } catch (_: Exception) {
                "Unknown"
            }
            map.getOrPut(label) { mutableListOf() }.add(m)
        }
        return map.entries.map { it.key to it.value.toPersistentList() }.toPersistentList()
    }
}
