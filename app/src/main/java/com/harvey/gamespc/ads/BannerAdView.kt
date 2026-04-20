package com.harvey.gamespc.ads

import android.annotation.SuppressLint
import android.content.res.Configuration
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView

// Este es el ID de un banner de prueba de Google.
private const val TEST_BANNER_AD_UNIT_ID = "ca-app-pub-3940256099942544/6300978111"
// TODO: Reemplaza con tu ID de bloque de anuncios de Banner de producción real
private const val PROD_BANNER_AD_UNIT_ID = "ca-app-pub-7408875684074602/8086361856"

@Composable
fun BannerAdView(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val adSize = rememberAdaptiveBannerAdSize(configuration)

    val adUnitId = if (com.harvey.gamespc.BuildConfig.DEBUG) {
        TEST_BANNER_AD_UNIT_ID
    } else {
        PROD_BANNER_AD_UNIT_ID
    }

    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        AndroidView(
            modifier = Modifier.fillMaxWidth(),
            factory = {
                AdView(context).apply {
                    this.adUnitId = adUnitId
                    this.setAdSize(adSize)
                    val adRequest = AdRequest.Builder().build()
                    this.loadAd(adRequest)
                }
            }
        )
    }
}

@SuppressLint("ConfigurationScreenWidthHeight")
@Composable
private fun rememberAdaptiveBannerAdSize(configuration: Configuration): AdSize {
    val screenWidth = configuration.screenWidthDp
    return AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(LocalContext.current, screenWidth)
}
