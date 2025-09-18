package com.tmukas.filmvault.presentation.movies

import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.tmukas.filmvault.domain.model.Movie
import com.tmukas.filmvault.presentation.ui.components.MovieCard
import kotlinx.collections.immutable.ImmutableList

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoviesScreen(
    listState: LazyListState = rememberLazyListState(),
    viewModel: MoviesViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val pullState = rememberPullToRefreshState()
    val context = LocalContext.current

    PullToRefreshBox(
        isRefreshing = state is MoviesState.Refreshing,
        onRefresh = { viewModel.refresh() },
        state = pullState,
        modifier = Modifier.fillMaxSize()
    ) {
        val currentState = state
        when (currentState) {
            is MoviesState.Loading -> InitialLoading()

            is MoviesState.Error -> MoviesList(
                groups = currentState.movies,
                listState = listState,
                onToggleFavorite = viewModel::onClickFavorite,
                onLoadMore = { viewModel.loadNextPage() },
                isLoadingMore = false,
                emptyMessage = currentState.message,
                canLoadMore = false
            )

            is MoviesState.Content -> {
                // Show error message if present
                currentState.errorMessage?.let { errorMessage ->
                    LaunchedEffect(errorMessage) {
                        Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
                        // Clear error after showing
                        viewModel.clearError()
                    }
                }

                MoviesList(
                    groups = currentState.movies,
                    listState = listState,
                    onToggleFavorite = viewModel::onClickFavorite,
                    onLoadMore = { viewModel.loadNextPage() },
                    isLoadingMore = false,
                    emptyMessage = "No movies yet. Pull to refresh to load movies.",
                    canLoadMore = currentState.canLoadMore
                )
            }

            is MoviesState.Refreshing -> MoviesList(
                groups = currentState.movies,
                listState = listState,
                onToggleFavorite = viewModel::onClickFavorite,
                onLoadMore = { viewModel.loadNextPage() },
                isLoadingMore = false,
                emptyMessage = "Loading movies...",
                canLoadMore = false
            )

            is MoviesState.LoadingMore -> MoviesList(
                groups = currentState.movies,
                listState = listState,
                onToggleFavorite = viewModel::onClickFavorite,
                onLoadMore = { viewModel.loadNextPage() },
                isLoadingMore = true,
                emptyMessage = "No movies yet. Pull to refresh to load movies.",
                canLoadMore = false
            )
        }
    }
}

@Composable
private fun InitialLoading() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun MoviesList(
    groups: ImmutableList<Pair<String, ImmutableList<Movie>>>,
    listState: LazyListState,
    onToggleFavorite: (Movie) -> Unit,
    onLoadMore: () -> Unit,
    isLoadingMore: Boolean,
    emptyMessage: String,
    canLoadMore: Boolean = true
) {
    val shouldLoadMore by remember {
        derivedStateOf {
            val lastVisible = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index
            val total = listState.layoutInfo.totalItemsCount
            lastVisible != null && total > 0 && lastVisible >= total - 6
        }
    }

    LaunchedEffect(shouldLoadMore, isLoadingMore, canLoadMore) {
        if (shouldLoadMore && !isLoadingMore && groups.isNotEmpty() && canLoadMore) {
            onLoadMore()
        }
    }

    LazyColumn(
        state = listState,
        contentPadding = PaddingValues(12.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        if (groups.isEmpty()) {
            // Show empty state as a single item in LazyColumn
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = emptyMessage,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            // Show movie groups
            groups.forEach { (header, movies) ->
                stickyHeader {
                    Text(
                        text = header,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(vertical = 6.dp)
                    )
                }
                items(movies, key = { it.id }) { movie ->
                    MovieCard(
                        movie = movie,
                        onToggleFavorite = onToggleFavorite,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp)
                    )
                }
            }

            if (isLoadingMore) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
            }
        }
    }
}
