package com.learn.behindsee2.ui.theme

import com.google.firebase.Timestamp

data class Message(
    val senderId: String = "",
    val messageText: String = "",
    val timestamp: Timestamp? = null,
    val timeString: String = ""
)

data class ChatRoom(
    val roomId: String = "",
    val senderId: String = "",
    val receiverId: String = "",
    val participants: List<String> = emptyList(), // CRITICAL for querying
    val lastMessage: String = "",
    val timestamp: Timestamp? = null,
    val isSenderTyping: Boolean = false,
    val isReceiverTyping: Boolean = false,
    var otherUserName: String = "مستخدم خلف الستار",
    var otherUserImage: String? = null
)
