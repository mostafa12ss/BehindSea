package com.learn.behindsee2.ui.theme

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.learn.behindsee2.R

@Composable
fun SignUpScreen(
    onBackToLogin: () -> Unit,
    onSignUpSuccess: () -> Unit = {},
    onBuyerClick: () -> Unit = {}, // للإبقاء على التوافق مع NavGraph
    onSellerClick: () -> Unit = {}  // للإبقاء على التوافق مع NavGraph
) {
    var currentScreen by remember { mutableStateOf("selection") }

    when (currentScreen) {
        "buyer" -> buyer(
            onBackClick = { currentScreen = "selection" },
            onNavigateToLogin = onBackToLogin,
            onSignUpSuccess = onSignUpSuccess
        )
        "seller" -> seller(
            onBackClick = { currentScreen = "selection" },
            onNavigateToLogin = onBackToLogin,
            onSignUpSuccess = onSignUpSuccess
        )
        "selection" -> SignUpSelectionContent(
            onBuyerClick = { currentScreen = "buyer" },
            onSellerClick = { currentScreen = "seller" },
            onBackToLogin = onBackToLogin
        )
    }
}

@Composable
fun SignUpSelectionContent(
    onBuyerClick: () -> Unit,
    onSellerClick: () -> Unit,
    onBackToLogin: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.screen),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        Image(
            painter = painterResource(R.drawable.seamless_waves),
            contentDescription = null,
            modifier = Modifier.fillMaxWidth().align(Alignment.BottomCenter),
            contentScale = ContentScale.Crop
        )

        Column(
            modifier = Modifier.fillMaxSize().padding(bottom = 100.dp).verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.wrapContentWidth().clickable { onBackToLogin() }
                ) {
                    Icon(painter = painterResource(id = R.drawable.outline_arrow_back_24), contentDescription = null, tint = Color(0xFF2A7DA0))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "الرجوع", color = Color(0xFF2A7DA0), style = MaterialTheme.typography.bodyMedium)
                }
                Text(text = "خلف البحر", color = Color(0xFF2A7DA0), style = MaterialTheme.typography.headlineLarge, fontSize = 30.sp)
            }

            Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = "انشاء حساب جديد", style = MaterialTheme.typography.headlineLarge, fontSize = 24.sp, color = Color(0xFF0C0C0C))
                Text(text = "اختر كيف تود البدء معنا اليوم في خلف البحر", style = MaterialTheme.typography.bodySmall, fontSize = 14.sp, color = Color(0xFF555555))
            }

            SignUpCard(
                title = "أبحث عن منزل",
                description = "أرغب في استئجار منزل أحلامي على الساحل",
                imageRes = R.drawable.bayerphoto,
                iconRes = R.drawable.outline_location_searching_24,
                iconBgColor = Color(0xFF7EC6FF),
                onClick = onBuyerClick
            )

            SignUpCard(
                title = "أريد تأجير منزلي",
                description = "أرغب في عرض عقاري للزوار",
                imageRes = R.drawable.seller_photo,
                iconRes = R.drawable.baseline_key_24,
                iconBgColor = Color(0xFFFFAB6D),
                onClick = onSellerClick
            )
        }
    }
}

@Composable
fun SignUpCard(title: String, description: String, imageRes: Int, iconRes: Int, iconBgColor: Color, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(0.95f).height(360.dp).shadow(6.dp, RoundedCornerShape(20.dp)).clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.fillMaxSize().padding(12.dp), verticalArrangement = Arrangement.SpaceBetween, horizontalAlignment = Alignment.CenterHorizontally) {
            Image(painter = painterResource(id = imageRes), contentDescription = null, modifier = Modifier.fillMaxWidth().height(180.dp).clip(RoundedCornerShape(12.dp)), contentScale = ContentScale.Crop)
            Box(modifier = Modifier.background(iconBgColor, CircleShape).size(50.dp), contentAlignment = Alignment.Center) {
                Icon(painter = painterResource(id = iconRes), contentDescription = null, tint = Color.White)
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = title, style = MaterialTheme.typography.headlineMedium, color = Color(0xFF0C0C0C), fontSize = 20.sp)
                Text(text = description, style = MaterialTheme.typography.bodyMedium, color = Color(0xFF7c7c7c))
            }
        }
    }
}

