package com.learn.behindsee2.ui.theme

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import com.learn.behindsee2.R
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
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
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import androidx.compose.ui.platform.LocalClipboardManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentScreen(
    propertyId: String,
    requiredAmount: String,
    startDate: String = "",
    endDate: String = "",
    paymentViewModel: PaymentViewModel = viewModel(),
    onBackClick: () -> Unit = {},
    onPaymentSubmitted: () -> Unit = {}
) {
    LaunchedEffect(propertyId) {
        paymentViewModel.loadOwnerPaymentInfo(propertyId)
    }

    PaymentScreenContent(
        propertyId = propertyId,
        requiredAmount = requiredAmount,
        startDate = startDate,
        endDate = endDate,
        ownerPaymentInfo = paymentViewModel.ownerPaymentInfo,
        isLoadingOwner = paymentViewModel.isLoadingOwner,
        isSubmitting = paymentViewModel.isSubmitting,
        statusMessage = paymentViewModel.statusMessage,
        onStatusMessageShown = { paymentViewModel.statusMessage = "" },
        onSubmitPayment = { pid, amount, method, uri, start, end, onComplete ->
            paymentViewModel.submitPaymentRequest(pid, amount, method, uri, start, end, onComplete)
        },
        onBackClick = onBackClick,
        onPaymentSubmitted = onPaymentSubmitted
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentScreenContent(
    propertyId: String,
    requiredAmount: String,
    startDate: String,
    endDate: String,
    ownerPaymentInfo: UserProfile?,
    isLoadingOwner: Boolean,
    isSubmitting: Boolean,
    statusMessage: String,
    onStatusMessageShown: () -> Unit,
    onSubmitPayment: (String, String, String, Uri, String, String, (Boolean) -> Unit) -> Unit,
    onBackClick: () -> Unit = {},
    onPaymentSubmitted: () -> Unit = {}
) {
    val context = LocalContext.current
    var selectedMethod by remember { mutableStateOf("Vodafone Cash") }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    val clipboardManager = LocalClipboardManager.current

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? -> selectedImageUri = uri }

    LaunchedEffect(statusMessage) {
        if (statusMessage.isNotEmpty()) {
            Toast.makeText(context, statusMessage, Toast.LENGTH_LONG).show()
            onStatusMessageShown()
        }
    }

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("تأكيد دفع عربون الحجز", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color(0xFF004D61)) },
                    navigationIcon = {
                        IconButton(onClick = onBackClick) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "رجوع", tint = Color(0xFF004D61))
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
                )
            }
        ) { paddingValues ->
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues).background(Color(0xFFF4F6F9))) {
                if (isLoadingOwner) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = Color(0xFF004D61))
                } else {
                    Column(
                        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF004D61))
                        ) {
                            Column(modifier = Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("المبلغ المطلوب دفعه لتأكيد الحجز", color = Color.White.copy(alpha = 0.8f), fontSize = 13.sp)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("$requiredAmount جنيه مصري", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Text("اختر طريقة التحويل المناسبة لك:", modifier = Modifier.fillMaxWidth(), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.Black)

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            PaymentMethodButton(title = "فودافون كاش", isSelected = selectedMethod == "Vodafone Cash", modifier = Modifier.weight(1f), onClick = { selectedMethod = "Vodafone Cash" })
                            PaymentMethodButton(title = "إنستاباي InstaPay", isSelected = selectedMethod == "InstaPay", modifier = Modifier.weight(1f), onClick = { selectedMethod = "InstaPay" })
                        }

                        val paymentDetail = if (selectedMethod == "Vodafone Cash") ownerPaymentInfo?.vodafoneCash else ownerPaymentInfo?.instaPay
                        
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp).fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = if (selectedMethod == "Vodafone Cash") "قم بتحويل المبلغ إلى رقم فودافون كاش التالي:" else "قم بتحويل المبلغ إلى عنوان إنستاباي التالي:",
                                    fontSize = 13.sp, color = Color.Gray
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                
                                if (!paymentDetail.isNullOrBlank()) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center,
                                        modifier = Modifier.background(Color(0xFFF4F6F9), RoundedCornerShape(8.dp)).padding(horizontal = 16.dp, vertical = 8.dp)
                                    ) {
                                        Text(text = paymentDetail, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF004D61))
                                        Spacer(modifier = Modifier.width(12.dp))
                                        IconButton(onClick = {
                                            clipboardManager.setText(androidx.compose.ui.text.AnnotatedString(paymentDetail))
                                            Toast.makeText(context, "تم النسخ بنجاح 📋", Toast.LENGTH_SHORT).show()
                                        }, modifier = Modifier.size(24.dp)) {
                                            Icon(painter = painterResource(id = R.drawable.baseline_content_copy_24), contentDescription = null, tint = Color(0xFF004D61), modifier = Modifier.size(20.dp))
                                        }
                                    }
                                } else {
                                    Text("بيانات الدفع غير متوفرة لهذا المالك حالياً", color = Color.Red, fontSize = 12.sp)
                                }
                            }
                        }

                        Text("قم برفع لقطة الشاشة (Screenshot) لإيصال التحويل الناجح:", modifier = Modifier.fillMaxWidth(), fontSize = 14.sp, fontWeight = FontWeight.Medium, color = Color.DarkGray)

                        Box(
                            modifier = Modifier.fillMaxWidth().height(180.dp).clip(RoundedCornerShape(12.dp)).background(Color.White).border(1.dp, if (selectedImageUri != null) Color(0xFF004D61) else Color.LightGray, RoundedCornerShape(12.dp)).clickable { photoPickerLauncher.launch("image/*") },
                            contentAlignment = Alignment.Center
                        ) {
                            if (selectedImageUri != null) {
                                Box(modifier = Modifier.fillMaxSize()) {
                                    Image(painter = rememberAsyncImagePainter(selectedImageUri), contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                                    IconButton(onClick = { selectedImageUri = null }, modifier = Modifier.align(Alignment.TopStart).padding(8.dp).background(Color.Black.copy(alpha = 0.6f), CircleShape)) {
                                        Icon(Icons.Default.Clear, contentDescription = null, tint = Color.White)
                                    }
                                }
                            } else {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(painter = painterResource(R.drawable.baseline_add_photo_alternate_24), contentDescription = null, tint = Color.LightGray, modifier = Modifier.size(40.dp))
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text("اضغط هنا لاختيار الصورة من المعرض", fontSize = 13.sp, color = Color.Gray)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.weight(1f))

                        Button(
                            onClick = {
                                selectedImageUri?.let { uri ->
                                    onSubmitPayment(propertyId, requiredAmount, selectedMethod, uri, startDate, endDate) { success ->
                                        if (success) onPaymentSubmitted()
                                    }
                                }
                            },
                            enabled = selectedImageUri != null && !isSubmitting && !paymentDetail.isNullOrBlank(),
                            modifier = Modifier.fillMaxWidth().height(52.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF004D61), disabledContainerColor = Color.LightGray)
                        ) {
                            if (isSubmitting) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                            else Text("تأكيد الحجز وإرسال الإيصال", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PaymentMethodButton(title: String, isSelected: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Box(
        modifier = modifier.height(50.dp).clip(RoundedCornerShape(10.dp)).background(if (isSelected) Color(0xFF004D61) else Color.White).border(1.dp, if (isSelected) Color(0xFF004D61) else Color.LightGray, RoundedCornerShape(10.dp)).clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(text = title, color = if (isSelected) Color.White else Color.Black, fontSize = 14.sp, fontWeight = FontWeight.Medium)
    }
}

@Preview(showBackground = true)
@Composable
fun PaymentScreenPreview() {
    Behindsee2Theme {
        PaymentScreenContent(
            propertyId = "prop123",
            requiredAmount = "1500",
            startDate = "2023-10-01",
            endDate = "2023-10-05",
            ownerPaymentInfo = UserProfile(
                uid = "owner123",
                name = "Owner Name",
                vodafoneCash = "01012345678",
                instaPay = "owner@instapay"
            ),
            isLoadingOwner = false,
            isSubmitting = false,
            statusMessage = "",
            onStatusMessageShown = {},
            onSubmitPayment = { _, _, _, _, _, _, _ -> },
            onBackClick = {},
            onPaymentSubmitted = {}
        )
    }
}
