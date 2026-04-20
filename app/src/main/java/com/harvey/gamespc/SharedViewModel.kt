package com.harvey.gamespc

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.harvey.gamespc.data.GameTable
import com.harvey.gamespc.data.repository.FirebaseGameRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
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
                            val fileSize = repository.fetchItemFileSize(url)
                            updateItemFileSize(table.name, item.id, fileSize)
                        }
                    }
                }
            }
        }
    }

    private fun updateItemFileSize(tableName: String?, itemId: String?, fileSize: String?) {
        if (tableName == null || itemId == null || fileSize == null) return
        
        _items.value = _items.value.map { currentTable ->
            if (currentTable.name == tableName) {
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
}
