package com.learn.behindsee2.ui.theme

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.compose.rememberAsyncImagePainter
import com.learn.behindsee2.navigation.BottomNavigationBar
import com.learn.behindsee2.navigation.Screen
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ProfileScreen(
    profileViewModel: ProfileViewModel = viewModel(),
    onBackClick: () -> Unit = {},
    onNavigateToHome: () -> Unit = {},
    onNavigateToMessages: () -> Unit = {},
    onNavigateToAddProperty: () -> Unit = {}
) {
    ProfileScreenContent(
        userProfile = profileViewModel.userProfile,
        isLoading = profileViewModel.isLoading,
        isSaving = profileViewModel.isSaving,
        isUploadingImage = profileViewModel.isUploadingImage,
        statusMessage = profileViewModel.statusMessage,
        accountCreationDate = profileViewModel.accountCreationDate,
        myProperties = profileViewModel.myProperties,
        incomingBookings = profileViewModel.incomingBookings,
        myBookings = profileViewModel.myBookings,
        onBackClick = onBackClick,
        onNavigateToHome = onNavigateToHome,
        onNavigateToMessages = onNavigateToMessages,
        onNavigateToAddProperty = onNavigateToAddProperty,
        onUploadImage = { profileViewModel.uploadProfileImage(it) },
        onSaveName = { profileViewModel.updateUserName(it) { } },
        onUpdatePaymentInfo = { vc, ip -> profileViewModel.updatePaymentInfo(vc, ip) },
        onDeleteProperty = { profileViewModel.deleteProperty(it) },
        onUpdateBookingStatus = { id, status -> profileViewModel.updateBookingStatus(id, status) },
        onStatusMessageShown = { profileViewModel.statusMessage = "" }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreenContent(
    userProfile: UserProfile?,
    isLoading: Boolean,
    isSaving: Boolean,
    isUploadingImage: Boolean,
    statusMessage: String,
    accountCreationDate: Long?,
    myProperties: List<PropertyData>,
    incomingBookings: List<PaymentRequest>,
    myBookings: List<PaymentRequest>,
    onBackClick: () -> Unit,
    onNavigateToHome: () -> Unit,
    onNavigateToMessages: () -> Unit,
    onNavigateToAddProperty: () -> Unit,
    onUploadImage: (Uri) -> Unit,
    onSaveName: (String) -> Unit,
    onUpdatePaymentInfo: (String, String) -> Unit,
    onDeleteProperty: (String) -> Unit,
    onUpdateBookingStatus: (String, String) -> Unit,
    onStatusMessageShown: () -> Unit
) {
    var nameInput by remember { mutableStateOf("") }
    var vodafoneCash by remember { mutableStateOf("") }
    var instaPay by remember { mutableStateOf("") }
    var selectedBookingScreenshot by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current

    val snackbarHostState = remember { SnackbarHostState() }
    val isNameTooShort = nameInput.trim().isNotEmpty() && nameInput.trim().length < 3
    val isDataChanged = nameInput.trim().isNotEmpty() &&
            nameInput.trim() != userProfile?.name &&
            nameInput.trim().length >= 3

    LaunchedEffect(userProfile) {
        if (userProfile != null) {
            if (nameInput.isEmpty()) nameInput = userProfile.name
            vodafoneCash = userProfile.vodafoneCash
            instaPay = userProfile.instaPay
        }
    }

    LaunchedEffect(statusMessage) {
        if (statusMessage.isNotEmpty()) {
            snackbarHostState.showSnackbar(message = statusMessage, duration = SnackbarDuration.Short)
            onStatusMessageShown()
        }
    }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? -> uri?.let { onUploadImage(it) } }

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Scaffold(
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
            topBar = {
                TopAppBar(
                    title = { Text(text = "الملف الشخصي", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color(0xFF004D61)) },
                    navigationIcon = {
                        IconButton(onClick = onBackClick) {
                            Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "رجوع", tint = Color(0xFF004D61))
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
                )
            },
            bottomBar = {
                BottomNavigationBar(
                    userRole = userProfile?.role ?: "buyer",
                    currentRoute = Screen.Profile.route,
                    onHomeClick = onNavigateToHome,
                    onAddClick = {
                        if (userProfile?.role == "seller" || userProfile?.role == "تاجر") {
                            onNavigateToAddProperty()
                        } else {
                            Toast.makeText(context, "هذا حساب مستأجر، لا يمكن إضافة عرض إلا من حساب تاجر", Toast.LENGTH_LONG).show()
                        }
                    },
                    onMessagesClick = onNavigateToMessages,
                    onProfileClick = { /* Already here */ }
                )
            }
        ) { paddingValues ->
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues).background(Color(0xFFF4F6F9))) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = Color(0xFF004D61))
                } else {
                    Column(
                        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp, vertical = 16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // User Profile Image Section
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(top = 8.dp)) {
                            Box(
                                contentAlignment = Alignment.BottomEnd,
                                modifier = Modifier.size(120.dp).clip(CircleShape).clickable { if (!isUploadingImage) photoPickerLauncher.launch("image/*") }
                            ) {
                                if (userProfile?.profileImageUrl?.isNotBlank() == true) {
                                    Image(painter = rememberAsyncImagePainter(userProfile.profileImageUrl), contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                                } else {
                                    Icon(imageVector = Icons.Default.AccountCircle, contentDescription = null, modifier = Modifier.fillMaxSize(), tint = Color.LightGray)
                                }
                                Box(modifier = Modifier.size(28.dp).background(Color(0xFF004D61), CircleShape).padding(4.dp), contentAlignment = Alignment.Center) {
                                    Icon(Icons.Default.Edit, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                                }
                            }
                            if (isUploadingImage) {
                                Box(modifier = Modifier.size(120.dp).background(Color.Black.copy(alpha = 0.4f), CircleShape), contentAlignment = Alignment.Center) {
                                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(32.dp))
                                }
                            }
                        }

                        // Name and Email
                        OutlinedTextField(
                            value = nameInput, onValueChange = { nameInput = it }, label = { Text("الاسم بالكامل") }, modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp), isError = isNameTooShort,
                            supportingText = { if (isNameTooShort) Text("الاسم قصير جداً", color = MaterialTheme.colorScheme.error) },
                            singleLine = true
                        )

                        if (isDataChanged) {
                            Button(onClick = { onSaveName(nameInput) }, modifier = Modifier.fillMaxWidth().height(50.dp), shape = RoundedCornerShape(12.dp), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF004D61))) {
                                if (isSaving) CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
                                else Text("حفظ تغيير الاسم", color = Color.White)
                            }
                        }

                        // Payment Methods Setup
                        if (userProfile?.role == "seller") {
                            Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Settings, contentDescription = null, tint = Color(0xFF004D61))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("إعدادات الدفع (للبائع)", fontWeight = FontWeight.Bold, color = Color(0xFF004D61))
                                    }
                                    Spacer(modifier = Modifier.height(12.dp))
                                    OutlinedTextField(value = vodafoneCash, onValueChange = { vodafoneCash = it }, label = { Text("رقم فودافون كاش") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(8.dp), singleLine = true)
                                    Spacer(modifier = Modifier.height(8.dp))
                                    OutlinedTextField(value = instaPay, onValueChange = { instaPay = it }, label = { Text("عنوان InstaPay") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(8.dp), singleLine = true)
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Button(onClick = { onUpdatePaymentInfo(vodafoneCash, instaPay) }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(8.dp), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2A7DA0))) {
                                        Text("تحديث بيانات الدفع", color = Color.White)
                                    }
                                }
                            }
                        }

                        // Seller Dashboard: My Properties & Incoming Requests
                        if (userProfile?.role == "seller") {
                            Text("لوحة تحكم البائع", modifier = Modifier.fillMaxWidth(), fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF004D61))
                            
                            // My Properties
                            Text("عقاراتي المرفوعة", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                            if (myProperties.isEmpty()) {
                                Text("لا يوجد عقارات مرفوعة حالياً", fontSize = 12.sp, color = Color.Gray)
                            } else {
                                myProperties.forEach { property ->
                                    PropertyItem(property, onDelete = { onDeleteProperty(property.id) })
                                }
                            }

                            // Incoming Booking Requests
                            Text("طلبات الحجز الواردة", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                            val pendingRequests = incomingBookings.filter { it.status == "pending" }
                            if (pendingRequests.isEmpty()) {
                                Text("لا توجد طلبات حجز قيد الانتظار", fontSize = 12.sp, color = Color.Gray)
                            } else {
                                pendingRequests.forEach { booking ->
                                    BookingRequestItem(
                                        booking = booking,
                                        onAccept = { onUpdateBookingStatus(booking.requestId, "approved") },
                                        onReject = { onUpdateBookingStatus(booking.requestId, "rejected") },
                                        onViewScreenshot = { selectedBookingScreenshot = it }
                                    )
                                }
                            }
                        }

                        // Buyer Dashboard: My Bookings
                        if (userProfile?.role == "buyer") {
                            Text("لوحة تحكم المشتري - حجوزاتي", modifier = Modifier.fillMaxWidth(), fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF004D61))
                            if (myBookings.isEmpty()) {
                                Text("لم تقم بأي طلبات حجز بعد", fontSize = 12.sp, color = Color.Gray)
                            } else {
                                myBookings.forEach { booking ->
                                    MyBookingItem(booking)
                                }
                            }
                        }

                        // Account Info Section
                        InfoCard(label = "نوع الحساب", value = if (userProfile?.role == "seller") "بائع" else "مشتري")
                        val creationDate = accountCreationDate?.let { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(it)) } ?: "-"
                        InfoCard(label = "تاريخ الإنشاء", value = creationDate)
                    }
                }
            }
        }
    }

    // Screenshot Viewer Dialog
    if (selectedBookingScreenshot != null) {
        AlertDialog(
            onDismissRequest = { selectedBookingScreenshot = null },
            confirmButton = { TextButton(onClick = { selectedBookingScreenshot = null }) { Text("إغلاق") } },
            text = {
                AsyncImage(model = selectedBookingScreenshot, contentDescription = "Screenshot", modifier = Modifier.fillMaxWidth().height(400.dp), contentScale = ContentScale.Fit)
            }
        )
    }
}

