package com.harvey.gamespc.notifications

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import com.harvey.gamespc.MainActivity
import com.harvey.gamespc.R
import com.harvey.gamespc.data.repository.ChatRepository
import com.harvey.gamespc.ui.screens.Message
import com.harvey.gamespc.utils.AnonymousIdManager
import com.harvey.gamespc.utils.ChatUiState
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

/**
 * PLAN B (sin Cloud Functions ni plan Blaze): escucha en vivo el nodo "chat" de
 * Realtime Database y muestra una notificación local por cada mensaje nuevo de
 * otro usuario.
 *
 * Funciona mientras el proceso de la app esté vivo (primer plano o segundo
 * plano). NO funciona si la app está completamente cerrada/eliminada de
 * memoria, porque no hay push externo.
 *
 * Para no notificar todo el historial, la primera emisión tras arrancar solo
 * registra los IDs existentes; a partir de ahí se notifica cada mensaje nuevo.
 *
 * NOTA: si algún día despliegas la Cloud Function (plan Blaze), desactiva este
 * manager para no recibir notificaciones duplicadas (FCM + local).
 */
@Singleton
class ChatNotificationManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val repository: ChatRepository,
    private val anonymousIdManager: AnonymousIdManager
) {
    companion object {
        private const val CHANNEL_ID = "chat_notifications_channel"
        private const val PREFS_NAME = "AppPrefs"
        private const val KEY_PROCESSED_IDS = "processed_chat_message_ids"
        private const val MAX_PROCESSED = 300
    }

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val processedIds = LinkedHashSet<String>()
    private var firstSeedDone = false

    private val prefs
        get() = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    /** Se llama desde MyApplication.onCreate: empieza a escuchar el chat. */
    fun start() {
        loadProcessedIds()
        scope.launch {
            repository.getMessages().collect { messages ->
                handleNewMessages(messages)
            }
        }
    }

    private fun handleNewMessages(messages: List<Message>) {
        // Primera emisión tras arrancar: solo se registran los IDs existentes
        // para no notificar todo el historial de golpe.
        if (!firstSeedDone) {
            messages.forEach { processedIds.add(it.id) }
            trimProcessedIds()
            persistProcessedIds()
            firstSeedDone = true
            return
        }

        var changed = false
        for (message in messages) {
            if (message.id in processedIds) continue
            processedIds.add(message.id)
            changed = true
            trimProcessedIds()

            // No notificar los mensajes enviados por este mismo dispositivo
            if (message.userId == anonymousIdManager.getAnonymousId()) continue
            // No notificar mientras la pantalla de chat está abierta
            if (ChatUiState.chatScreenVisible) continue

            showNotification(message)
        }
        if (changed) persistProcessedIds()
    }

    private fun showNotification(message: Message) {
        // En Android 13+ la notificación no se muestra sin permiso
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        val contentIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(ChatUiState.EXTRA_OPEN_CHAT, true)
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            message.id.hashCode(),
            contentIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(message.senderName.ifBlank { "Anónimo" })
            .setContentText(message.text)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message.text))
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)

        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationManager.createNotificationChannel(
                NotificationChannel(CHANNEL_ID, "Mensajes del chat", NotificationManager.IMPORTANCE_HIGH)
            )
        }

        // ID único por mensaje para que no se sobrescriban entre sí
        notificationManager.notify(message.id.hashCode(), builder.build())
    }

    private fun loadProcessedIds() {
        val saved = prefs.getStringSet(KEY_PROCESSED_IDS, emptySet()) ?: emptySet()
        processedIds.addAll(saved)
    }

    private fun persistProcessedIds() {
        prefs.edit().putStringSet(KEY_PROCESSED_IDS, LinkedHashSet(processedIds)).apply()
    }

    private fun trimProcessedIds() {
        while (processedIds.size > MAX_PROCESSED) {
            processedIds.remove(processedIds.first())
        }
    }
}
