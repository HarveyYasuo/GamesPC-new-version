package com.harvey.gamespc.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import com.harvey.gamespc.ui.home.HomeUiState
import com.harvey.gamespc.ui.home.HomeViewModel

@Composable
fun HomeScreen(
    searchQuery: String, 
    onItemClick: (itemId: String, categoryName: String) -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    when (val state = uiState) {
        is HomeUiState.Loading -> {
            ItemsGridScreen(
                items = emptyList(),
                isLoading = true,
                onItemClick = {}
            )
        }
        is HomeUiState.Success -> {
            val itemsToDisplay = viewModel.searchGames(searchQuery, state)
            ItemsGridScreen(
                items = itemsToDisplay,
                isLoading = false,
                onItemClick = { onItemClick(it.id ?: "", "Juegos") }
            )
        }
        is HomeUiState.Error -> {
            // Podrías mostrar un mensaje de error aquí
        }
    }
}