@Composable
fun PropertyItem(property: PropertyData, onDelete: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(8.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
        Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
            AsyncImage(model = property.imageUrl, contentDescription = null, modifier = Modifier.size(60.dp).clip(RoundedCornerShape(8.dp)), contentScale = ContentScale.Crop)
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(property.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text("${property.price.toInt()} ج.م / ليلة", fontSize = 12.sp, color = Color.Gray)
            }
            IconButton(onClick = onDelete) { Icon(Icons.Default.Delete, contentDescription = "حذف", tint = Color.Red) }
        }
    }
}

@Composable
fun BookingRequestItem(booking: PaymentRequest, onAccept: () -> Unit, onReject: () -> Unit, onViewScreenshot: (String) -> Unit) {
    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text("العميل: ${booking.tenantName}", fontWeight = FontWeight.Bold)
            Text("العقار: ${booking.propertyName}", fontSize = 12.sp)
            Text("الفترة: ${booking.startDate} إلى ${booking.endDate}", fontSize = 12.sp)
            Text("المبلغ الإجمالي: ${booking.amount} ج.م", fontWeight = FontWeight.SemiBold, color = Color(0xFF2E7D32))
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = { onViewScreenshot(booking.screenshotUrl) }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE0E0E0))) {
                Text("عرض إيصال الدفع 🖼️", color = Color.Black)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = onAccept, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))) {
                    Text("قبول ✅", color = Color.White)
                }
                Button(onClick = onReject, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF44336))) {
                    Text("رفض ❌", color = Color.White)
                }
            }
        }
    }
}

