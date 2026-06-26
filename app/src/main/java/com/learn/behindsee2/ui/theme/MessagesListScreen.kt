package com.learn.behindsee2.ui.theme

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.learn.behindsee2.navigation.BottomNavigationBar
import com.learn.behindsee2.navigation.Screen
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MessagesListScreen(
    viewModel: MessagesListViewModel = viewModel(),
    onChatClick: (roomId: String, currentUserId: String, receiverId: String, receiverName: String, imageUrl: String?) -> Unit,
    onBackClick: () -> Unit,
    onNavigateToHome: () -> Unit = {},
    onNavigateToAddProperty: () -> Unit = {},
    onNavigateToProfile: () -> Unit = {}
) {
    val currentUserId = remember { FirebaseAuth.getInstance().currentUser?.uid ?: "" }
    val rooms = viewModel.roomsList
    val isLoading = viewModel.isLoading
    val context = LocalContext.current
    
    var userRole by remember { mutableStateOf("buyer") }
    LaunchedEffect(currentUserId) {
        if (currentUserId.isNotEmpty()) {
            FirebaseFirestore.getInstance().collection("users").document(currentUserId)
                .get().addOnSuccessListener { userRole = it.getString("role") ?: "buyer" }
        }
    }

    LaunchedEffect(Unit) {
        viewModel.listenForRooms()
    }

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Scaffold(
            modifier = Modifier.fillMaxSize().statusBarsPadding(),
            topBar = {
                TopAppBar(
                    title = { Text("المحادثات", fontWeight = FontWeight.Bold, color = Color(0xFF004D61)) },
                    navigationIcon = {
                        IconButton(onClick = onBackClick) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "رجوع",
                                tint = Color(0xFF004D61)
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
                )
            },
            bottomBar = {
                BottomNavigationBar(
                    userRole = userRole,
                    currentRoute = Screen.MessagesList.route,
                    onHomeClick = onNavigateToHome,
                    onAddClick = {
                        if (userRole == "seller" || userRole == "تاجر") {
                            onNavigateToAddProperty()
                        } else {
                            Toast.makeText(context, "هذا حساب مستأجر، لا يمكن إضافة عرض إلا من حساب تاجر", Toast.LENGTH_LONG).show()
                        }
                    },
                    onMessagesClick = { /* Already here */ },
                    onProfileClick = onNavigateToProfile
                )
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(Color(0xFFF4F6F9))
            ) {
                if (isLoading && rooms.isEmpty()) {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = Color(0xFF004D61)
                    )
                } else if (rooms.isEmpty()) {
                    Text(
                        text = "لا توجد محادثات حالياً",
                        modifier = Modifier.align(Alignment.Center),
                        color = Color.Gray
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(rooms) { room ->
                            ChatRoomItem(
                                room = room,
                                currentUserId = currentUserId,
                                onClick = {
                                    val otherId = if (room.senderId == currentUserId) room.receiverId else room.senderId
                                    onChatClick(room.roomId, currentUserId, otherId, room.otherUserName, room.otherUserImage)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ChatRoomItem(
    room: ChatRoom,
    currentUserId: String,
    onClick: () -> Unit
) {
    val formattedTime = remember(room.timestamp) {
        room.timestamp?.toDate()?.let { date ->
            SimpleDateFormat("hh:mm a", Locale("ar")).format(date)
        } ?: ""
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (!room.otherUserImage.isNullOrBlank() && room.otherUserImage != "no_image") {
                Image(
                    painter = rememberAsyncImagePainter(room.otherUserImage),
                    contentDescription = null,
                    modifier = Modifier
                        .size(55.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(55.dp)
                        .background(Color(0xFF004D61).copy(alpha = 0.1f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = Color(0xFF004D61),
                        modifier = Modifier.size(30.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = room.otherUserName,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = room.lastMessage,
                    fontSize = 14.sp,
                    color = Color.Gray,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            if (formattedTime.isNotBlank()) {
                Text(
                    text = formattedTime,
                    fontSize = 11.sp,
                    color = Color.LightGray,
                    modifier = Modifier.align(Alignment.Top)
                )
            }
        }
    }
}
