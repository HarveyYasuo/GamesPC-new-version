package com.harvey.gamespc.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.harvey.gamespc.data.repository.AddContentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class AddContentState { IDLE, SUBMITTING, SUCCESS, ERROR }

@HiltViewModel
class AddContentViewModel @Inject constructor(
    private val repository: AddContentRepository
) : ViewModel() {

    private val _state = MutableStateFlow(AddContentState.IDLE)
    val state: StateFlow<AddContentState> = _state.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    fun addContent(
        titulo: String,
        descripcion: String,
        enlaceSitio: String,
        enlaceImagen: String,
        enlaceVideo: String
    ) {
        if (_state.value == AddContentState.SUBMITTING) return
        _state.value = AddContentState.SUBMITTING
        _errorMessage.value = null
        viewModelScope.launch {
            repository.addContent(titulo, descripcion, enlaceSitio, enlaceImagen, enlaceVideo)
                .onSuccess {
                    _state.value = AddContentState.SUCCESS
                }
                .onFailure { e ->
                    _errorMessage.value = e.message ?: "Error desconocido"
                    _state.value = AddContentState.ERROR
                }
        }
    }

    fun resetToIdle() {
        _state.value = AddContentState.IDLE
        _errorMessage.value = null
    }
}
