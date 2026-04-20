package com.harvey.gamespc.utils

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

object AdBlockerDetector {

    private val CANARY_URLS = listOf(
        "https://raw.githubusercontent.com/harvey122/ad-blocker-canary/main/ads.js",
        "https://pagead2.googlesyndication.com/pagead/js/adsbygoogle.js",
        "https://static.adzerk.net/ados.js"
    )

    suspend fun isAdBlockerActive(context: Context): Boolean {
        return withContext(Dispatchers.IO) {
            var allBlocked = true
            for (url in CANARY_URLS) {
                try {
                    val connection = URL(url).openConnection() as HttpURLConnection
                    connection.requestMethod = "GET"
                    connection.connectTimeout = 10000 // 10 seconds
                    connection.readTimeout = 10000 // 10 seconds

                    connection.connect()

                    val responseCode = connection.responseCode
                    connection.disconnect()

                    if (responseCode in 200..299) {
                        // If we get a successful response from any of the URLs, it means no ad blocker.
                        allBlocked = false
                        break
                    }
                } catch (e: IOException) {
                    // This URL is blocked, continue to the next one.
                } catch (e: Exception) {
                    // Other exceptions might occur, but we'll consider them as no ad blocker for safety.
                    allBlocked = false
                    break
                }
            }
            allBlocked
        }
    }
}
