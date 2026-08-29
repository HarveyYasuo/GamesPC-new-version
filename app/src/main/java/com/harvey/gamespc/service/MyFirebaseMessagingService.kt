package com.harvey.gamespc.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.harvey.gamespc.MainActivity
import com.harvey.gamespc.R
import com.harvey.gamespc.utils.AnonymousIdManager
import com.harvey.gamespc.utils.ChatUiState

class MyFirebaseMessagingService : FirebaseMessagingService() {

    companion object {
        private const val GENERAL_CHANNEL_ID = "general_notifications_channel"
        private const val CHAT_CHANNEL_ID = "chat_notifications_channel"
        // Debe coincidir con el tema usado en la Cloud Function y en MyApplication
        private const val CHAT_TOPIC = "general_chat"
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        val data = remoteMessage.data

        // Mensajes de datos del chat general (enviados por la Cloud Function)
        if (data["type"] == "chat") {
            handleChatMessage(data)
            return
        }

        // Mensajes de notificación clásicos: en primer plano los mostramos aquí;
        // en segundo plano el sistema los muestra automáticamente.
        remoteMessage.notification?.let {
            sendNotification(GENERAL_CHANNEL_ID, "Notificaciones Generales", it.title, it.body)
        }
    }

    /**
     * Muestra la notificación de un mensaje del chat general.
     * Ignora los mensajes enviados por este mismo dispositivo y los que
     * llegan mientras la pantalla de chat está abierta.
     */
    private fun handleChatMessage(data: Map<String, String>) {
        val senderId = data["senderId"].orEmpty()
        if (senderId == AnonymousIdManager(applicationContext).getAnonymousId()) return
        if (ChatUiState.chatScreenVisible) return

        val senderName = data["senderName"]?.takeIf { it.isNotBlank() } ?: "Anónimo"
        val text = data["text"].orEmpty()
        if (text.isBlank()) return
        val messageId = data["messageId"].orEmpty()

        val contentIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(ChatUiState.EXTRA_OPEN_CHAT, true)
        }
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            contentIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notificationBuilder = NotificationCompat.Builder(this, CHAT_CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(senderName)
            .setContentText(text)
            .setStyle(NotificationCompat.BigTextStyle().bigText(text))
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)

        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        // Se necesita un NotificationChannel para Android 8.0 (API 26) y superior.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHAT_CHANNEL_ID,
                "Mensajes del chat",
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(channel)
        }

        // ID único por mensaje para que no se sobrescriban entre sí
        notificationManager.notify(messageId.hashCode(), notificationBuilder.build())
    }

    /**
     * Crea y muestra una notificación simple.
     */
    private fun sendNotification(channelId: String, channelName: String, title: String?, messageBody: String?) {
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
                channelName,
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(channel)
        }

        // Muestra la notificación
        notificationManager.notify(0 /* ID de la notificación */, notificationBuilder.build())
    }

    /**
     * Se llama cuando se genera un nuevo token de FCM para el dispositivo.
     * Hay que renovar la suscripción al tema del chat para que las notificaciones
     * sigan llegando con el token nuevo.
     */
    override fun onNewToken(token: String) {
        FirebaseMessaging.getInstance().subscribeToTopic(CHAT_TOPIC)
    }
}
