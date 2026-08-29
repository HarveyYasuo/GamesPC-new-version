package com.harvey.gamespc.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.harvey.gamespc.SharedViewModel
import com.harvey.gamespc.ui.home.HomeUiState
import com.harvey.gamespc.ui.home.HomeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    searchQuery: String,
    onItemClick: (itemId: String, categoryName: String) -> Unit,
    sharedViewModel: SharedViewModel,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    val contentVersion by sharedViewModel.contentVersion.collectAsState()

    // Refrescar automáticamente cuando se agrega contenido desde la pestaña "Agregar"
    LaunchedEffect(contentVersion) {
        if (contentVersion > 0) viewModel.refresh()
    }

    // Al volver al Home (desde una tarjeta u otra pestaña) refresca la lista,
    // así los contadores de visitas y los items nuevos aparecen solos
    LaunchedEffect(Unit) {
        viewModel.refreshOnEnter()
    }

    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = { viewModel.refresh() },
        modifier = Modifier.fillMaxSize()
    ) {
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
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "No se pudo cargar el contenido",
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Button(
                            onClick = { viewModel.refresh() },
                            modifier = Modifier.padding(top = 8.dp)
                        ) {
                            Text("Reintentar")
                        }
                    }
                }
            }
        }
    }
}
