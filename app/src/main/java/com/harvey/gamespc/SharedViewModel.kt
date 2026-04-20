package com.harvey.gamespc

import android.app.Application
import android.content.Context
import android.os.Bundle
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.database.FirebaseDatabase
import com.harvey.gamespc.data.GameTable
import com.harvey.gamespc.utils.DownloadStatus
import com.harvey.gamespc.utils.FileSizeFetcher
import com.harvey.gamespc.utils.MediafireDownloader
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import androidx.core.net.toUri
import kotlinx.coroutines.tasks.await

class SharedViewModel(
    application: Application,
    private val repository: FirebaseGameRepository = FirebaseGameRepository()
) : AndroidViewModel(application) {

    private val _items = MutableStateFlow<List<GameTable>>(emptyList())
    val items: StateFlow<List<GameTable>> = _items

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _searchResults = MutableStateFlow<List<GameTable>>(emptyList())
    val searchResults: StateFlow<List<GameTable>> = _searchResults

    private val _downloadStates = MutableStateFlow<Map<String, DownloadStatus>>(emptyMap())
    val downloadStates: StateFlow<Map<String, DownloadStatus>> = _downloadStates

    private val _pipModeState = MutableStateFlow(false)
    val pipModeState: StateFlow<Boolean> = _pipModeState

    private val downloadIds = mutableMapOf<String, Long>()

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

    fun startInAppDownload(url: String, fileName: String) {
        val bundle = Bundle()
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, fileName)
        MyApplication.analytics.logEvent("download_start", bundle)

        val downloadId = MediafireDownloader.downloadFile(getApplication(), url, fileName)
        if (downloadId != -1L) {
            downloadIds[url] = downloadId
            viewModelScope.launch {
                monitorDownload(url, downloadId)
            }
        }
    }

    private suspend fun monitorDownload(url: String, downloadId: Long) {
        val downloadManager =
            getApplication<Application>().getSystemService(Context.DOWNLOAD_SERVICE) as android.app.DownloadManager
        var downloading = true
        while (downloading) {
            val query = android.app.DownloadManager.Query().setFilterById(downloadId)
            val cursor = downloadManager.query(query)
            if (cursor.moveToFirst()) {
                val statusColumnIndex =
                    cursor.getColumnIndex(android.app.DownloadManager.COLUMN_STATUS)
                if (statusColumnIndex != -1) {
                    val status = cursor.getInt(statusColumnIndex)
                    when (status) {
                        android.app.DownloadManager.STATUS_SUCCESSFUL -> {
                            downloading = false
                            val uriStringIndex =
                                cursor.getColumnIndex(android.app.DownloadManager.COLUMN_LOCAL_URI)
                            if (uriStringIndex != -1) {
                                val uriString = cursor.getString(uriStringIndex)
                                val file = java.io.File(uriString.toUri().path!!)
                                val newMap = _downloadStates.value.toMutableMap()
                                newMap[url] = DownloadStatus.Success(file)
                                _downloadStates.value = newMap
                            } else {
                                val newMap = _downloadStates.value.toMutableMap()
                                newMap[url] =
                                    DownloadStatus.Error("Download failed: Local URI not found.")
                                _downloadStates.value = newMap
                            }
                        }

                        android.app.DownloadManager.STATUS_FAILED -> {
                            downloading = false
                            val newMap = _downloadStates.value.toMutableMap()
                            newMap[url] = DownloadStatus.Error("Download failed")
                            _downloadStates.value = newMap
                        }

                        android.app.DownloadManager.STATUS_PAUSED, android.app.DownloadManager.STATUS_PENDING -> {
                            // Do nothing, wait for next update
                        }

                        android.app.DownloadManager.STATUS_RUNNING -> {
                            val totalBytesIndex =
                                cursor.getColumnIndex(android.app.DownloadManager.COLUMN_TOTAL_SIZE_BYTES)
                            val downloadedBytesIndex =
                                cursor.getColumnIndex(android.app.DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR)
                            if (totalBytesIndex != -1 && downloadedBytesIndex != -1) {
                                val total = cursor.getLong(totalBytesIndex)
                                if (total >= 0) {
                                    val downloaded = cursor.getLong(downloadedBytesIndex)
                                    val progress = ((downloaded * 100) / total).toInt()
                                    val newMap = _downloadStates.value.toMutableMap()
                                    newMap[url] = DownloadStatus.Progress(progress)
                                    _downloadStates.value = newMap
                                }
                            }
                        }
                    }
                }
                cursor.close()
            }
            kotlinx.coroutines.delay(1000) // Update every second
        }
    }

    fun cancelDownload(url: String) {
        val downloadId = downloadIds[url]
        if (downloadId != null) {
            val downloadManager =
                getApplication<Application>().getSystemService(Context.DOWNLOAD_SERVICE) as android.app.DownloadManager
            downloadManager.remove(downloadId)
            downloadIds.remove(url)
            val newMap = _downloadStates.value.toMutableMap()
            newMap[url] = DownloadStatus.Idle
            _downloadStates.value = newMap
        }
    }
}