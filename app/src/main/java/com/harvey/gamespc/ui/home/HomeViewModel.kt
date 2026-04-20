package com.harvey.gamespc.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.harvey.gamespc.data.GameItem
import com.harvey.gamespc.data.repository.GameRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: GameRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        fetchHomeContent()
    }

    private fun fetchHomeContent() {
        viewModelScope.launch {
            _uiState.value = HomeUiState.Loading
            repository.getAllGames().collect { result ->
                result.fold(
                    onSuccess = { tables ->
                        val games = tables
                            .filter { it.name.equals("Juegos", ignoreCase = true) }
                            .flatMap { it.data.orEmpty() }
                        _uiState.value = HomeUiState.Success(games)
                    },
                    onFailure = { exception ->
                        _uiState.value = HomeUiState.Error(exception.message ?: "Unknown error")
                    }
                )
            }
        }
    }

    fun searchGames(query: String, successState: HomeUiState.Success): List<GameItem> {
        if (query.isBlank()) return successState.games
        return successState.games.filter {
            it.title?.contains(query, ignoreCase = true) == true ||
            it.description?.contains(query, ignoreCase = true) == true
        }
    }
}

sealed class HomeUiState {
    object Loading : HomeUiState()
    data class Success(val games: List<GameItem>) : HomeUiState()
    data class Error(val message: String) : HomeUiState()
}
