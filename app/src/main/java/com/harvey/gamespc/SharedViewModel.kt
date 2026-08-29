package com.harvey.gamespc

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.harvey.gamespc.data.GameItem
import com.harvey.gamespc.data.GameTable
import com.harvey.gamespc.data.repository.FirebaseGameRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import javax.inject.Inject

@HiltViewModel
class SharedViewModel @Inject constructor(
    application: Application,
    private val repository: FirebaseGameRepository
) : AndroidViewModel(application) {

    private val _items = MutableStateFlow<List<GameTable>>(emptyList())
    val items: StateFlow<List<GameTable>> = _items

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _searchResults = MutableStateFlow<List<GameTable>>(emptyList())
    val searchResults: StateFlow<List<GameTable>> = _searchResults

    private val _pipModeState = MutableStateFlow(false)
    val pipModeState: StateFlow<Boolean> = _pipModeState

    // Señal para refrescar el Home cuando se agrega contenido desde la pestaña "Agregar"
    private val _contentVersion = MutableStateFlow(0)
    val contentVersion: StateFlow<Int> = _contentVersion.asStateFlow()

    // Petición para abrir la pestaña del chat (notificaciones)
    private val _openChatRequest = MutableStateFlow(false)
    val openChatRequest: StateFlow<Boolean> = _openChatRequest.asStateFlow()

    // Limita a 4 peticiones de tamaño simultáneas para no saturar la red al abrir
    private val sizeFetchSemaphore = Semaphore(4)

    init {
        fetchItems()
    }

    fun fetchItems() {
        viewModelScope.launch {
            _loading.value = true
            _error.value = null
            
            repository.getAllGames().collect { result ->
                result.fold(
                    onSuccess = { fetchedItems ->
                        _items.value = fetchedItems
                        _loading.value = false
                        // Once we have items, we start fetching file sizes in background
                        fetchFileSizes(fetchedItems)
                    },
                    onFailure = { exception ->
                        _error.value = "Error fetching items: ${exception.message}"
                        _items.value = emptyList()
                        _loading.value = false
                    }
                )
            }
        }
    }

    private fun fetchFileSizes(tables: List<GameTable>) {
        tables.forEach { table ->
            table.data?.forEach { item ->
                item.downloadUrl?.let { url ->
                    if (item.fileSize == null) { // Only fetch if not already present
                        viewModelScope.launch {
                            sizeFetchSemaphore.withPermit {
                                val fileSize = repository.fetchItemFileSize(url)
                                updateItemFileSize(table, item, fileSize)
                            }
                        }
                    }
                }
            }
        }
    }

    private fun updateItemFileSize(table: GameTable, item: GameItem, fileSize: String?) {
        val itemId = item.id ?: return
        if (fileSize == null) return

        _items.value = _items.value.map { currentTable ->
            if (currentTable.name == table.name) {
                val updatedData = currentTable.data?.map { currentItem ->
                    if (currentItem.id == itemId) {
                        currentItem.copy(fileSize = fileSize)
                    } else {
                        currentItem
                    }
                }
                currentTable.copy(data = updatedData)
            } else {
                currentTable
            }
        }

        // Persistir el tamaño en Firebase con las claves reales (tabla e item)
        val tableKey = table.key ?: return
        val itemKey = item.key ?: itemId
        repository.saveFileSize(tableKey, itemKey, fileSize)
    }

    fun searchItems(query: String) {
        val allItems = _items.value
        _searchResults.value = allItems.mapNotNull { table ->
            val filteredData = table.data?.filter { item ->
                (item.title?.contains(query, ignoreCase = true) ?: false) ||
                        (item.description?.contains(query, ignoreCase = true) ?: false)
            }
            if (filteredData.isNullOrEmpty()) null else table.copy(data = filteredData)
        }
    }

    fun setPipMode(enabled: Boolean) {
        _pipModeState.value = enabled
    }

    /** Se llama cuando la pestaña "Agregar" guarda contenido nuevo. */
    fun notifyContentAdded() {
        _contentVersion.value += 1
    }

    /** Se llama desde MainActivity cuando una notificación de chat se toca. */
    fun requestOpenChat() {
        _openChatRequest.value = true
    }

    fun consumeOpenChatRequest() {
        _openChatRequest.value = false
    }
}
