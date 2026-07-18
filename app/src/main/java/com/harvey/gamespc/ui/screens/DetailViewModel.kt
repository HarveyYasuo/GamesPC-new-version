package com.harvey.gamespc.ui.screens

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.Intent
import androidx.core.net.toUri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.harvey.gamespc.ads.AdManager
import com.harvey.gamespc.data.GameItem
import com.harvey.gamespc.data.repository.GameRepository
import com.harvey.gamespc.utils.AdBlockerDetector
import com.harvey.gamespc.utils.AppDownloadManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class AdState {
    object Idle : AdState()
    object Loading : AdState()
    object Ready : AdState()
    data class Error(val message: String) : AdState()
    object AdBlockerDetected : AdState()
}

@HiltViewModel
class DetailViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val repository: GameRepository,
    private val application: Application,
    private val downloadManager: AppDownloadManager
) : ViewModel() {

    private val itemId: String = checkNotNull(savedStateHandle["itemId"])
    private val categoryName: String = checkNotNull(savedStateHandle["categoryName"])

    private val _item = MutableStateFlow<GameItem?>(null)
    val item: StateFlow<GameItem?> = _item

    private val _adState = MutableStateFlow<AdState>(AdState.Idle)
    val adState: StateFlow<AdState> = _adState

    private val _fileSizes = MutableStateFlow<Map<String, String>>(emptyMap())
    val fileSizes: StateFlow<Map<String, String>> = _fileSizes

    private val _activeDownloadUrl = MutableStateFlow<String?>(null)
    val activeDownloadUrl: StateFlow<String?> = _activeDownloadUrl

    private val _userEarnedReward = MutableStateFlow(false)

    val downloadStates = downloadManager.downloadStates

    init {
        loadItem()
    }

    private fun loadItem() {
        viewModelScope.launch {
            repository.getAllGames().collect { result ->
                result.onSuccess { tables ->
                    val category = tables.find { it.name.equals(categoryName, ignoreCase = true) }
                    val foundItem = category?.data?.find { it.id == itemId }
                    _item.value = foundItem
                    
                    foundItem?.let { item ->
                        // Actualizar tamaños de archivos si no están presentes
                        if (item.fileSize == null) {
                            val urls = item.downloadUrl?.split(",")?.map { it.trim() } ?: emptyList()
                            val sizes = mutableMapOf<String, String>()
                            urls.forEach { url ->
                                val size = repository.fetchItemFileSize(url) ?: "N/A"
                                sizes[url] = size
                            }
                            _fileSizes.value = sizes
                        } else {
                            // Si ya tiene tamaño (por el SharedViewModel o similar), lo usamos
                            val url = item.downloadUrl?.split(",")?.get(0)?.trim() ?: ""
                            _fileSizes.value = mapOf(url to (item.fileSize ?: "N/A"))
                        }
                    }
                }
            }
        }
    }

    fun loadAdForDownload(url: String, context: Context) {
        viewModelScope.launch {
            if (AdBlockerDetector.isAdBlockerActive(application)) {
                _adState.value = AdState.AdBlockerDetected
            } else {
                _activeDownloadUrl.value = url
                _adState.value = AdState.Loading
                AdManager.loadRewardedAd(
                    context,
                    onAdLoaded = { _adState.value = AdState.Ready },
                    onAdFailedToLoad = {
                        _adState.value = AdState.Error(it)
                        _activeDownloadUrl.value = null
                    }
                )
            }
        }
    }

    fun showAd(activity: Activity) {
        val downloadUrl = _activeDownloadUrl.value ?: return
        _userEarnedReward.value = false

        AdManager.showRewardedAd(
            activity,
            onUserEarnedReward = { _userEarnedReward.value = true },
            onAdDismissed = {
                if (_userEarnedReward.value) {
                    startDownload(downloadUrl, activity)
                }
                _adState.value = AdState.Idle
                _activeDownloadUrl.value = null
                _userEarnedReward.value = false
            },
            onAdFailedToShow = {
                _adState.value = AdState.Error("Ad failed to show.")
                _activeDownloadUrl.value = null
                _userEarnedReward.value = false
            }
        )
    }

    fun showAdForInAppDownload(activity: Activity, url: String) {
        val downloadUrl = _activeDownloadUrl.value ?: return
        _userEarnedReward.value = false

        AdManager.showRewardedAd(
            activity,
            onUserEarnedReward = { _userEarnedReward.value = true },
            onAdDismissed = {
                if (_userEarnedReward.value) {
                    val fileName = item.value?.title ?: "downloaded_file"
                    downloadManager.startInAppDownload(downloadUrl, fileName)
                }
                _adState.value = AdState.Idle
                _activeDownloadUrl.value = null
                _userEarnedReward.value = false
            },
            onAdFailedToShow = {
                _adState.value = AdState.Error("Ad failed to show.")
                _activeDownloadUrl.value = null
                _userEarnedReward.value = false
            }
        )
    }

    fun cancelDownload(url: String) {
        downloadManager.cancelDownload(url)
    }

    fun startDownload(url: String, context: Context) {
        val intent = Intent(Intent.ACTION_VIEW, url.toUri())
        context.startActivity(intent)
    }

    override fun onCleared() {
        super.onCleared()
        AdManager.releaseAd()
    }
}
