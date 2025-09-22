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
import kotlinx.coroutines.delay
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

    // Block automatic loading after network errors
    private var isLoadingBlocked: Boolean = false

    init {
        observeMovies()
        // Trigger first page load
        loadNextPage()
    }

    private fun observeMovies() {
        viewModelScope.launch {
            observeMoviesUseCase().collect { movies ->
                val formattedMovies = movies.toUiMovieList()

                _uiState.value = when (val currentState = _uiState.value) {
                    is MoviesState.Loading -> {
                        val newState = MoviesState.Content(
                            movies = formattedMovies,
                            nextPageToLoad = if (formattedMovies.isEmpty()) 1 else 2,
                            canLoadMore = formattedMovies.isNotEmpty() && (lastPageResult?.hasMorePages
                                ?: true)
                        )
                        newState
                    }
                    is MoviesState.Content -> currentState.copy(movies = formattedMovies)
                    is MoviesState.Refreshing -> MoviesState.Content(
                        movies = formattedMovies,
                        nextPageToLoad = 2,
                        canLoadMore = lastPageResult?.hasMorePages ?: true
                    )
                    is MoviesState.LoadingMore -> {
                        // After successful loading, nextPageToLoad should be the page we just loaded + 1
                        // currentState.nextPageToLoad is the page we were loading
                        MoviesState.Content(
                            movies = formattedMovies,
                            nextPageToLoad = currentState.nextPageToLoad + 1,
                            canLoadMore = lastPageResult?.hasMorePages ?: true
                        )
                    }
                    is MoviesState.Error -> {
                        if (formattedMovies.isNotEmpty()) {
                            // We have cached data, show it as Content
                            // Don't transfer errorMessage to prevent infinite toast loop
                            MoviesState.Content(
                                movies = formattedMovies,
                                nextPageToLoad = currentState.nextPageToLoad,
                                canLoadMore = currentState.canLoadMore
                            )
                        } else {
                            // No cached data available, keep error state but update movies
                            currentState.copy(movies = formattedMovies)
                        }
                    }
                }
            }
        }
    }

    fun loadNextPage() {
        viewModelScope.launch {
            if (isLoadingBlocked) return@launch

            val currentState = _uiState.value
            val (movies, nextPageToLoad, canLoadMore) = when (currentState) {
                is MoviesState.Loading -> Triple(persistentListOf(), 1, true)
                is MoviesState.Refreshing -> return@launch
                is MoviesState.LoadingMore -> return@launch
                else -> extractStateData(currentState)
            }

            if (!canLoadMore) return@launch

            // Prevent loading first page if we already have data
            if (nextPageToLoad == 1 && movies.isNotEmpty()) {
                // Update state to reflect correct next page
                if (currentState is MoviesState.Content) {
                    _uiState.value = currentState.copy(nextPageToLoad = 2)
                }
                return@launch
            }

            _uiState.value = if (nextPageToLoad == 1) {
                MoviesState.Loading
            } else {
                MoviesState.LoadingMore(
                    movies = movies,
                    nextPageToLoad = nextPageToLoad,
                    canLoadMore = true
                )
            }

            try {
                val result = requestMoviesPage(nextPageToLoad).getOrThrow()
                lastPageResult = result
                isLoadingBlocked = false
                // State will be updated in observeMovies() when new data arrives from DB
            } catch (e: Exception) {
                val errorMessage = getErrorMessage(e)
                isLoadingBlocked = true

                // For first page, wait for observeMovies to provide cached data
                if (nextPageToLoad == 1) {
                    // Set a temporary error state that will be handled by observeMovies
                    _uiState.value = MoviesState.Content(
                        movies = persistentListOf(),
                        nextPageToLoad = nextPageToLoad,
                        canLoadMore = true,
                        errorMessage = errorMessage
                    )
                } else {
                    // For pagination, show error with existing data
                    _uiState.value = MoviesState.Content(
                        movies = movies,
                        nextPageToLoad = nextPageToLoad,
                        canLoadMore = true,
                        errorMessage = errorMessage
                    )
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
                val (movies, nextPageToLoad, canLoadMore) = extractStateData(currentState)

                val errorMessage = getErrorMessage(e, "Failed to toggle favorite")
                _uiState.value = MoviesState.Error(
                    message = errorMessage,
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

            val (movies, nextPageToLoad, canLoadMore) = extractStateData(currentState)

            // Always show refreshing state first for better UX
            _uiState.value = MoviesState.Refreshing(
                movies = movies,
                nextPageToLoad = nextPageToLoad,
                canLoadMore = canLoadMore
            )

            try {
                refreshMovies().getOrThrow()
                // Reset pagination info after refresh
                lastPageResult = null
                isLoadingBlocked = false // Only unblock on successful refresh
                // Force transition from Refreshing to Content
                val currentRefreshingState = _uiState.value as? MoviesState.Refreshing
                if (currentRefreshingState != null) {
                    _uiState.value = MoviesState.Content(
                        movies = currentRefreshingState.movies,
                        nextPageToLoad = 2,
                        canLoadMore = true
                    )
                }
            } catch (e: Exception) {
                val errorMessage = getErrorMessage(e, "Failed to refresh")
                // Add small delay to show refresh indicator
                delay(300)
                // Show cached data with error message
                // Don't unblock loading if refresh failed due to network error
                _uiState.value = MoviesState.Content(
                    movies = movies,
                    nextPageToLoad = nextPageToLoad,
                    canLoadMore = true, // Keep true so user can retry by scrolling
                    errorMessage = errorMessage
                )
            }
        }
    }

    fun clearError() {
        val currentState = _uiState.value
        if (currentState is MoviesState.Content && currentState.errorMessage != null) {
            _uiState.value = currentState.copy(errorMessage = null)
            // Don't unblock loading here - only unblock on successful operations
        }
    }

    private fun getErrorMessage(e: Exception, defaultMessage: String = "Unknown error"): String {
        return when (e) {
            is java.net.UnknownHostException -> "No internet connection"
            is java.net.SocketTimeoutException -> "Connection timeout. Check your internet connection."
            is java.io.IOException -> "Network error. Please check your connection."
            else -> e.message ?: defaultMessage
        }
    }

    private fun extractStateData(state: MoviesState): Triple<ImmutableList<Pair<String, ImmutableList<Movie>>>, Int, Boolean> {
        return when (state) {
            is MoviesState.Content -> Triple(
                state.movies,
                state.nextPageToLoad,
                state.canLoadMore
            )

            is MoviesState.Refreshing -> Triple(
                state.movies,
                state.nextPageToLoad,
                state.canLoadMore
            )

            is MoviesState.LoadingMore -> Triple(
                state.movies,
                state.nextPageToLoad,
                state.canLoadMore
            )

            is MoviesState.Error -> Triple(
                state.movies,
                state.nextPageToLoad,
                state.canLoadMore
            )

            is MoviesState.Loading -> Triple(persistentListOf(), 1, true)
        }
    }

    private fun List<Movie>.toUiMovieList(): ImmutableList<Pair<String, ImmutableList<Movie>>> =
        // Movies are already sorted by release date DESC from the database query
        this.sortedByDescending { it.releaseDate }      // enforce DESC order by date
            .groupBy { formatReleaseDate(it.releaseDate) }
            .map { (month, movies) -> month to movies.toPersistentList() }
            .toPersistentList()

    private fun formatReleaseDate(releaseDate: String): String {
        val input = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val output = SimpleDateFormat("MMMM yyyy", Locale.ENGLISH)

        return try {
            val date = input.parse(releaseDate)
            if (date != null) {
                val formattedDate = output.format(date)
                formattedDate.replaceFirstChar {
                    if (it.isLowerCase()) it.titlecase(Locale.ENGLISH) else it.toString()
                }
            } else {
                "Unknown"
            }
        } catch (_: Exception) {
            "Unknown"
        }
    }
}
