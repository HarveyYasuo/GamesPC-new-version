package com.harvey.gamespc.ads

import android.app.Activity
import android.content.Context
import android.util.Log
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback

object AdManager {

    private const val TAG = "AdManager"
    // TODO: Reemplaza con tu ID de bloque de anuncios de AdMob real
    private const val PROD_REWARDED_AD_UNIT_ID = "ca-app-pub-7408875684074602/1197519873"
    private const val REWARDED_AD_UNIT_ID = "ca-app-pub-3940256099942544/5224354917" // Test ID

    private var rewardedAd: RewardedAd? = null
    private var isLoadingAd = false

    fun loadRewardedAd(context: Context, onAdLoaded: () -> Unit = {}, onAdFailedToLoad: (String) -> Unit = {}) {
        if (rewardedAd != null || isLoadingAd) {
            return
        }
        isLoadingAd = true

        val adUnitId = if (com.harvey.gamespc.BuildConfig.DEBUG) {
            Log.d(TAG, "Using test rewarded ad unit ID.")
            REWARDED_AD_UNIT_ID
        } else {
            Log.d(TAG, "Using production rewarded ad unit ID.")
            PROD_REWARDED_AD_UNIT_ID
        }

        val adRequest = AdRequest.Builder().build()
        RewardedAd.load(context, adUnitId, adRequest, object : RewardedAdLoadCallback() {
            override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                Log.d(TAG, "Rewarded ad failed to load: ${loadAdError.message}")
                Log.d(TAG, "Response Info: ${loadAdError.responseInfo}")
                rewardedAd = null
                isLoadingAd = false
                onAdFailedToLoad(loadAdError.message)
            }

            override fun onAdLoaded(ad: RewardedAd) {
                Log.d(TAG, "Rewarded ad was loaded.")
                Log.d(TAG, "Response Info: ${ad.responseInfo}")
                rewardedAd = ad
                isLoadingAd = false
                onAdLoaded()
                Log.d(TAG, "Ad is ready to be shown.")
            }
        })
    }

    fun releaseAd() {
        rewardedAd = null
    }

    fun showRewardedAd(activity: Activity, onUserEarnedReward: () -> Unit, onAdDismissed: () -> Unit, onAdFailedToShow: () -> Unit) {
        if (rewardedAd != null) {
            rewardedAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    Log.d(TAG, "Ad dismissed fullscreen content.")
                    rewardedAd = null
                    // Pre-load the next ad automatically
                    loadRewardedAd(activity)
                    onAdDismissed()
                }

                override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                    Log.e(TAG, "Ad failed to show fullscreen content.")
                    Log.e(TAG, "Error: domain=${adError.domain}, code=${adError.code}, message=${adError.message}")
                    rewardedAd = null
                    // Pre-load the next ad automatically
                    loadRewardedAd(activity)
                    onAdFailedToShow()
                }

                override fun onAdShowedFullScreenContent() {
                    Log.d(TAG, "Ad showed fullscreen content.")
                    rewardedAd?.let { ad ->
                        Log.d(TAG, "Ad Impression Response: ${ad.responseInfo}")
                    }
                }
            }
            rewardedAd?.show(activity) { rewardItem ->
                Log.d(TAG, "User earned reward: ${rewardItem.amount} ${rewardItem.type}")
                onUserEarnedReward()
            }
        } else {
            Log.d(TAG, "Rewarded ad wasn't ready yet.")
            // Try to load another ad for the next attempt
            loadRewardedAd(activity)
            onAdFailedToShow()
        }
    }
}