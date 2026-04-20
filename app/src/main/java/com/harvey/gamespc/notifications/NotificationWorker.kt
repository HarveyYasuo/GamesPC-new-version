package com.harvey.gamespc.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.harvey.gamespc.MainActivity
import com.harvey.gamespc.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.random.Random

class NotificationWorker(appContext: Context, workerParams: WorkerParameters) :
    CoroutineWorker(appContext, workerParams) {

    companion object {
        const val CHANNEL_ID = "game_event_channel"
        const val NOTIFICATION_ID = 1
    }

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        // In a real app, you'd check if the app is still in the background
        // For simplicity, we'll assume it is for now.

        // Simulate fetching a random game
        val gameTitles = listOf(
            "Cyberpunk 2077",
            "The Witcher 3: Wild Hunt",
            "Red Dead Redemption 2",
            "Grand Theft Auto V",
            "Minecraft",
            "Valorant",
            "League of Legends",
            "Apex Legends"
        )
        val randomGame = gameTitles[Random.nextInt(gameTitles.size)]

        val notificationTitle = applicationContext.getString(R.string.notification_inactivity_title)
        val notificationMessage = applicationContext.getString(R.string.notification_inactivity_message, randomGame)

        createNotificationChannel()
        showNotification(notificationTitle, notificationMessage)

        Result.success()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = applicationContext.getString(R.string.notification_channel_name)
            val descriptionText = applicationContext.getString(R.string.notification_channel_description)
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun showNotification(title: String, message: String) {
        val intent = Intent(applicationContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent: PendingIntent = PendingIntent.getActivity(
            applicationContext,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val builder = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher) // Uses the main launcher icon
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        with(NotificationManagerCompat.from(applicationContext)) {
            // Check for POST_NOTIFICATIONS permission before notifying
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (applicationContext.checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) == android.content.pm.PackageManager.PERMISSION_GRANTED) {
                    notify(NOTIFICATION_ID, builder.build())
                } else {
                    // Permission not granted, do not show notification
                    // Log a message or handle gracefully
                    // Log.w("NotificationWorker", "POST_NOTIFICATIONS permission not granted.")
                }
            } else {
                // For older Android versions, permission is granted at install time
                notify(NOTIFICATION_ID, builder.build())
            }
        }
    }
}
