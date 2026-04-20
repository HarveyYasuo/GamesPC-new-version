package com.harvey.gamespc.utils

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.util.Locale
import java.util.concurrent.TimeUnit
import kotlin.math.log10
import kotlin.math.pow

object FileSizeFetcher {

    private const val TAG = "FileSizeFetcher"

    private var gofileAuthToken: String? = null

    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS) // Increase to 10 seconds
        .readTimeout(10, TimeUnit.SECONDS)    // Increase to 10 seconds
        .followRedirects(true) // Follow redirects to find the final download link
        .build()

    suspend fun getFileSize(url: String): String? {
        Log.d(TAG, "Fetching file size for URL: $url")
        return withContext(Dispatchers.IO) {
            try {
                when {
                    "mediafire.com" in url -> getMediafireFileSize(url)
                    "1fichier.com" in url -> get1FichierFileSize(url)

                    else -> getDirectLinkFileSize(url) // Fallback for other direct links
                }
            } catch (t: Throwable) {
                Log.e(TAG, "General exception in getFileSize", t)
                null // Return null on any exception
            }
        }
    }



    private fun getMediafireFileSize(url: String): String? {
        return try {
            // 1. Get the HTML page content
            val pageRequest = Request.Builder().url(url).build()
            val htmlContent = client.newCall(pageRequest).execute().use { response ->
                if (!response.isSuccessful) {
                    Log.e(TAG, "Mediafire page request not successful: ${response.code}")
                    return null
                }
                response.body?.string()
            } ?: return null

            Log.d(TAG, "Mediafire HTML size: ${htmlContent.length}")

            // 2. Find the file size string from the download button text
            Log.d(TAG, "Searching for size text in Mediafire HTML...")
            val regex = """Download \(([\d.]+\s*(?:GB|MB|KB|TB|B))\)""" .toRegex()
            val matchResult = regex.find(htmlContent)
            Log.d(TAG, "Mediafire regex search finished.")

            if (matchResult != null && matchResult.groupValues.size > 1) {
                val sizeString = matchResult.groupValues[1]
                Log.d(TAG, "Mediafire size string found: $sizeString")
                sizeString // Return the found string directly
            } else {
                Log.w(TAG, "Mediafire size string not found in HTML.")
                null
            }
        } catch (t: Throwable) {
            Log.e(TAG, "Mediafire (scraping) request failed with Throwable", t)
            null
        }
    }

    private fun get1FichierFileSize(url: String): String? {
        return try {
            // 1. Get the HTML page content
            val pageRequest = Request.Builder().url(url).build()
            val htmlContent = client.newCall(pageRequest).execute().use { response ->
                if (!response.isSuccessful) {
                    Log.e(TAG, "1Fichier page request not successful: ${response.code}")
                    return null
                }
                response.body?.string()
            } ?: return null

            Log.d(TAG, "1Fichier HTML size: ${htmlContent.length}")

            // 2. Find the file size string in the HTML
            // This regex is a general pattern, might need refinement after inspecting a sample page
            Log.d(TAG, "Searching for size in 1Fichier HTML...")
            val regex = """<span style="font-size:0.9em;font-style:italic">([\d.]+\s*(?:GB|MB|KB|TB|B))</span>""".toRegex()
            val matchResult = regex.find(htmlContent)
            Log.d(TAG, "1Fichier regex search finished.")

            if (matchResult != null && matchResult.groupValues.size > 1) {
                val sizeString = matchResult.groupValues[1]
                Log.d(TAG, "1Fichier size string found: $sizeString")
                sizeString
            } else {
                Log.w(TAG, "1Fichier size string not found in HTML.")
                null
            }
        } catch (t: Throwable) {
            Log.e(TAG, "1Fichier (scraping) request failed with Throwable", t)
            null
        }
    }

    private fun getDirectLinkFileSize(url: String): String? {
        return try {
            val request = Request.Builder().head().url(url).build()
            client.newCall(request).execute().use {
                if (!it.isSuccessful) {
                    Log.e(TAG, "Direct link request not successful: ${it.code}")
                    return@use null
                }
                val contentLength = it.header("Content-Length")?.toLongOrNull()
                Log.d(TAG, "Direct link Content-Length: $contentLength")
                contentLength?.let { length -> formatBytes(length) }
            }
        } catch (t: Throwable) {
            Log.e(TAG, "Direct link request failed with Throwable", t)
            null
        }
    }



    private fun formatBytes(bytes: Long): String {
        if (bytes <= 0) return "0 B"
        val units = arrayOf("B", "KB", "MB", "GB", "TB")
        val digitGroups = (log10(bytes.toDouble()) / log10(1024.0)).toInt()
        if (digitGroups >= units.size) return "Large File"
        return String.format(Locale.US, "%.1f %s", bytes / 1024.0.pow(digitGroups.toDouble()), units[digitGroups])
    }
}