@Composable
fun MyBookingItem(booking: PaymentRequest) {
    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(booking.propertyName, fontWeight = FontWeight.Bold)
                Text("${booking.startDate} - ${booking.endDate}", fontSize = 12.sp)
                Text("${booking.amount} ج.م", fontWeight = FontWeight.SemiBold)
            }
            val (statusText, statusColor) = when (booking.status) {
                "pending" -> "قيد المراجعة من المالك ⏳" to Color(0xFFFFB300)
                "approved" -> "تم تأكيد الحجز بنجاح 🎉" to Color(0xFF4CAF50)
                else -> "تم الرفض - إيصال غير صحيح ❌" to Color(0xFFF44336)
            }
            Surface(shape = RoundedCornerShape(8.dp), color = statusColor.copy(alpha = 0.1f)) {
                Text(statusText, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), fontSize = 10.sp, fontWeight = FontWeight.Bold, color = statusColor)
            }
        }
    }
}

@Composable
fun InfoCard(label: String, value: String) {
    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(10.dp), colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(text = label, fontSize = 12.sp, color = Color.Gray)
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = value, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = Color.Black)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ProfileScreenContentPreview() {
    Behindsee2Theme {
        ProfileScreenContent(
            userProfile = UserProfile(
                name = "أحمد محمد",
                role = "seller",
                vodafoneCash = "01012345678",
                instaPay = "ahmed@instapay"
            ),
            isLoading = false,
            isSaving = false,
            isUploadingImage = false,
            statusMessage = "",
            accountCreationDate = System.currentTimeMillis(),
            myProperties = listOf(
                PropertyData(
                    id = "1",
                    name = "شقة فاخرة تطل على البحر",
                    price = 1500.0,
                    imageUrl = ""
                )
            ),
            incomingBookings = listOf(
                PaymentRequest(
                    requestId = "1",
                    tenantName = "سارة علي",
                    propertyName = "شقة فاخرة تطل على البحر",
                    amount = "3000",
                    startDate = "2023-10-01",
                    endDate = "2023-10-03",
                    status = "pending"
                )
            ),
            myBookings = listOf(
                PaymentRequest(
                    requestId = "2",
                    propertyName = "فيلا متميزة",
                    amount = "5000",
                    startDate = "2023-08-20",
                    endDate = "2023-08-25",
                    status = "approved"
                )
            ),
            onBackClick = {},
            onNavigateToHome = {},
            onNavigateToMessages = {},
            onNavigateToAddProperty = {},
            onUploadImage = {},
            onSaveName = {},
            onUpdatePaymentInfo = { _, _ -> },
            onDeleteProperty = {},
            onUpdateBookingStatus = { _, _ -> },
            onStatusMessageShown = {}
        )
    }
}
