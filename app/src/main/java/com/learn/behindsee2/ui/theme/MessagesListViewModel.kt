package com.learn.behindsee2.ui.theme

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query

class MessagesListViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private var roomsListener: ListenerRegistration? = null

    val roomsList = mutableStateListOf<ChatRoom>()
    var isLoading by mutableStateOf(false)

    private val usersCache = mutableMapOf<String, Pair<String, String?>>()

    fun listenForRooms() {
        val currentUserId = auth.currentUser?.uid ?: return
        if (roomsList.isEmpty()) isLoading = true

        // 🟢 FIX: Close previous listener if exists to prevent leaks
        roomsListener?.remove()

        // 🟢 FIX: Using 'participants' array for a reliable and efficient query
        roomsListener = db.collection("chat_rooms")
            .whereArrayContains("participants", currentUserId)
            .addSnapshotListener { snapshot, error ->
                isLoading = false
                if (error != null) return@addSnapshotListener

                if (snapshot != null) {
                    processSnapshot(snapshot, currentUserId)
                }
            }
    }

    private fun processSnapshot(snapshot: com.google.firebase.firestore.QuerySnapshot, currentUserId: String) {
        val rooms = snapshot.toObjects(ChatRoom::class.java)
        val sortedRooms = rooms.sortedByDescending { room -> room.timestamp ?: com.google.firebase.Timestamp.now() }
        updateRoomsWithCacheAndFetch(sortedRooms, currentUserId)
    }

    private fun updateRoomsWithCacheAndFetch(newRooms: List<ChatRoom>, currentUserId: String) {
        val updatedList = newRooms.map { room ->
            val otherId = if (room.senderId == currentUserId) room.receiverId else room.senderId
            if (usersCache.containsKey(otherId)) {
                room.copy(
                    otherUserName = usersCache[otherId]?.first ?: "مستخدم",
                    otherUserImage = usersCache[otherId]?.second
                )
            } else {
                fetchSingleUserDetails(otherId)
                room
            }
        }
        roomsList.clear()
        roomsList.addAll(updatedList)
    }

    private fun fetchSingleUserDetails(otherId: String) {
        if (otherId.isEmpty()) return
        db.collection("users").document(otherId).get().addOnSuccessListener { doc ->
            if (doc.exists()) {
                val name = doc.getString("name") ?: "مستخدم"
                val image = doc.getString("profileImageUrl")
                usersCache[otherId] = Pair(name, image)
                roomsList.forEachIndexed { index, room ->
                    val id = if (room.senderId == auth.currentUser?.uid) room.receiverId else room.senderId
                    if (id == otherId) {
                        roomsList[index] = room.copy(otherUserName = name, otherUserImage = image)
                    }
                }
            }
        }
    }

    // 🟢 CRITICAL: Cleanup listener on ViewModel destruction
    override fun onCleared() {
        super.onCleared()
        roomsListener?.remove()
    }
}