@Composable
fun buyer(onBackClick: () -> Unit, onNavigateToLogin: () -> Unit, onSignUpSuccess: () -> Unit) {
    SignUpForm(role = "buyer", roleLabel = "مشتري / مستأجر", iconRes = R.drawable.outline_location_searching_24, iconBgColor = Color(0x4027AED7), onBackClick = onBackClick, onNavigateToLogin = onNavigateToLogin, onSignUpSuccess = onSignUpSuccess)
}

@Composable
fun seller(onBackClick: () -> Unit, onNavigateToLogin: () -> Unit, onSignUpSuccess: () -> Unit) {
    SignUpForm(role = "seller", roleLabel = "مؤجر / بائع", iconRes = R.drawable.baseline_key_24, iconBgColor = Color(0xFFFFAB6D), onBackClick = onBackClick, onNavigateToLogin = onNavigateToLogin, onSignUpSuccess = onSignUpSuccess)
}

@Composable
fun SignUpForm(role: String, roleLabel: String, iconRes: Int, iconBgColor: Color, onBackClick: () -> Unit, onNavigateToLogin: () -> Unit, onSignUpSuccess: () -> Unit) {
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        val context = LocalContext.current
        var phone by remember { mutableStateOf("") }
        var name by remember { mutableStateOf("") }
        var email by remember { mutableStateOf("") }
        var password by remember { mutableStateOf("") }
        var isLoading by remember { mutableStateOf(false) }
        var isPasswordVisible by remember { mutableStateOf(false) }

        Box(modifier = Modifier.background(Color(0xFFE8F9FC)).fillMaxSize()) {
            Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(bottom = 120.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(20.dp)) {
                Row(modifier = Modifier.fillMaxWidth().padding(top = 16.dp, start = 16.dp, end = 16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "خلف البحر", color = Color(0xFF165D78), style = MaterialTheme.typography.headlineLarge, fontSize = 30.sp)
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { onBackClick() }) {
                        Text(text = "الرجوع", color = Color(0xFF2A7DA0), style = MaterialTheme.typography.bodyMedium)
                        Icon(painter = painterResource(id = R.drawable.outline_arrow_back_24), contentDescription = null, modifier = Modifier.rotate(180f), tint = Color(0xFF2A7DA0))
                    }
                }

                Card(modifier = Modifier.fillMaxWidth(0.9f).shadow(8.dp, RoundedCornerShape(20.dp)), shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                    Column(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(modifier = Modifier.padding(top = 8.dp).fillMaxWidth(0.45f).height(32.dp).clip(CircleShape).background(iconBgColor), contentAlignment = Alignment.Center) {
                            Text(text = roleLabel, style = MaterialTheme.typography.bodyMedium, color = Color(0xFF0C0C0C), fontSize = 14.sp)
                        }
                        Text(text = "إكمال البيانات", style = MaterialTheme.typography.headlineMedium, fontSize = 24.sp)
                        
                        SignUpField(label = "الاسم الكامل", value = name, onValueChange = { name = it }, placeholder = "أدخل اسمك الثلاثي")
                        SignUpField(label = "البريد الإلكتروني", value = email, onValueChange = { email = it }, placeholder = "example@email.com", keyboardType = KeyboardType.Email)
                        SignUpField(
                            label = "رقم الهاتف",
                            value = phone,
                            onValueChange = { if (it.all { c -> c.isDigit() } && it.length <= 11) phone = it },
                            placeholder = "1xxxxxxxxx",
                            keyboardType = KeyboardType.Phone,
                            prefix = "+20 ",
                            iconRes = R.drawable.outline_phone_24,
                            supportingText = "(ينصح بكتابة رقم يحتوي على محفظة إلكترونية)"
                        )
                        
                        Column(modifier = Modifier.fillMaxWidth(0.95f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(text = "كلمة المرور", style = MaterialTheme.typography.bodyMedium, fontSize = 14.sp)
                            OutlinedTextField(
                                value = password, onValueChange = { password = it }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp),
                                visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password), singleLine = true,
                                trailingIcon = {
                                    val icon = if (isPasswordVisible) R.drawable.outline_hide_source_24 else R.drawable.outline_remove_red_eye_24
                                    IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) { Icon(painter = painterResource(id = icon), contentDescription = null, tint = Color(0xFF165D78)) }
                                }
                            )
                        }

                        Button(
                            onClick = {
                                if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
                                    Toast.makeText(context, "من فضلك املأ جميع الحقول", Toast.LENGTH_SHORT).show()
                                } else {
                                    isLoading = true
                                    FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password).addOnCompleteListener { task ->
                                        if (task.isSuccessful) {
                                            val userId = task.result?.user?.uid
                                            val userMap = hashMapOf("uid" to userId, "name" to name, "email" to email, "phone" to phone, "role" to role)
                                            if (userId != null) {
                                                FirebaseFirestore.getInstance().collection("users").document(userId).set(userMap).addOnSuccessListener {
                                                    isLoading = false
                                                    Toast.makeText(context, "تم التسجيل بنجاح!", Toast.LENGTH_LONG).show()
                                                    onSignUpSuccess()
                                                }.addOnFailureListener { isLoading = false }
                                            }
                                        } else { isLoading = false }
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth(0.95f).height(50.dp), shape = RoundedCornerShape(12.dp), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF165D78)), enabled = !isLoading
                        ) {
                            if (isLoading) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                            else Text(text = "إنشاء الحساب", fontSize = 16.sp, color = Color.White)
                        }

                        Row(modifier = Modifier.fillMaxWidth(0.95f), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                            Text(text = "لديك حساب بالفعل؟ ", style = MaterialTheme.typography.bodyMedium, fontSize = 14.sp, color = Color(0xFF555555))
                            Text(text = "تسجيل الدخول", style = MaterialTheme.typography.bodyMedium, fontSize = 14.sp, color = Color(0xFF165D78), modifier = Modifier.clickable { onNavigateToLogin() }.padding(horizontal = 4.dp))
                        }
                    }
                }
            }
            Image(painter = painterResource(R.drawable.seamless_waves2), contentDescription = null, modifier = Modifier.fillMaxWidth().height(50.dp).background(Color(0xFF77FCFF)).align(Alignment.BottomCenter), contentScale = ContentScale.Crop)
        }
    }
}

