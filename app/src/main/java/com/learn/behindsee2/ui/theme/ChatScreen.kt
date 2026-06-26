package com.learn.behindsee2.ui.theme

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    roomId: String,
    currentUserId: String,
    receiverId: String,
    receiverName: String,
    receiverImageUrl: String? = null, // التحسين رقم 4: رابط الصورة الدائرية
    chatViewModel: ChatViewModel = viewModel(),
    onBackClick: () -> Unit = {}
) {
    var typedMessage by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    LaunchedEffect(roomId) {
        chatViewModel.listenForChat(roomId, currentUserId)
    }

    // التحسين رقم 3: التمرير التلقائي التلقائي لآخر رسالة عند وصول رسائل جديدة
    LaunchedEffect(chatViewModel.messagesList.size) {
        if (chatViewModel.messagesList.isNotEmpty()) {
            listState.animateScrollToItem(chatViewModel.messagesList.size - 1)
        }
    }

    // مراقبة حقل النص لتحديث حالة الكتابة (Typing...) في الفايربيس تلقائياً
    LaunchedEffect(typedMessage) {
        chatViewModel.updateTypingStatus(roomId, currentUserId, typedMessage.isNotBlank())
    }

    ChatContent(
        receiverName = receiverName,
        receiverImageUrl = receiverImageUrl,
        isOtherUserTyping = chatViewModel.isOtherUserTyping,
        messagesList = chatViewModel.messagesList,
        currentUserId = currentUserId,
        typedMessage = typedMessage,
        onTypedMessageChange = { typedMessage = it },
        onBackClick = onBackClick,
        onSendMessage = {
            if (typedMessage.trim().isNotEmpty()) {
                chatViewModel.sendMessage(
                    roomId = roomId,
                    senderId = currentUserId,
                    receiverId = receiverId,
                    text = typedMessage
                )
                typedMessage = ""
            }
        },
        listState = listState
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatContent(
    receiverName: String,
    receiverImageUrl: String?,
    isOtherUserTyping: Boolean,
    messagesList: List<Message>,
    currentUserId: String,
    typedMessage: String,
    onTypedMessageChange: (String) -> Unit,
    onBackClick: () -> Unit,
    onSendMessage: () -> Unit,
    listState: LazyListState = rememberLazyListState()
) {
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            // التحسين رقم 4: عرض صورة دائرية للطرف الآخر أو أيقونة افتراضية
                            if (!receiverImageUrl.isNullOrBlank()) {
                                Image(
                                    painter = rememberAsyncImagePainter(receiverImageUrl),
                                    contentDescription = "صورة الملف الشخصي",
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .background(Color.LightGray, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.Person, contentDescription = null, tint = Color.White)
                                }
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column {
                                Text(text = receiverName, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.Black)
                                // التحسين رقم 5: عرض "يكتب الآن..." تحت الاسم بشكل ديناميكي
                                if (isOtherUserTyping) {
                                    Text(text = "يكتب الآن...", fontSize = 12.sp, color = Color(0xFF004D61), fontWeight = FontWeight.Medium)
                                }
                            }
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = onBackClick) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "رجوع")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
                )
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(Color(0xFFF4F6F9))
            ) {
                // 1️⃣ قائمة عرض الرسائل المعدلة بالكامل
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(messagesList) { message ->
                        // التحسين رقم 1: المستخدم الحالي يمين (أزرق)، الطرف الآخر يسار (رمادي)
                        val isMyMessage = message.senderId == currentUserId

                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = if (isMyMessage) Alignment.CenterStart else Alignment.CenterEnd
                        ) {
                            Column(horizontalAlignment = if (isMyMessage) Alignment.End else Alignment.Start) {
                                Card(
                                    shape = RoundedCornerShape(
                                        topStart = 12.dp,
                                        topEnd = 12.dp,
                                        bottomStart = if (isMyMessage) 12.dp else 0.dp,
                                        bottomEnd = if (isMyMessage) 0.dp else 12.dp
                                    ),
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (isMyMessage) Color(0xFFE5E5EA) else Color(0xFF007AFF)
                                    ),
                                    modifier = Modifier.widthIn(max = 280.dp)
                                ) {
                                    Text(
                                        text = message.messageText,
                                        color = if (isMyMessage) Color.Black else Color.White,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                        fontSize = 15.sp
                                    )
                                }
                                // التحسين رقم 2: عرض وقت الرسالة بصيغة صغيرة أسفل الفقاعة
                                if (message.timeString.isNotBlank()) {
                                    Text(
                                        text = message.timeString,
                                        fontSize = 11.sp,
                                        color = Color.Gray,
                                        modifier = Modifier.padding(start = 4.dp, end = 4.dp, top = 2.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // 2️⃣ شريط الإدخال المطور
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White)
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = typedMessage,
                        onValueChange = onTypedMessageChange,
                        placeholder = { Text("اكتب رسالتك هنا...") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(24.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF004D61)
                        )
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    IconButton(
                        onClick = onSendMessage,
                        modifier = Modifier.background(Color(0xFF004D61), shape = RoundedCornerShape(50))
                    ) {
                        Icon(imageVector = Icons.Default.Send, contentDescription = "إرسال", tint = Color.White)
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ChatScreenPreview() {
    Behindsee2Theme {
        ChatContent(
            receiverName = "محمد علي",
            receiverImageUrl = null,
            isOtherUserTyping = true,
            messagesList = listOf(
                Message(senderId = "1", messageText = "السلام عليكم", timeString = "10:00"),
                Message(senderId = "2", messageText = "وعليكم السلام، كيف حالك؟", timeString = "10:01"),
                Message(senderId = "1", messageText = "بخير الحمد لله، وأنت؟", timeString = "10:02")
            ),
            currentUserId = "1",
            typedMessage = "أنا بخير",
            onTypedMessageChange = {},
            onBackClick = {},
            onSendMessage = {}
        )
    }
}
