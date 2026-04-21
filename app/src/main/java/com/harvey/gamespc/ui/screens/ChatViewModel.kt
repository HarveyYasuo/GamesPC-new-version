package com.harvey.gamespc.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.harvey.gamespc.data.repository.ChatRepository
import com.harvey.gamespc.utils.AnonymousIdManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class MessageStatus {
    SENT, DELIVERED, READ
}

data class Message(
    val id: String = "",
    val userId: String = "",
    val senderName: String = "",
    val text: String = "",
    val timestamp: Long = 0L,
    val status: MessageStatus = MessageStatus.SENT
)

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val repository: ChatRepository,
    private val anonymousIdManager: AnonymousIdManager
) : ViewModel() {
    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages: StateFlow<List<Message>> = _messages.asStateFlow()

    private val _typingUsers = MutableStateFlow<List<String>>(emptyList())
    val typingUsers: StateFlow<List<String>> = _typingUsers.asStateFlow()

    val currentUserId: String = anonymousIdManager.getAnonymousId()
    private val anonymousName = "Anon-" + currentUserId.substring(0, 5)

    init {
        viewModelScope.launch {
            repository.getMessages().collect { list ->
                _messages.value = list
                // Marcar como leídos los mensajes que NO son del usuario actual y que aún no están en READ
                list.filter { it.userId != currentUserId && it.status != MessageStatus.READ }
                    .forEach { message ->
                        repository.markMessageAsRead(message.id)
                    }
            }
        }

        viewModelScope.launch {
            repository.getTypingUsers(currentUserId).collect {
                _typingUsers.value = it
            }
        }
    }

    fun onTyping(isTyping: Boolean) {
        repository.setTypingStatus(currentUserId, anonymousName, isTyping)
    }

    fun sendMessage(text: String) {
        val newMessage = Message(
            id = "", // Firebase generará el ID
            userId = currentUserId,
            senderName = anonymousName,
            text = text,
            timestamp = System.currentTimeMillis(),
            status = MessageStatus.SENT
        )
        repository.sendMessage(newMessage)
        // Dejar de escribir al enviar
        onTyping(false)
    }

    override fun onCleared() {
        super.onCleared()
        // Asegurarse de limpiar el estado de escritura al salir
        onTyping(false)
    }
}
