package com.harvey.gamespc.ui.components

import android.view.LayoutInflater
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.nativead.MediaView
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdView
import com.harvey.gamespc.R

import android.widget.FrameLayout

@Composable
fun NativeAdComposable(nativeAd: NativeAd, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val adView = remember {
        // Inflate the XML layout, providing a temporary parent to generate correct LayoutParams
        val dummyParent = FrameLayout(context)
        LayoutInflater.from(context).inflate(R.layout.native_ad_layout, dummyParent, false) as NativeAdView
    }

    DisposableEffect(nativeAd) {
        // Find views within the inflated layout
        val headlineView = adView.findViewById<TextView>(R.id.ad_headline)
        val bodyView = adView.findViewById<TextView>(R.id.ad_body)
        val callToActionView = adView.findViewById<Button>(R.id.ad_call_to_action)
        val iconView = adView.findViewById<ImageView>(R.id.ad_icon)
        val mediaView = adView.findViewById<MediaView>(R.id.ad_media)
        val advertiserView = adView.findViewById<TextView>(R.id.ad_advertiser)

        // Associate the views with the NativeAdView
        adView.headlineView = headlineView
        adView.bodyView = bodyView
        adView.callToActionView = callToActionView
        adView.iconView = iconView
        adView.mediaView = mediaView
        adView.advertiserView = advertiserView

        // Populate the views with ad content
        headlineView.text = nativeAd.headline
        bodyView.text = nativeAd.body
        callToActionView.text = nativeAd.callToAction
        nativeAd.icon?.drawable?.let { iconView.setImageDrawable(it) }
        nativeAd.mediaContent?.let { mediaView.mediaContent = it }
        advertiserView.text = nativeAd.advertiser

        // Register the NativeAd object with the NativeAdView
        adView.setNativeAd(nativeAd)

        onDispose {
            // Optional: Clean up resources if needed, though NativeAdView handles much of this.
            // adView.destroy()
        }
    }

    AndroidView(
        factory = { adView },
        modifier = modifier.fillMaxWidth().padding(vertical = 8.dp)
    )
}