@Composable
fun SignUpField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    keyboardType: KeyboardType = KeyboardType.Text,
    prefix: String? = null,
    iconRes: Int? = null,
    supportingText: String? = null
) {
    Column(modifier = Modifier.fillMaxWidth(0.95f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(text = label, style = MaterialTheme.typography.bodyMedium, fontSize = 14.sp)
        OutlinedTextField(
            value = value, onValueChange = onValueChange, placeholder = { Text(placeholder) }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(unfocusedBorderColor = Color(0xFF3F616D), focusedBorderColor = Color(0xFF165D78), unfocusedContainerColor = Color(0xFFF6FCFD), focusedContainerColor = Color(0xFFFFFFFF)),
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType), singleLine = true,
            prefix = prefix?.let { { Text(text = it, color = Color(0xFF165D78)) } },
            trailingIcon = iconRes?.let { { Icon(painter = painterResource(id = it), contentDescription = null, tint = Color(0xFF165D78)) } },
            supportingText = supportingText?.let { { Text(text = it, fontSize = 10.sp, color = Color.Gray) } }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun SignUpScreenPreview() {
    Behindsee2Theme {
        SignUpScreen(
            onBackToLogin = {},
            onSignUpSuccess = {},
            onBuyerClick = {},
            onSellerClick = {}
        )
    }
}
