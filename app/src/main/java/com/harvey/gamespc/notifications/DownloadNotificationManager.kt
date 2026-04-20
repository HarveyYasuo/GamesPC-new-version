package com.harvey.gamespc.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.harvey.gamespc.R

object DownloadNotificationManager {

    private const val CHANNEL_ID = "download_channel"

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Descargas"
            val descriptionText = "Notificaciones sobre el progreso de las descargas"
            val importance = NotificationManager.IMPORTANCE_LOW
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun showDownloadNotification(context: Context, fileName: String, progress: Int, downloadUrl: String) {
        val cancelIntent = Intent("com.harvey.gamespc.CANCEL_DOWNLOAD").apply {
            putExtra("url", downloadUrl)
        }
        val cancelPendingIntent = PendingIntent.getBroadcast(
            context,
            downloadUrl.hashCode(),
            cancelIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.stat_sys_download)
            .setContentTitle(fileName)
            .setContentText("Descargando... $progress%")
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .setProgress(100, progress, false)
            .addAction(android.R.drawable.ic_menu_close_clear_cancel, "Cancelar", cancelPendingIntent)

        with(NotificationManagerCompat.from(context)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (context.checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) == android.content.pm.PackageManager.PERMISSION_GRANTED) {
                    notify(downloadUrl.hashCode(), builder.build())
                }
            } else {
                notify(downloadUrl.hashCode(), builder.build())
            }
        }
    }

    fun showDownloadComplete(context: Context, fileName: String, downloadUrl: String) {
        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.stat_sys_download_done)
            .setContentTitle(fileName)
            .setContentText("Descarga completa")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)

        with(NotificationManagerCompat.from(context)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (context.checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) == android.content.pm.PackageManager.PERMISSION_GRANTED) {
                    notify(downloadUrl.hashCode(), builder.build())
                }
            } else {
                notify(downloadUrl.hashCode(), builder.build())
            }
        }
    }

    fun showDownloadFailed(context: Context, fileName: String, downloadUrl: String) {
        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_warning)
            .setContentTitle(fileName)
            .setContentText("Error en la descarga")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)

        with(NotificationManagerCompat.from(context)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (context.checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) == android.content.pm.PackageManager.PERMISSION_GRANTED) {
                    notify(downloadUrl.hashCode(), builder.build())
                }
            } else {
                notify(downloadUrl.hashCode(), builder.build())
            }
        }
    }
    
    fun cancelNotification(context: Context, downloadUrl: String) {
        with(NotificationManagerCompat.from(context)) {
            cancel(downloadUrl.hashCode())
        }
    }
}
