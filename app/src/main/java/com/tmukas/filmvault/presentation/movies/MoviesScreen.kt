package com.tmukas.filmvault.presentation.movies

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.tmukas.filmvault.domain.model.Movie
import com.tmukas.filmvault.presentation.ui.components.MovieCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoviesScreen(
    viewModel: MoviesViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val listState = rememberLazyListState()
    val groups = state.movies
    val pullState = rememberPullToRefreshState()

    PullToRefreshBox(
        isRefreshing = state.isRefreshing,
        onRefresh = { viewModel.refresh() },
        state = pullState,
        modifier = Modifier.fillMaxSize()
    ) {
        when {
            state.isLoadingInitial && state.movies.isEmpty() -> InitialLoading()
            state.error != null && state.movies.isEmpty() -> ErrorBox(state.error ?: "Error")
            else -> MoviesList(
                groups = groups,
                listState = listState,
                onToggleFavorite = viewModel::onClickFavorite,
                onLoadMore = { viewModel.loadNextPage() },
                isLoadingMore = state.isLoadingMore
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

@Composable
private fun ErrorBox(message: String) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text = message, color = MaterialTheme.colorScheme.error)
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun MoviesList(
    groups: List<Pair<String, List<Movie>>>,
    listState: LazyListState,
    onToggleFavorite: (Movie) -> Unit,
    onLoadMore: () -> Unit,
    isLoadingMore: Boolean
) {
    val shouldLoadMore by remember {
        derivedStateOf {
            val lastVisible = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index
            val total = listState.layoutInfo.totalItemsCount
            lastVisible != null && total > 0 && lastVisible >= total - 6
        }
    }

    LaunchedEffect(shouldLoadMore, isLoadingMore) {
        if (shouldLoadMore && !isLoadingMore) onLoadMore()
    }

    LazyColumn(
        state = listState,
        contentPadding = PaddingValues(12.dp)
    ) {
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
                    modifier = Modifier.padding(bottom = 12.dp)
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
