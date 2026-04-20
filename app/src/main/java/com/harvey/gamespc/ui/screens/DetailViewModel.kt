package com.harvey.gamespc.ui.screens

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.harvey.gamespc.SharedViewModel
import com.harvey.gamespc.ads.AdManager
import com.harvey.gamespc.data.GameItem
import com.harvey.gamespc.utils.AdBlockerDetector
import com.harvey.gamespc.utils.FileSizeFetcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class AdState {
    object Idle : AdState()
    object Loading : AdState()
    object Ready : AdState()
    data class Error(val message: String) : AdState()
    object AdBlockerDetected : AdState()
}

class DetailViewModel(
    private val itemId: String,
    private val categoryName: String,
    val sharedViewModel: SharedViewModel
) : ViewModel() {

    private val _item = MutableStateFlow<GameItem?>(null)
    val item: StateFlow<GameItem?> = _item

    private val _adState = MutableStateFlow<AdState>(AdState.Idle)
    val adState: StateFlow<AdState> = _adState

    private val _fileSizes = MutableStateFlow<Map<String, String>>(emptyMap())
    val fileSizes: StateFlow<Map<String, String>> = _fileSizes

    private val _activeDownloadUrl = MutableStateFlow<String?>(null)
    val activeDownloadUrl: StateFlow<String?> = _activeDownloadUrl

    private val _userEarnedReward = MutableStateFlow(false)

    init {
        loadItem()
        // loadAd() // REMOVED: No longer loading ad automatically
    }

    private fun loadItem() {
        viewModelScope.launch {
            sharedViewModel.items.collect { items ->
                val category = items.find { it.name.equals(categoryName, ignoreCase = true) }
                val foundItem = category?.data?.find { it.id == itemId }
                _item.value = foundItem
                foundItem?.let {
                    val sizes = it.downloadUrl?.split(",")?.associate { url ->
                        val trimmedUrl = url.trim()
                        trimmedUrl to (FileSizeFetcher.getFileSize(trimmedUrl) ?: "N/A")
                    } ?: emptyMap()
                    _fileSizes.value = sizes
                }
            }
        }
    }

    fun loadAdForDownload(url: String) {
        viewModelScope.launch {
            if (AdBlockerDetector.isAdBlockerActive(sharedViewModel.getApplication())) {
                _adState.value = AdState.AdBlockerDetected
            } else {
                _activeDownloadUrl.value = url
                _adState.value = AdState.Loading
                AdManager.loadRewardedAd(
                    sharedViewModel.getApplication(),
                    onAdLoaded = { _adState.value = AdState.Ready },
                    onAdFailedToLoad = {
                        _adState.value = AdState.Error(it)
                        _activeDownloadUrl.value = null // Reset on failure
                    }
                )
            }
        }
    }

    fun showAd(activity: Activity) {
        val downloadUrl = _activeDownloadUrl.value ?: return // Don't show if no URL is active

        // Reset reward state before showing a new ad
        _userEarnedReward.value = false

        AdManager.showRewardedAd(
            activity,
            onUserEarnedReward = {
                // Mark that the user has earned the reward
                _userEarnedReward.value = true
            },
            onAdDismissed = {
                // Check if the user earned the reward before starting the download
                if (_userEarnedReward.value) {
                    startDownload(downloadUrl, activity)
                }
                // Reset states after the ad is dismissed
                _adState.value = AdState.Idle
                _activeDownloadUrl.value = null
                _userEarnedReward.value = false // Reset for the next ad cycle
            },
            onAdFailedToShow = {
                _adState.value = AdState.Error("Ad failed to show.")
                _activeDownloadUrl.value = null // Reset on failure
                _userEarnedReward.value = false // Reset on failure
            }
        )
    }

    fun showAdForInAppDownload(activity: Activity, url: String) {
        val downloadUrl = _activeDownloadUrl.value ?: return

        _userEarnedReward.value = false

        AdManager.showRewardedAd(
            activity,
            onUserEarnedReward = {
                _userEarnedReward.value = true
            },
            onAdDismissed = {
                if (_userEarnedReward.value) {
                    val fileName = item.value?.title ?: "downloaded_file"
                    sharedViewModel.startInAppDownload(downloadUrl, fileName)
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

    fun startDownload(url: String, context: Context) {
        val intent = Intent(Intent.ACTION_VIEW, url.toUri())
        context.startActivity(intent)
    }

    override fun onCleared() {
        super.onCleared()
        AdManager.releaseAd()
    }

    class Factory(
        private val itemId: String,
        private val categoryName: String,
        private val sharedViewModel: SharedViewModel
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(DetailViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return DetailViewModel(itemId, categoryName, sharedViewModel) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
