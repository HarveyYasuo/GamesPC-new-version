package com.harvey.gamespc.utils

import android.app.DownloadManager
import android.content.Context
import android.os.Environment
import android.util.Log
import androidx.core.net.toUri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.util.concurrent.TimeUnit
import java.util.regex.Pattern


// Sealed class to represent download status
sealed class DownloadStatus {
    data object Idle : DownloadStatus()
    data class Progress(val progress: Int) : DownloadStatus()
    data class Success(val file: File) : DownloadStatus()
    data class Error(val message: String) : DownloadStatus()
}

// Object to handle Mediafire downloads
object MediafireDownloader {

    private const val TAG = "MediafireDownloader"

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .followRedirects(true)
        .build()

    private suspend fun getDirectLink(mediafireUrl: String): Result<String> {
        return withContext(Dispatchers.IO) {
            try {
                val request = Request.Builder().url(mediafireUrl).build()
                val response = client.newCall(request).execute()

                if (!response.isSuccessful) {
                    val errorMessage = "Failed to fetch Mediafire page: ${response.code}"
                    Log.e(TAG, errorMessage)
                    return@withContext Result.failure(Exception(errorMessage))
                }

                val htmlContent = response.body?.string() ?: run {
                    val errorMessage = "Failed to get HTML content from Mediafire page."
                    Log.e(TAG, errorMessage)
                    return@withContext Result.failure(Exception(errorMessage))
                }

                val pattern = Pattern.compile("<a[^>]*id=\"downloadButton\"[^>]*>")
                val matcher = pattern.matcher(htmlContent)

                if (matcher.find()) {
                    val aTag = matcher.group(0)
                    if (aTag != null) {
                        val hrefPattern = Pattern.compile("href=\"([^\"]+)\"")
                        val hrefMatcher = hrefPattern.matcher(aTag)
                        if (hrefMatcher.find()) {
                            val directUrl = hrefMatcher.group(1)
                            if (directUrl != null) {
                                Result.success(directUrl) // The direct URL
                            } else {
                                val errorMessage = "Found download button but href is null."
                                Log.e(TAG, errorMessage)
                                Result.failure(Exception(errorMessage))
                            }
                        } else {
                            val errorMessage = "Could not find href in download button."
                            Log.e(TAG, errorMessage)
                            Result.failure(Exception(errorMessage))
                        }
                    } else {
                        val errorMessage = "Could not find download link pattern in HTML."
                        Log.e(TAG, errorMessage)
                        Result.failure(Exception(errorMessage))
                    }
                } else {
                    val errorMessage = "Could not find download link pattern in HTML."
                    Log.e(TAG, errorMessage)
                    Result.failure(Exception(errorMessage))
                }
            } catch (e: Exception) {
                Log.e(TAG, "Exception while getting direct link", e)
                Result.failure(e)
            }
        }
    }

    fun downloadFile(
        context: Context,
        mediafireUrl: String,
        fileName: String
    ): Long {
        val directLinkResult = runBlocking { getDirectLink(mediafireUrl) }
        if (directLinkResult.isFailure) {
            return -1
        }
        val directLink = directLinkResult.getOrThrow()

        val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val request = DownloadManager.Request(directLink.toUri())
            .setTitle(fileName)
            .setDescription("Downloading")
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "GamesOG/Downloads/$fileName")

        return downloadManager.enqueue(request)
    }
}

