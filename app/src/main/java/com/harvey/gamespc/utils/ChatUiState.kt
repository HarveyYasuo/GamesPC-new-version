package com.harvey.gamespc.utils

/**
 * Estado de UI compartido entre la pantalla de chat y el servicio de
 * notificaciones (MyFirebaseMessagingService), para no notificar mensajes
 * del chat mientras el usuario está viendo la pantalla de chat.
 */
object ChatUiState {
    @Volatile
    var chatScreenVisible: Boolean = false

    /** Extra del Intent para que la notificación del chat abra la pestaña Mensajes. */
    const val EXTRA_OPEN_CHAT = "open_chat"
}
