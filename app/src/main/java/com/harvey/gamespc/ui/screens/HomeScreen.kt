package com.harvey.gamespc.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.harvey.gamespc.SharedViewModel

@Composable
fun HomeScreen(
    sharedViewModel: SharedViewModel, 
    searchQuery: String, 
    onItemClick: (itemId: String, categoryName: String) -> Unit
) {
    val gistTables by sharedViewModel.items.collectAsState()
    val isLoading by sharedViewModel.loading.collectAsState()
    val searchResults by sharedViewModel.searchResults.collectAsState()

    val itemsToDisplay = if (searchQuery.isNotBlank()) {
        searchResults
            .filter { it.name.equals("Juegos", ignoreCase = true) }
            .flatMap { it.data.orEmpty() }
    } else {
        gistTables
            .filter { it.name.equals("Juegos", ignoreCase = true) }
            .flatMap { it.data.orEmpty() }
    }

    ItemsGridScreen(
        items = itemsToDisplay,
        isLoading = isLoading,
        onItemClick = { onItemClick(it.id ?: "", "Juegos") }
    )
}