package com.learn.behindsee2.ui.theme

import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.learn.behindsee2.R
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

@Composable
fun PropertyDetailsScreen(
    propertyId: String,
    viewModel: PropertyDetailsViewModel = viewModel(),
    onBackClick: () -> Unit = {},
    // 🟢 التحسين: إضافة onBookingSubmit هنا ليتم استخدامه في الـ NavGraph
    onBookingSubmit: (startDate: String, endDate: String, totalPrice: Int) -> Unit = { _, _, _ -> },
    // 🟢 التحسين: دعم تمرير رابط الصورة هنا ليتوافق 100% مع الـ NavGraph والـ ChatScreen المحدثة
    onMessageSeller: (roomId: String, currentUserId: String, receiverId: String, receiverName: String, imageUrl: String?) -> Unit = { _, _, _, _, _ -> }
) {
    LaunchedEffect(propertyId) {
        viewModel.fetchPropertyById(propertyId)
    }

    val property = viewModel.property
    val isLoading = viewModel.isLoading
    val errorMessage = viewModel.errorMessage

    Box(modifier = Modifier.fillMaxSize()) {
        if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = Color(0xFF2A7DA0))
        } else if (errorMessage != null) {
            Text(text = errorMessage, modifier = Modifier.align(Alignment.Center), color = Color.Red)
        } else if (property != null) {
            PropertyDetailsContent(
                property = property,
                onBackClick = onBackClick,
                onBookingSubmit = onBookingSubmit, // استخدام الـ lambda الممرر من الـ NavGraph
                onMessageSeller = {
                    val currentUserId = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid ?: ""
                    if (currentUserId.isEmpty()) {
                        return@PropertyDetailsContent
                    }
                    // دمج الـ IDs بشكل أبجدي لضمان ثبات الـ roomId بين الطرفين دائماً
                    val roomId = if (currentUserId < property.ownerId) "${currentUserId}_${property.ownerId}" else "${property.ownerId}_$currentUserId"

                    // 🟢 تمرير الاسم الفعلي للمالك وصورته من كلاس الـ PropertyData لمنع وميض "مستخدم" في الشات
                    val ownerName = property.ownerName.ifBlank { "مالك العقار" }
                    onMessageSeller(roomId, currentUserId, property.ownerId, ownerName, property.ownerImageUrl)
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PropertyDetailsContent(
    property: PropertyData,
    onBackClick: () -> Unit,
    onBookingSubmit: (startDate: String, endDate: String, totalPrice: Int) -> Unit,
    onMessageSeller: () -> Unit = {}
) {
    val context = LocalContext.current

    var startDateText by remember { mutableStateOf("اختر تاريخ البدء") }
    var endDateText by remember { mutableStateOf("اختر تاريخ الانتهاء") }
    var totalPrice by remember { mutableStateOf(0) }

    var startCalendar by remember { mutableStateOf<Calendar?>(null) }
    var endCalendar by remember { mutableStateOf<Calendar?>(null) }

    fun calculateTotal() {
        if (startCalendar != null && endCalendar != null) {
            val diffInMillis = endCalendar!!.timeInMillis - startCalendar!!.timeInMillis
            val days = TimeUnit.MILLISECONDS.toDays(diffInMillis)
            if (days > 0) {
                totalPrice = (days * property.price).toInt()
            } else {
                totalPrice = 0
            }
        }
    }

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("تفاصيل العقار", fontWeight = FontWeight.Bold, color = Color(0xFF004D61)) },
                    navigationIcon = {
                        IconButton(onClick = onBackClick) {
                            Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "رجوع", tint = Color(0xFF004D61))
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
                    .verticalScroll(rememberScrollState())
                    .background(Color(0xFFF7F9FC))
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(350.dp),
                    shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp)
                ) {
                    AsyncImage(
                        model = property.imageUrl,
                        contentDescription = "صورة العقار",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.FillWidth
                    )
                }

                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = property.name, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1A1A1A))
                        Text(text = "${property.price.toInt()} ج.م / ليلة", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2A7DA0))
                    }

                    if (property.address.isNotBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "الموقع: ${property.address}",
                            fontSize = 14.sp,
                            color = Color(0xFF2A7DA0),
                            modifier = Modifier.clickable {
                                property.latitude?.let { lat ->
                                    property.longitude?.let { lng ->
                                        val uri = Uri.parse("geo:$lat,$lng?q=$lat,$lng(${property.name})")
                                        val intent = Intent(Intent.ACTION_VIEW, uri)
                                        intent.setPackage("com.google.android.apps.maps")
                                        context.startActivity(intent)
                                    }
                                }
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(text = "الوصف", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = property.description.ifEmpty { "لا يوجد وصف متاح لهذا العقار حالياً." },
                        fontSize = 14.sp,
                        color = Color(0xFF4A4A4A),
                        lineHeight = 22.sp
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    if (property.wifi || property.ac || property.parking || property.pool) {
                        Text(text = "المرافق المتوفرة", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            if (property.wifi) AmenityChip(text = "واي فاي منزلي", iconRes = R.drawable.outline_wifi_24)
                            if (property.ac) AmenityChip(text = "تكييف مركزي", iconRes = R.drawable.rounded_snowflake_24)
                            if (property.parking) AmenityChip(text = "موقف خاص", iconRes = R.drawable.baseline_local_parking_24)
                            if (property.pool) AmenityChip(text = "مسبح خاص", iconRes = R.drawable.baseline_pool_24)
                        }
                        Spacer(modifier = Modifier.height(24.dp))
                    }

                    Text(text = "تحديد فترة الحجز", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // زرار تاريخ البدء
                        OutlinedButton(
                            onClick = {
                                val currentCal = Calendar.getInstance()
                                DatePickerDialog(context, { _, year, month, dayOfMonth ->
                                    val cal = Calendar.getInstance().apply { set(year, month, dayOfMonth) }
                                    startDateText = SimpleDateFormat("yyyy/MM/dd", Locale.getDefault()).format(cal.time)
                                    startCalendar = cal
                                    calculateTotal()
                                }, currentCal.get(Calendar.YEAR), currentCal.get(Calendar.MONTH), currentCal.get(Calendar.DAY_OF_MONTH)).show()
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.DateRange, contentDescription = null)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = startDateText, fontSize = 12.sp)
                        }

                        // زرار تاريخ الانتهاء (محدث برمجياً لمنع الاختيارات العشوائية المسبقة)
                        OutlinedButton(
                            onClick = {
                                val baseCal = startCalendar ?: Calendar.getInstance()
                                val dialog = DatePickerDialog(context, { _, year, month, dayOfMonth ->
                                    val cal = Calendar.getInstance().apply { set(year, month, dayOfMonth) }
                                    endDateText = SimpleDateFormat("yyyy/MM/dd", Locale.getDefault()).format(cal.time)
                                    endCalendar = cal
                                    calculateTotal()
                                }, baseCal.get(Calendar.YEAR), baseCal.get(Calendar.MONTH), baseCal.get(Calendar.DAY_OF_MONTH))

                                // 🟢 تحسين: منع حجز تاريخ انتهاء قبل تاريخ البدء المختار
                                startCalendar?.let { dialog.datePicker.minDate = it.timeInMillis + 24 * 60 * 60 * 1000 }
                                dialog.show()
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.DateRange, contentDescription = null)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = endDateText, fontSize = 12.sp)
                        }
                    }

                    if (totalPrice > 0) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9))
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp).fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(text = "إجمالي سعر الحجز:", fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32))
                                Text(text = "$totalPrice ج.م", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color(0xFF2E7D32))
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    if (!property.videoUrl.isNullOrEmpty()) {
                        Text(text = "معاينة الفيديو", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(property.videoUrl)).apply {
                                    setDataAndType(Uri.parse(property.videoUrl), "video/*")
                                }
                                context.startActivity(intent)
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE5F1F7)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(painter = painterResource(id = android.R.drawable.ic_media_play), contentDescription = null, tint = Color(0xFF2A7DA0))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = "تشغيل الفيديو التوضيحي", color = Color(0xFF2A7DA0), fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(24.dp))
                    }

                    // الزرار الرئيسي: حجز
                    Button(
                        onClick = {
                            if (startCalendar == null || endCalendar == null || totalPrice == 0) {
                                Toast.makeText(context, "الرجاء اختيار تواريخ صحيحة أولاً", Toast.LENGTH_SHORT).show()
                            } else {
                                onBookingSubmit(startDateText, endDateText, totalPrice)
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2A7DA0)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        val buttonText = if (property.instantBooking) "تأكيد الحجز الفوري الآن" else "إرسال طلب حجز للمالك"
                        Text(text = buttonText, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // 🟢 التعديل البصري: جعل زرار المراسلة ثانوي OutlinedButton لتناسق الـ UI والـ UX
                    OutlinedButton(
                        onClick = { onMessageSeller() },
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        border = BorderStroke(1.5.dp, Color(0xFF2A7DA0)),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF2A7DA0))
                    ) {
                        Text(text = "مراسلة البائع", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// 🟢 الـ AmenityChip المفقودة لتجنب أي خطأ كومبيلر أثناء التشغيل
@Composable
fun AmenityChip(text: String, iconRes: Int) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = Color.White,
        border = BorderStroke(1.dp, Color(0xFFE0E0E0))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(painter = painterResource(id = iconRes), contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.Gray)
            Spacer(modifier = Modifier.width(6.dp))
            Text(text = text, fontSize = 13.sp, color = Color.DarkGray)
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun PropertyDetailsScreenPreview() {
    PropertyDetailsContent(
        property = PropertyData(
            id = "1",
            name = "شقة فاخرة على البحر",
            description = "شقة واسعة ومريحة تطل مباشرة على البحر، مجهزة بجميع المرافق الحديثة لضمان إقامة ممتعة.",
            address = "الإسكندرية، جليم",
            price = 1500.0,
            imageUrl = "",
            wifi = true,
            ac = true,
            parking = true,
            pool = true,
            ownerName = "أحمد محمد"
        ),
        onBackClick = {},
        onBookingSubmit = { _, _, _ -> }
    )
}
