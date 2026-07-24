package com.harvey.gamespc

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import coil3.SingletonImageLoader
import coil3.disk.DiskCache
import coil3.memory.MemoryCache
import com.google.android.gms.ads.MobileAds
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.messaging.FirebaseMessaging
import com.harvey.gamespc.notifications.ChatNotificationManager
import com.harvey.gamespc.notifications.NotificationWorker
import com.harvey.gamespc.utils.PresenceManager
import dagger.hilt.android.HiltAndroidApp
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import okio.Path.Companion.toOkioPath

@HiltAndroidApp
class MyApplication : Application(), SingletonImageLoader.Factory, LifecycleEventObserver {

    @Inject
    lateinit var presenceManager: PresenceManager

    @Inject
    lateinit var chatNotificationManager: ChatNotificationManager

    companion object {
        lateinit var analytics: FirebaseAnalytics
    }

    override fun onCreate() {
        super.onCreate()
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)

        // Initialize Firebase Analytics
        analytics = FirebaseAnalytics.getInstance(this)

        // PLAN B: notificaciones locales del chat general escuchando
        // Realtime Database (sin Cloud Functions). Funciona mientras el
        // proceso de la app esté vivo; NO con la app cerrada del todo.
        chatNotificationManager.start()

        // Suscribirse al tema FCM del chat general para recibir notificaciones
        // cuando alguien envía un mensaje (lo envía la Cloud Function).
        FirebaseMessaging.getInstance().subscribeToTopic("general_chat")
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d("MyApplication", "Suscrito al tema general_chat")
                } else {
                    Log.w("MyApplication", "Fallo al suscribirse a general_chat: ${task.exception?.message}")
                }
            }

        // Initialize Google Mobile Ads SDK
        MobileAds.initialize(this) { initializationStatus ->
            Log.d("MyApplication", "Mobile Ads SDK initialized with status: $initializationStatus")
        }

        // Global AdMob Policy Compliance: Ensure ads are family-friendly and COPPA compliant
        // This is necessary to reach the "widest possible audience" (General Audience/Families)
        val requestConfiguration = MobileAds.getRequestConfiguration().toBuilder()
            .setTagForChildDirectedTreatment(com.google.android.gms.ads.RequestConfiguration.TAG_FOR_CHILD_DIRECTED_TREATMENT_TRUE)
            .setMaxAdContentRating(com.google.android.gms.ads.RequestConfiguration.MAX_AD_CONTENT_RATING_G)
            .build()
        MobileAds.setRequestConfiguration(requestConfiguration)

    }

    override fun newImageLoader(context: Context): coil3.ImageLoader {
        return coil3.ImageLoader.Builder(context)
            .memoryCache {
                MemoryCache.Builder()
                    .maxSizePercent(context, 0.25) // Use 25% of the app's available memory for the memory cache.
                    .build()
            }
            .diskCache {
                DiskCache.Builder()
                    .directory(context.cacheDir.resolve("image_cache").toOkioPath())
                    .maxSizeBytes(512L * 1024 * 1024) // 512MB
                    .build()
            }
            .build()
    }

    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        when (event) {
            Lifecycle.Event.ON_START -> {
                presenceManager.goOnline()
                // Cancel any pending inactivity notifications
                WorkManager.getInstance(applicationContext).cancelUniqueWork("inactivity_notification_work")
            }
            Lifecycle.Event.ON_STOP -> {
                presenceManager.goOffline()
                // Schedule an inactivity notification after 30 minutes
                val inactivityRequest = OneTimeWorkRequestBuilder<NotificationWorker>()
                    .setInitialDelay(30, TimeUnit.MINUTES)
                    .build()
                WorkManager.getInstance(applicationContext).enqueueUniqueWork(
                    "inactivity_notification_work",
                    ExistingWorkPolicy.REPLACE,
                    inactivityRequest
                )
            }
            else -> {}
        }
    }
}