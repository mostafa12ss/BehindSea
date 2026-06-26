package com.learn.behindsee2.ui.theme

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.Date

class ChatViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    private var messagesListener: ListenerRegistration? = null
    private var typingListener: ListenerRegistration? = null
    
    val messagesList = mutableStateListOf<Message>()
    var isOtherUserTyping by mutableStateOf(false)

    fun createRoomId(userId1: String, userId2: String): String {
        return if (userId1 < userId2) "${userId1}_${userId2}" else "${userId2}_${userId1}"
    }

    fun updateTypingStatus(roomId: String, currentUserId: String, isTyping: Boolean) {
        db.collection("chat_rooms").document(roomId).get().addOnSuccessListener { doc ->
            if (doc.exists()) {
                val senderId = doc.getString("senderId") ?: ""
                val field = if (currentUserId == senderId) "isSenderTyping" else "isReceiverTyping"
                db.collection("chat_rooms").document(roomId).update(field, isTyping)
            }
        }
    }

    fun listenForChat(roomId: String, currentUserId: String) {
        // Cleanup existing listeners if any
        messagesListener?.remove()
        typingListener?.remove()

        // 1. Listen for messages
        messagesListener = db.collection("chat_rooms")
            .document(roomId)
            .collection("messages")
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) return@addSnapshotListener
                if (snapshot != null) {
                    messagesList.clear()
                    for (doc in snapshot.documents) {
                        val message = doc.toObject(Message::class.java)
                        if (message != null) messagesList.add(message)
                    }
                }
            }

        // 2. Listen for other user's typing status
        typingListener = db.collection("chat_rooms").document(roomId).addSnapshotListener { snapshot, _ ->
            if (snapshot != null && snapshot.exists()) {
                val senderId = snapshot.getString("senderId") ?: ""
                val isSenderTyping = snapshot.getBoolean("isSenderTyping") ?: false
                val isReceiverTyping = snapshot.getBoolean("isReceiverTyping") ?: false

                isOtherUserTyping = if (currentUserId == senderId) isReceiverTyping else isSenderTyping
            }
        }
    }

    fun sendMessage(roomId: String, senderId: String, receiverId: String, text: String) {
        val cleanText = text.trim()
        if (cleanText.isEmpty()) return

        val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
        val currentTimeString = sdf.format(Date())

        val messageData = Message(
            senderId = senderId,
            messageText = cleanText,
            timestamp = Timestamp.now(),
            timeString = currentTimeString
        )

        db.collection("chat_rooms")
            .document(roomId)
            .collection("messages")
            .add(messageData)

        val roomData = mapOf(
            "roomId" to roomId,
            "senderId" to senderId,
            "receiverId" to receiverId,
            "participants" to listOf(senderId, receiverId), // Ensure participants are set
            "lastMessage" to cleanText,
            "timestamp" to Timestamp.now()
        )
        db.collection("chat_rooms").document(roomId).set(roomData, com.google.firebase.firestore.SetOptions.merge())
    }

    override fun onCleared() {
        super.onCleared()
        messagesListener?.remove()
        typingListener?.remove()
    }
}
