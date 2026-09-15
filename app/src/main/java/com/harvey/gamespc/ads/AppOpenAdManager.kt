package com.harvey.gamespc.ads

import android.app.Activity
import android.content.Context
import android.util.Log
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.appopen.AppOpenAd
import java.util.Date

object AppOpenAdManager {

    private const val TAG = "AppOpenAdManager"

    private const val TEST_APP_OPEN_AD_UNIT_ID = "ca-app-pub-3940256099942544/9257395921"
    private const val PROD_APP_OPEN_AD_UNIT_ID = "ca-app-pub-7408875684074602/3080493820"

    private const val AD_EXPIRATION_MS = 4 * 60 * 60 * 1000L // 4 horas

    private var appOpenAd: AppOpenAd? = null
    private var isLoadingAd = false
    private var isShowingAd = false
    private var loadTime: Long = 0

    private val adUnitId: String
        get() = if (com.harvey.gamespc.BuildConfig.DEBUG) {
            TEST_APP_OPEN_AD_UNIT_ID
        } else {
            PROD_APP_OPEN_AD_UNIT_ID
        }

    fun isAdAvailable(): Boolean {
        if (appOpenAd == null) return false
        val isExpired = Date().time - loadTime > AD_EXPIRATION_MS
        return !isExpired
    }

    fun loadAd(context: Context, onAdLoaded: () -> Unit = {}) {
        if (isLoadingAd || isAdAvailable()) {
            return
        }
        isLoadingAd = true

        val adRequest = AdRequest.Builder().build()
        AppOpenAd.load(context, adUnitId, adRequest, object : AppOpenAd.AppOpenAdLoadCallback() {
            override fun onAdLoaded(ad: AppOpenAd) {
                Log.d(TAG, "App open ad was loaded.")
                appOpenAd = ad
                isLoadingAd = false
                loadTime = Date().time
                onAdLoaded()
            }

            override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                Log.d(TAG, "App open ad failed to load: ${loadAdError.message}")
                appOpenAd = null
                isLoadingAd = false
            }
        })
    }

    fun showAdIfAvailable(activity: Activity, onAdDismissed: () -> Unit = {}) {
        if (!isAdAvailable()) {
            Log.d(TAG, "App open ad wasn't ready yet. Loading for next time.")
            loadAd(activity)
            return
        }
        if (isShowingAd) {
            Log.d(TAG, "App open ad is already showing.")
            return
        }

        isShowingAd = true
        appOpenAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                Log.d(TAG, "App open ad dismissed fullscreen content.")
                appOpenAd = null
                isShowingAd = false
                loadAd(activity)
                onAdDismissed()
            }

            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                Log.e(TAG, "App open ad failed to show: ${adError.message}")
                appOpenAd = null
                isShowingAd = false
                loadAd(activity)
            }

            override fun onAdShowedFullScreenContent() {
                Log.d(TAG, "App open ad showed fullscreen content.")
            }
        }
        appOpenAd?.show(activity)
    }
}
