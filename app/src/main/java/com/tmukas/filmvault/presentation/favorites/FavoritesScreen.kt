package com.tmukas.filmvault.presentation.favorites

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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.tmukas.filmvault.domain.model.Movie
import com.tmukas.filmvault.presentation.ui.components.MovieCard

@Composable
fun FavoritesScreen(
    listState: LazyListState = rememberLazyListState(),
    viewModel: FavoritesViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    when (val currentState = state) {
        is FavoritesState.Loading -> {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }

        is FavoritesState.Empty -> {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No favorites yet",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        is FavoritesState.Content -> {
            LazyColumn(
                state = listState,
                contentPadding = PaddingValues(12.dp)
            ) {
                items(currentState.favorites, key = { it.id }) { movie: Movie ->
                    MovieCard(
                        movie = movie,
                        onToggleFavorite = { viewModel.onClickFavorite(it) },
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                }
            }
        }

        is FavoritesState.Error -> {
            if (currentState.favorites.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = currentState.message,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            } else {
                LazyColumn(
                    state = listState,
                    contentPadding = PaddingValues(12.dp)
                ) {
                    items(currentState.favorites, key = { it.id }) { movie: Movie ->
                        MovieCard(
                            movie = movie,
                            onToggleFavorite = { viewModel.onClickFavorite(it) },
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                    }
                }
            }
        }
    }
}
