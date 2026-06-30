package com.learn.behindsee2.ui.theme

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import coil.compose.AsyncImage
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.learn.behindsee2.Category
import com.learn.behindsee2.R
import com.learn.behindsee2.navigation.BottomNavigationBar
import com.learn.behindsee2.navigation.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OverScreen(
    modifier: Modifier = Modifier,
    categories: List<Category> = emptyList(),
    filteredProperties: List<PropertyData> = emptyList(),
    searchQuery: String = "",
    selectedCategory: String? = "الكل",
    userRole: String = "seller",
    userProfileUrl: String? = null,
    onQueryChange: (String) -> Unit = {},
    onCategorySelected: (Category) -> Unit = {},
    onNavigateToAddProperty: () -> Unit = {},
    onNavigateToPropertyDetails: (String) -> Unit = {},
    onNavigateToMessages: () -> Unit = {},
    onProfileClick: () -> Unit = {},
    onBackClick: () -> Unit = {},
    onNavigateToHome: () -> Unit = {},
) {
    val context = LocalContext.current
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    var uploadProgress by remember { mutableStateOf(0) }

    // حقول المدخلات
    var descrep by remember { mutableStateOf("") }
    var adress by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var dataLocation by remember { mutableStateOf("") }

    // حالة الصورة والفيديو المختارة
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var selectedVideoUri by remember { mutableStateOf<Uri?>(null) }

    // الإحداثيات
    var latitude by remember { mutableStateOf<Double?>(null) }
    var longitude by remember { mutableStateOf<Double?>(null) }

    // الحالات والخيارات
    var selectedAvailability by remember { mutableStateOf(0) }
    var isInstantBookingEnabled by remember { mutableStateOf(false) }
    var isWifiSelected by remember { mutableStateOf(false) }
    var isAcSelected by remember { mutableStateOf(false) }
    var isParkingSelected by remember { mutableStateOf(false) }
    var isPoolSelected by remember { mutableStateOf(false) }
    var selectedCategoryForUpload by remember { mutableStateOf("شاليه") }

    // حالة التحميل (Loading State)
    var isUploading by remember { mutableStateOf(false) }

    val photoPickerLauncher = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) selectedImageUri = uri
    }

    val videoPickerLauncher = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
            selectedVideoUri = uri
            Toast.makeText(context, "تم اختيار الفيديو بنجاح!", Toast.LENGTH_SHORT).show()
        }
    }

    val locationPermissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) {
            getCurrentLocation(context, fusedLocationClient) { lat, lng ->
                latitude = lat
                longitude = lng
                adress = "تم تحديد الموقع الجغرافي بنجاح عبر الـ GPS"
                Toast.makeText(context, "تم تحديد موقعك بنجاح!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    val isFormValid = descrep.isNotBlank() && adress.isNotBlank() && price.isNotBlank() &&
            dataLocation.isNotBlank() && selectedImageUri != null &&
            latitude != null && longitude != null

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Scaffold(
            modifier = modifier.fillMaxSize().statusBarsPadding(),
            topBar = {
                TopAppBar(
                    navigationIcon = {
                        IconButton(onClick = onBackClick) {
                            Icon(painterResource(R.drawable.outline_menu_24), null, tint = Color(0xFF004D61))
                        }
                    },
                    title = {
                        Row(Modifier.fillMaxWidth().padding(end = 16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("خلف البحر", fontWeight = FontWeight.Bold, color = Color(0xFF004D61))
                            if (!userProfileUrl.isNullOrEmpty()) {
                                AsyncImage(userProfileUrl, null, Modifier.size(38.dp).clip(CircleShape).clickable { onProfileClick() }, contentScale = ContentScale.Crop)
                            } else {
                                Icon(Icons.Default.AccountCircle, null, Modifier.size(38.dp).clip(CircleShape).clickable { onProfileClick() }, tint = Color(0xFF004D61))
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
                )
            },
            bottomBar = {
                BottomNavigationBar(
                    userRole = userRole,
                    currentRoute = Screen.Over.route,
                    onHomeClick = onNavigateToHome,
                    onAddClick = { /* Already here */ },
                    onMessagesClick = onNavigateToMessages,
                    onProfileClick = onProfileClick
                )
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier.fillMaxSize().padding(innerPadding).background(Color(0xFFF8FBFC))
                    .verticalScroll(rememberScrollState()).padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // العناوين
                Column(Modifier.fillMaxWidth()) {
                    Text("إضافة عرض جديد", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color(0xFF004D61))
                    Text("شارك جمال منزلك الساحلي مع المسافرين.", fontSize = 14.sp, color = Color(0xFF555555))
                }

                Spacer(Modifier.height(24.dp))

                // رفع الوسائط
                Row(Modifier.fillMaxWidth(), Arrangement.spacedBy(12.dp)) {
                    MediaPickerBox(Modifier.weight(1f), selectedImageUri, "إضافة صورة", R.drawable.baseline_add_photo_alternate_24) {
                        photoPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                    }
                    MediaPickerBox(Modifier.weight(1f), selectedVideoUri, if (selectedVideoUri != null) "تم اختيار فيديو" else "إضافة فيديو", R.drawable.outline_rectangle_add_24) {
                        videoPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly))
                    }
                }

                // الحقول
                CustomInputField("عنوان العرض", descrep, "مثال: فيلا هائلة مطلة على البحر") { descrep = it }

                Text("الموقع والمدينة", Modifier.fillMaxWidth().padding(top = 20.dp), fontWeight = FontWeight.Bold, color = Color(0xFF004D61))
                OutlinedTextField(
                    value = adress,
                    onValueChange = { adress = it },
                    Modifier.fillMaxWidth().padding(top = 8.dp),
                    leadingIcon = {
                        IconButton(onClick = {
                            if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                                getCurrentLocation(context, fusedLocationClient) { lat, lng ->
                                    latitude = lat
                                    longitude = lng
                                    adress = "تم تحديد الموقع الجغرافي بنجاح عبر الـ GPS"
                                }
                            } else {
                                locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                            }
                        }) {
                            Icon(painterResource(R.drawable.outline_location_searching_24), null, tint = if (latitude != null) Color(0xFF007AFF) else Color.Gray)
                        }
                    },
                    placeholder = { Text("اكتب العنوان تفصيلياً أو اضغط علامة الـ GPS") },
                    shape = RoundedCornerShape(12.dp)
                )

                CustomInputField("السعر لليلة الواحدة", price, "0.00", isPrice = true) { price = it }
                CustomInputField("وصف المكان", dataLocation, "اكتب وصفاً جذاباً...", singleLine = false) { dataLocation = it }

                Spacer(Modifier.height(24.dp))

                // المرافق والتوافر
                AmenitiesSection(isWifiSelected, { isWifiSelected = it }, isAcSelected, { isAcSelected = it }, isParkingSelected, { isParkingSelected = it }, isPoolSelected, { isPoolSelected = it })

                Spacer(Modifier.height(12.dp))

                AvailabilitySection(selectedAvailability) { selectedAvailability = it }

                Spacer(Modifier.height(12.dp))

                Row(Modifier.fillMaxWidth().background(Color.White, RoundedCornerShape(12.dp)).border(1.dp, Color(0xFFE0E0E0), RoundedCornerShape(12.dp)).padding(16.dp), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                    Text("قبول الحجوزات الفورية", color = Color(0xFF004D61))
                    CustomToggleButton(isInstantBookingEnabled) { isInstantBookingEnabled = it }
                }
                Text("نوع العقار", fontWeight = FontWeight.Bold, color = Color(0xFF004D61), modifier = Modifier.padding(top = 20.dp))
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val categories = listOf("شاليه", "فيلا", "كوخ", "منزل")
                    categories.forEach { category ->
                        AvailabilityBox(
                            modifier = Modifier.weight(1f),
                            label = category,
                            isSelected = selectedCategoryForUpload == category,
                            onClick = { selectedCategoryForUpload = category }
                        )
                    }
                }

                Spacer(Modifier.height(32.dp))

                // زرار النشر المحدث لـ Cloudinary
                Button(
                    onClick = {
                        isUploading = true
                        val ownerId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
                        val newProperty = PropertyData(
                            name = descrep,
                            description = dataLocation,
                            address = adress,
                            price = price.toDoubleOrNull() ?: 0.0,
                            latitude = latitude,
                            longitude = longitude,
                            wifi = isWifiSelected,
                            ac = isAcSelected,
                            parking = isParkingSelected,
                            pool = isPoolSelected,
                            instantBooking = isInstantBookingEnabled,
                            availability = selectedAvailability,
                            ownerId = ownerId,
                            categoryName = selectedCategoryForUpload
                        )

                        val db = FirebaseFirestore.getInstance()
                        val propertyId = db.collection("properties").document().id

                        if (selectedImageUri != null) {
                            uploadProgress = 0
                            MediaManager.get().upload(selectedImageUri!!)
                                .option("unsigned", true)
                                .option("upload_preset", "behindsee2")
                                .callback(object : UploadCallback {
                                    override fun onStart(requestId: String) {}
                                    override fun onProgress(requestId: String, bytes: Long, totalBytes: Long) {
                                        val progress = (100.0 * bytes / totalBytes)
                                        uploadProgress = if (selectedVideoUri != null) (progress * 0.5).toInt() else progress.toInt()
                                    }
                                    override fun onSuccess(requestId: String, resultData: Map<*, *>) {
                                        val imageUrl = resultData["secure_url"] as String
                                        if (selectedVideoUri != null) {
                                            MediaManager.get().upload(selectedVideoUri!!)
                                                .option("unsigned", true)
                                                .option("upload_preset", "behindsee2")
                                                .option("resource_type", "video")
                                                .callback(object : UploadCallback {
                                                    override fun onStart(requestId: String) {}
                                                    override fun onProgress(requestId: String, bytes: Long, totalBytes: Long) {
                                                        val videoProgress = (100.0 * bytes / totalBytes)
                                                        uploadProgress = 50 + (videoProgress * 0.5).toInt()
                                                    }
                                                    override fun onSuccess(requestId: String, resultData: Map<*, *>) {
                                                        val videoUrl = resultData["secure_url"] as String
                                                        val finalProperty = newProperty.copy(
                                                            id = propertyId,
                                                            imageUrl = imageUrl,
                                                            videoUrl = videoUrl
                                                        )
                                                        saveToFirestore(db, propertyId, finalProperty, context) { isUploading = false }
                                                    }
                                                    override fun onError(requestId: String, error: ErrorInfo) {
                                                        isUploading = false
                                                        Toast.makeText(context, "فشل رفع الفيديو: ${error.description}", Toast.LENGTH_LONG).show()
                                                    }
                                                    override fun onReschedule(requestId: String, error: ErrorInfo) {}
                                                }).dispatch()
                                        } else {
                                            val finalProperty = newProperty.copy(id = propertyId, imageUrl = imageUrl)
                                            saveToFirestore(db, propertyId, finalProperty, context) { isUploading = false }
                                        }
                                    }
                                    override fun onError(requestId: String, error: ErrorInfo) {
                                        isUploading = false
                                        Toast.makeText(context, "فشل رفع الصورة: ${error.description}", Toast.LENGTH_LONG).show()
                                    }
                                    override fun onReschedule(requestId: String, error: ErrorInfo) {}
                                }).dispatch()
                        }
                    },
                    enabled = isFormValid && !isUploading,
                    modifier = Modifier.fillMaxWidth().height(54.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF004D61), disabledContainerColor = Color(0xFFD1D1D6))
                ) {
                    if (isUploading) {
                        Row(
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = if (selectedVideoUri != null && uploadProgress < 50) "جاري رفع الصورة... $uploadProgress%"
                                else if (selectedVideoUri != null) "جاري رفع الفيديو... $uploadProgress%"
                                else "جاري الرفع... $uploadProgress%",
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    } else {
                        Text("حفظ ونشر العرض الحالي", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        }
    }
}

// الأجزاء السفلية المساعدة تبقى مستقرة وتعمل كما هي بدون أي تعديل إضافي...
@Composable fun MediaPickerBox(modifier: Modifier, uri: Uri?, label: String, iconRes: Int, onClick: () -> Unit) { Box(modifier.height(150.dp).background(Color(0xFFF2F5F8), RoundedCornerShape(16.dp)).border(1.5.dp, if (uri != null) Color(0xFF004D61) else Color(0xFF0972F3).copy(0.3f), RoundedCornerShape(16.dp)).clickable { onClick() }, Alignment.Center) { if (uri != null) { if (label.contains("فيديو") || uri.toString().contains("video")) { Icon(painterResource(R.drawable.outline_all_inclusive_24), "فيديو", tint = Color(0xFF4CAF50), modifier = Modifier.size(32.dp)) } else { AsyncImage(uri, null, Modifier.fillMaxSize().clip(RoundedCornerShape(16.dp)), contentScale = ContentScale.Crop) } } else { Column(horizontalAlignment = Alignment.CenterHorizontally) { Icon(painterResource(iconRes), null, tint = Color(0xFF0972F3), modifier = Modifier.size(26.dp)); Text(label, fontSize = 12.sp, color = Color(0xFF0972F3)) } } } }
@Composable fun CustomInputField(label: String, value: String, placeholder: String, isPrice: Boolean = false, singleLine: Boolean = true, onValueChange: (String) -> Unit) { Column(Modifier.fillMaxWidth().padding(top = 20.dp)) { Text(label, fontWeight = FontWeight.Bold, color = Color(0xFF004D61)); OutlinedTextField(value = value, onValueChange = onValueChange, Modifier.fillMaxWidth().padding(top = 8.dp), placeholder = { Text(placeholder) }, prefix = { if (isPrice) Text("EGP ", fontWeight = FontWeight.Bold) }, shape = RoundedCornerShape(12.dp), singleLine = singleLine) } }
@Composable fun AmenitiesSection(w: Boolean, onW: (Boolean) -> Unit, a: Boolean, onA: (Boolean) -> Unit, p: Boolean, onP: (Boolean) -> Unit, s: Boolean, onS: (Boolean) -> Unit) { Column(Modifier.fillMaxWidth(), Arrangement.spacedBy(8.dp)) { Text("المرافق", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF004D61)); Row(Modifier.fillMaxWidth(), Arrangement.spacedBy(12.dp)) { AmenityCard(Modifier.weight(1f), "واي فاي", R.drawable.outline_wifi_24, w) { onW(!w) }; AmenityCard(Modifier.weight(1f), "تكييف",
    R.drawable.rounded_snowflake_24, a) { onA(!a) } }; Row(Modifier.fillMaxWidth(), Arrangement.spacedBy(12.dp)) { AmenityCard(Modifier.weight(1f), "موقف خاص", R.drawable.baseline_local_parking_24, p) { onP(!p) }; AmenityCard(Modifier.weight(1f), "مسبح خاص", R.drawable.baseline_pool_24, s) { onS(!s) } } } }
@Composable fun AvailabilitySection(selected: Int, onSelect: (Int) -> Unit) { Column(Modifier.fillMaxWidth(), Arrangement.spacedBy(8.dp)) { Row(verticalAlignment = Alignment.CenterVertically) { Icon(painterResource(
    R.drawable.outline_all_inclusive_24
), null, tint = Color(0xFF004D61), modifier = Modifier.size(20.dp)); Spacer(Modifier.width(8.dp)); Text("تحديد التوافر", fontWeight = FontWeight.Bold, color = Color(0xFF004D61)) }; Row(Modifier.fillMaxWidth(), Arrangement.spacedBy(12.dp)) { AvailabilityBox(Modifier.weight(1f), "متاح دائماً", selected == 0) { onSelect(0) }; AvailabilityBox(Modifier.weight(1f), "مواعيد محددة", selected == 1) { onSelect(1) } }; AvailabilityBox(Modifier.fillMaxWidth(), "عطلات نهاية الاسبوع", selected == 2) { onSelect(2) } } }
@Composable fun AmenityCard(modifier: Modifier, label: String, iconRes: Int, isSelected: Boolean, onClick: () -> Unit) { Box(modifier.background(if (isSelected) Color(0xFF004D61).copy(0.08f) else Color.White, RoundedCornerShape(12.dp)).border(1.5.dp, if (isSelected) Color(0xFF004D61) else Color(0xFFE0E0E0), RoundedCornerShape(12.dp)).clickable { onClick() }.padding(vertical = 14.dp), Alignment.Center) { Row(verticalAlignment = Alignment.CenterVertically) { Icon(painterResource(iconRes), null, tint = if (isSelected) Color(0xFF004D61) else Color(0xFF555555)); Spacer(Modifier.width(8.dp)); Text(label, fontSize = 14.sp, color = if (isSelected) Color(0xFF004D61) else Color(0xFF555555)) } } }
@Composable fun AvailabilityBox(modifier: Modifier, label: String, isSelected: Boolean, onClick: () -> Unit) { Box(modifier.background(if (isSelected) Color(0xFF004D61).copy(0.08f) else Color.White, RoundedCornerShape(12.dp)).border(1.5.dp, if (isSelected) Color(0xFF004D61) else Color(0xFFE0E0E0), RoundedCornerShape(12.dp)).clickable { onClick() }.padding(vertical = 14.dp), Alignment.Center) { Text(label, color = if (isSelected) Color(0xFF004D61) else Color(0xFF555555)) } }
private fun getCurrentLocation(context: Context, fusedLocationClient: com.google.android.gms.location.FusedLocationProviderClient, onLocationRetrieved: (Double, Double) -> Unit) { try { fusedLocationClient.lastLocation.addOnSuccessListener { location -> if (location != null) onLocationRetrieved(location.latitude, location.longitude) } } catch (e: SecurityException) { } }
@Composable fun CustomToggleButton(isChecked: Boolean, onCheckedChange: (Boolean) -> Unit) { val backgroundColor by animateColorAsState(if (isChecked) Color(0xFF007AFF) else Color(0xFFD1D1D6), tween(300)); val horizontalBias by androidx.compose.animation.core.animateFloatAsState(if (isChecked) 1f else -1f, tween(300)); Box(Modifier.width(64.dp).height(34.dp).clip(RoundedCornerShape(20.dp)).background(backgroundColor).clickable { onCheckedChange(!isChecked) }.padding(4.dp), contentAlignment = BiasAlignment(horizontalBias, 0f)) { Box(Modifier.size(26.dp).clip(CircleShape).background(Color.White)) } }
private fun saveToFirestore(db: FirebaseFirestore, id: String, property: PropertyData, context: Context, onComplete: () -> Unit) { db.collection("properties").document(id).set(property).addOnSuccessListener { Toast.makeText(context, "تم النشر بنجاح!", Toast.LENGTH_SHORT).show() }.addOnFailureListener { showUploadError(context) }.addOnCompleteListener { onComplete() } }
private fun showUploadError(context: Context) { Toast.makeText(context, "فشل رفع البيانات، يرجى مراجعة الإنترنت أو حجم الفيديو.", Toast.LENGTH_LONG).show() }
@Preview(showBackground = true, showSystemUi = true) @Composable fun OverScreenPreview() { Behindsee2Theme { OverScreen() } }
