package com.harvey.gamespc.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.harvey.gamespc.R

class MyFirebaseMessagingService : FirebaseMessagingService() {

    /**
     * Se llama cuando se recibe un mensaje mientras la app está en primer plano.
     */
    override fun onMessageReceived(remoteMessage: RemoteMessage) {


        // Los mensajes de notificación son manejados automáticamente por el SDK de FCM
        // cuando la app está en segundo plano. Aquí manejamos los que llegan en primer plano
        // o los mensajes de datos.

        remoteMessage.notification?.let {
            sendNotification(it.title, it.body)
        }
    }

    /**
     * Crea y muestra una notificación simple.
     */
    private fun sendNotification(title: String?, messageBody: String?) {
        val channelId = "general_notifications_channel"
        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.mipmap.ic_launcher) // Usar el icono de la app como fallback
            .setContentTitle(title ?: "GamesPC")
            .setContentText(messageBody ?: "Tienes una nueva notificación.")
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)

        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        // Se necesita un NotificationChannel para Android 8.0 (API 26) y superior.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Notificaciones Generales",
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(channel)
        }

        // Muestra la notificación
        notificationManager.notify(0 /* ID de la notificación */, notificationBuilder.build())
    }

    /**
     * Se llama cuando se genera un nuevo token de FCM para el dispositivo.
     * Aquí podrías guardar el token en tu servidor si tuvieras un sistema de usuarios.
     */
    override fun onNewToken(token: String) {

        // Por ahora, solo lo registramos. En un futuro, se podría enviar a un servidor.
        // Log.d("FCM", "New token: $token")
    }
}
