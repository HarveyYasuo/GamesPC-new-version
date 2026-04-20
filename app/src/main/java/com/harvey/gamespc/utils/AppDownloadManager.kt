package com.harvey.gamespc.utils

import android.app.Application
import android.app.DownloadManager
import android.content.Context
import android.os.Bundle
import androidx.core.net.toUri
import com.google.firebase.analytics.FirebaseAnalytics
import com.harvey.gamespc.MyApplication
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppDownloadManager @Inject constructor(
    private val application: Application
) {
    private val _downloadStates = MutableStateFlow<Map<String, DownloadStatus>>(emptyMap())
    val downloadStates: StateFlow<Map<String, DownloadStatus>> = _downloadStates.asStateFlow()

    private val downloadIds = mutableMapOf<String, Long>()
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    fun startInAppDownload(url: String, fileName: String) {
        val bundle = Bundle()
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, fileName)
        MyApplication.analytics.logEvent("download_start", bundle)

        val downloadId = MediafireDownloader.downloadFile(application, url, fileName)
        if (downloadId != -1L) {
            downloadIds[url] = downloadId
            scope.launch {
                monitorDownload(url, downloadId)
            }
        }
    }

    private suspend fun monitorDownload(url: String, downloadId: Long) {
        val downloadManager = application.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        var downloading = true
        while (downloading) {
            val query = DownloadManager.Query().setFilterById(downloadId)
            val cursor = downloadManager.query(query)
            if (cursor.moveToFirst()) {
                val statusColumnIndex = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)
                if (statusColumnIndex != -1) {
                    val status = cursor.getInt(statusColumnIndex)
                    when (status) {
                        DownloadManager.STATUS_SUCCESSFUL -> {
                            downloading = false
                            val uriStringIndex = cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI)
                            if (uriStringIndex != -1) {
                                val uriString = cursor.getString(uriStringIndex)
                                val file = java.io.File(uriString.toUri().path!!)
                                updateState(url, DownloadStatus.Success(file))
                            } else {
                                updateState(url, DownloadStatus.Error("Download failed: Local URI not found."))
                            }
                        }

                        DownloadManager.STATUS_FAILED -> {
                            downloading = false
                            updateState(url, DownloadStatus.Error("Download failed"))
                        }

                        DownloadManager.STATUS_PAUSED, DownloadManager.STATUS_PENDING -> {
                            // Do nothing
                        }

                        DownloadManager.STATUS_RUNNING -> {
                            val totalBytesIndex = cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES)
                            val downloadedBytesIndex = cursor.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR)
                            if (totalBytesIndex != -1 && downloadedBytesIndex != -1) {
                                val total = cursor.getLong(totalBytesIndex)
                                if (total >= 0) {
                                    val downloaded = cursor.getLong(downloadedBytesIndex)
                                    val progress = ((downloaded * 100) / total).toInt()
                                    updateState(url, DownloadStatus.Progress(progress))
                                }
                            }
                        }
                    }
                }
                cursor.close()
            }
            delay(1000)
        }
    }

    private fun updateState(url: String, status: DownloadStatus) {
        val newMap = _downloadStates.value.toMutableMap()
        newMap[url] = status
        _downloadStates.value = newMap
    }

    fun cancelDownload(url: String) {
        val downloadId = downloadIds[url]
        if (downloadId != null) {
            val downloadManager = application.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            downloadManager.remove(downloadId)
            downloadIds.remove(url)
            updateState(url, DownloadStatus.Idle)
        }
    }
}
