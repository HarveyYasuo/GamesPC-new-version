package com.harvey.gamespc.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import com.harvey.gamespc.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(chatViewModel: ChatViewModel = hiltViewModel()) {
    var messageText by remember { mutableStateOf("") }
    val messages by chatViewModel.messages.collectAsState()
    val currentUserId = chatViewModel.currentUserId
    val listState = rememberLazyListState()

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFF8FAFC),
                        Color(0xFFEBF8FF)
                    )
                )
            )
    ) {
        // Messages
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            items(messages) { message ->
                MessageItem(message = message, currentUserId = currentUserId)
            }
        }

        // Input
        Surface(
            color = Color.White.copy(alpha = 0.95f),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.Bottom
            ) {
                IconButton(
                    onClick = { /* Handle attachment */ }
                ) {
                    Icon(
                        Icons.Default.Attachment,
                        contentDescription = "Adjuntar",
                        tint = Color(0xFF6B7280)
                    )
                }

                OutlinedTextField(
                    value = messageText,
                    onValueChange = { messageText = it },
                    modifier = Modifier.weight(1f),
                    placeholder = {
                        Text(
                            stringResource(R.string.chat_input_placeholder),
                            color = Color(0xFF9CA3AF)
                        )
                    },
                    shape = RoundedCornerShape(24.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF3B82F6),
                        unfocusedBorderColor = Color(0xFFE5E7EB),
                        focusedContainerColor = Color(0xFFF9FAFB),
                        unfocusedContainerColor = Color(0xFFF9FAFB)
                    )
                )

                Spacer(modifier = Modifier.width(8.dp))

                IconButton(
                    onClick = { /* Handle emoji */ }
                ) {
                    Icon(
                        Icons.Default.EmojiEmotions,
                        contentDescription = "Emoji",
                        tint = Color(0xFF6B7280)
                    )
                }

                FloatingActionButton(
                    onClick = {
                        if (messageText.isNotBlank()) {
                            chatViewModel.sendMessage(messageText)
                            messageText = ""
                        }
                    },
                    modifier = Modifier.size(48.dp),
                    containerColor = if (messageText.isBlank()) {
                        Color(0xFFE5E7EB)
                    } else {
                        Color(0xFF3B82F6)
                    }
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.Send,
                        contentDescription = stringResource(R.string.chat_send_button),
                        tint = if (messageText.isBlank()) {
                            Color(0xFF9CA3AF)
                        } else {
                            Color.White
                        }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MessageItem(message: Message, currentUserId: String) {
    val isCurrentUser = message.userId == currentUserId
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isCurrentUser) {
            Arrangement.End
        } else {
            Arrangement.Start
        }
    ) {
        Surface(
            modifier = Modifier.widthIn(max = 280.dp),
            shape = RoundedCornerShape(
                topStart = 20.dp,
                topEnd = 20.dp,
                bottomStart = if (isCurrentUser) 20.dp else 8.dp,
                bottomEnd = if (isCurrentUser) 8.dp else 20.dp
            ),
            color = if (isCurrentUser) {
                Color(0xFF3B82F6)
            } else {
                Color.White
            },
            shadowElevation = 2.dp
        ) {
            Column(
                modifier = Modifier.padding(12.dp)
            ) {
                Text(
                    text = message.senderName,
                    color = if (isCurrentUser) {
                        Color.White.copy(alpha = 0.7f)
                    } else {
                        Color(0xFF1F2937).copy(alpha = 0.7f)
                    },
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = message.text,
                    color = if (isCurrentUser) {
                        Color.White
                    } else {
                        Color(0xFF1F2937)
                    },
                    fontSize = 14.sp,
                    lineHeight = 20.sp
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Row(
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(message.timestamp)),
                        fontSize = 11.sp,
                        color = if (isCurrentUser) {
                            Color.White.copy(alpha = 0.7f) 
                        } else {
                            Color(0xFF9CA3AF)
                        }
                    )
                    
                    if (isCurrentUser) {
                        Spacer(modifier = Modifier.width(4.dp))
                        MessageStatusIndicator(status = message.status)
                    }
                }
            }
        }
    }
}

@Composable
fun MessageStatusIndicator(status: MessageStatus) {
    val color = if (status == MessageStatus.READ) Color(0xFF10B981) else Color.White.copy(alpha = 0.6f)
    val icon = if (status == MessageStatus.READ || status == MessageStatus.DELIVERED) {
        Icons.Default.DoneAll
    } else {
        Icons.Default.Done
    }

    Icon(
        imageVector = icon,
        contentDescription = status.name,
        modifier = Modifier.size(16.dp),
        tint = color
    )
}