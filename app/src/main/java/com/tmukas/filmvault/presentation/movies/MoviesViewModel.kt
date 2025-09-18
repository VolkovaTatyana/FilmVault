package com.tmukas.filmvault.presentation.movies

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tmukas.filmvault.domain.model.Movie
import com.tmukas.filmvault.domain.usecase.ObserveMoviesUseCase
import com.tmukas.filmvault.domain.usecase.PageResult
import com.tmukas.filmvault.domain.usecase.RequestMoviesPageUseCase
import com.tmukas.filmvault.domain.usecase.RefreshMoviesUseCase
import com.tmukas.filmvault.domain.usecase.ToggleFavoriteUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
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

    private val _uiState = MutableStateFlow<MoviesState>(MoviesState.Loading)
    val uiState: StateFlow<MoviesState> = _uiState.asStateFlow()

    // Store the last pagination result to use in observeMovies
    private var lastPageResult: PageResult? = null

    init {
        observeMovies()
        // Trigger first page load so DB gets populated for inspection
        loadNextPage()
    }

    private fun observeMovies() {
        viewModelScope.launch {
            observeMoviesUseCase().collect { movies ->
                val formattedMovies = movies.toUiMovieList()
                _uiState.value = when (val currentState = _uiState.value) {
                    is MoviesState.Loading -> MoviesState.Content(
                        movies = formattedMovies,
                        nextPageToLoad = 2,
                        canLoadMore = lastPageResult?.hasMorePages ?: true
                    )
                    is MoviesState.Content -> currentState.copy(movies = formattedMovies)
                    is MoviesState.Refreshing -> currentState.copy(movies = formattedMovies)
                    is MoviesState.LoadingMore -> MoviesState.Content(
                        movies = formattedMovies,
                        nextPageToLoad = currentState.nextPageToLoad + 1,
                        canLoadMore = lastPageResult?.hasMorePages ?: true
                    )
                    is MoviesState.Error -> currentState.copy(movies = formattedMovies)
                }
            }
        }
    }

    fun loadNextPage() {
        viewModelScope.launch {
            val currentState = _uiState.value
            val (canLoadMore, nextPageToLoad, movies) = when (currentState) {
                is MoviesState.Loading -> Triple(true, 1, persistentListOf())
                is MoviesState.Content -> Triple(
                    currentState.canLoadMore,
                    currentState.nextPageToLoad,
                    currentState.movies
                )

                is MoviesState.Refreshing -> return@launch
                is MoviesState.LoadingMore -> return@launch
                is MoviesState.Error -> Triple(
                    currentState.canLoadMore,
                    currentState.nextPageToLoad,
                    currentState.movies
                )
            }

            if (!canLoadMore) return@launch

            val isFirstPage = nextPageToLoad == 1
            _uiState.value = if (isFirstPage) {
                MoviesState.Loading
            } else {
                MoviesState.LoadingMore(
                    movies = movies,
                    nextPageToLoad = nextPageToLoad,
                    canLoadMore = canLoadMore
                )
            }

            try {
                val result = requestMoviesPage(nextPageToLoad).getOrThrow()
                lastPageResult = result
                // State will be updated in observeMovies() with correct canLoadMore
            } catch (e: Exception) {
                _uiState.value = MoviesState.Error(
                    message = e.message ?: "Unknown error",
                    movies = movies,
                    nextPageToLoad = nextPageToLoad,
                    canLoadMore = canLoadMore
                )
            }
        }
    }

    fun onClickFavorite(movie: Movie) {
        viewModelScope.launch {
            try {
                toggleFavorite(movie)
            } catch (e: Exception) {
                val currentState = _uiState.value
                val (movies, nextPageToLoad, canLoadMore) = when (currentState) {
                    is MoviesState.Content -> Triple(
                        currentState.movies,
                        currentState.nextPageToLoad,
                        currentState.canLoadMore
                    )

                    is MoviesState.Refreshing -> Triple(
                        currentState.movies,
                        currentState.nextPageToLoad,
                        currentState.canLoadMore
                    )

                    is MoviesState.LoadingMore -> Triple(
                        currentState.movies,
                        currentState.nextPageToLoad,
                        currentState.canLoadMore
                    )

                    is MoviesState.Error -> Triple(
                        currentState.movies,
                        currentState.nextPageToLoad,
                        currentState.canLoadMore
                    )

                    is MoviesState.Loading -> Triple(persistentListOf(), 1, true)
                }

                _uiState.value = MoviesState.Error(
                    message = e.message ?: "Failed to toggle favorite",
                    movies = movies,
                    nextPageToLoad = nextPageToLoad,
                    canLoadMore = canLoadMore
                )
            }
        }
    }

    fun refresh() {
        viewModelScope.launch {
            val currentState = _uiState.value
            if (currentState is MoviesState.Refreshing || currentState is MoviesState.Loading) return@launch

            val (movies, nextPageToLoad, canLoadMore) = when (currentState) {
                is MoviesState.Content -> Triple(
                    currentState.movies,
                    currentState.nextPageToLoad,
                    currentState.canLoadMore
                )

                is MoviesState.LoadingMore -> Triple(
                    currentState.movies,
                    currentState.nextPageToLoad,
                    currentState.canLoadMore
                )

                is MoviesState.Error -> Triple(
                    currentState.movies,
                    currentState.nextPageToLoad,
                    currentState.canLoadMore
                )
                else -> error("Unexpected state: $currentState")
            }

            _uiState.value = MoviesState.Refreshing(
                movies = movies,
                nextPageToLoad = nextPageToLoad,
                canLoadMore = canLoadMore
            )

            try {
                refreshMovies().getOrThrow()
                // Reset pagination info after refresh
                lastPageResult = null
                _uiState.value = MoviesState.Content(
                    movies = movies,
                    nextPageToLoad = 2,
                    canLoadMore = true // We assume there are more pages after refresh
                )
            } catch (e: Exception) {
                _uiState.value = MoviesState.Error(
                    message = e.message ?: "Failed to refresh",
                    movies = movies,
                    nextPageToLoad = nextPageToLoad,
                    canLoadMore = canLoadMore
                )
            }
        }
    }

    private fun List<Movie>.toUiMovieList(): ImmutableList<Pair<String, ImmutableList<Movie>>> {
        val input = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val output = SimpleDateFormat("MMMM yyyy", Locale.ENGLISH)

        // Movies are already sorted by release date DESC from the database query
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
