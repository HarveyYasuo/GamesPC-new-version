package com.harvey.gamespc.ui.screens

import androidx.lifecycle.ViewModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

enum class MessageStatus {
    SENT, DELIVERED, READ
}

data class Message(
    val userId: String = "",
    val senderName: String = "",
    val text: String = "",
    val timestamp: Long = 0L,
    val status: MessageStatus = MessageStatus.SENT
)

class ChatViewModel : ViewModel() {
    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages: StateFlow<List<Message>> = _messages.asStateFlow()

    private val database = FirebaseDatabase.getInstance()
    private val chatRef = database.getReference("chat")

    val currentUserId: String = UUID.randomUUID().toString()

    private val messageListener = object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            val messageList = mutableListOf<Message>()
            for (messageSnapshot in snapshot.children) {
                val message = messageSnapshot.getValue(Message::class.java)
                message?.let { messageList.add(it) }
            }
            _messages.value = messageList.sortedBy { it.timestamp }
        }

        override fun onCancelled(error: DatabaseError) {
            // Handle error
        }
    }

    init {
        chatRef.addValueEventListener(messageListener)
    }

    fun sendMessage(text: String) {
        val anonymousName = "Anon-" + currentUserId.substring(0, 5)
        val newMessage = Message(currentUserId, anonymousName, text, System.currentTimeMillis(), MessageStatus.SENT)
        chatRef.push().setValue(newMessage)
    }

    override fun onCleared() {
        super.onCleared()
        chatRef.removeEventListener(messageListener)
    }
}