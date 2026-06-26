package com.learn.behindsee2.ui.theme

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.learn.behindsee2.R

@Composable
fun WelcomeScreen(
    onNavigateToSignUp: () -> Unit = {},
    onNavigateToLogin: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 1️⃣ الهيدر العلوي (أخذ مساحة محددة وثابتة تناسب اللوجو)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp, horizontal = 24.dp),
            horizontalArrangement = Arrangement.Center, // جعل الهيدر يبدأ من اليمين ليتناسق مع اللغة العربية
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "خلف البحر",
                color = Color(0xFF0180D4),
                fontSize = 28.sp,
                style = MaterialTheme.typography.headlineLarge
            )
            Spacer(modifier = Modifier.width(8.dp))
            Icon(
                painter = painterResource(R.drawable.icon2),
                contentDescription = null,
                tint = Color(0xFF0180D4),
                modifier = Modifier.height(35.dp)
            )
        }

        // 2️⃣ الجزء السفلي (أصبح مرن ياخذ المساحة المتبقية كاملة فما يخرجش برا الشاشة)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f) // التريكة هنا: يملأ الشاشة بذكاء مهما كان طول الموبايل
                .clip(RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp)) // لمسة جمالية لقص الخلفية من الأعلى
        ) {
            // الصورة الأساسية مع إعادة تفعيل التعتيم لبروز النصوص
            Image(
                painter = painterResource(R.drawable.welcome),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
                colorFilter = ColorFilter.tint(
                    Color.Black.copy(alpha = 0.3f),
                    BlendMode.Darken
                )
            )

            // نصوص الترحيب والأزرار مرتبة عمودياً بالكامل ومتناسقة
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.SpaceBetween // توزيع العناصر بذكاء بين الأعلى والأسفل
            ) {
                // الكولوم العلوي للنصوص داخل البوكس
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.End
                ) {
                    Spacer(modifier = Modifier.height(20.dp))
                    Text(
                        text = "مرحباً بك في خلف البحر",
                        fontSize = 34.sp, // حجم متناسق وآمن للشاشات الصغيرة
                        color = Color.White, // الأبيض هنا فوق التعتيم يظهر بشكل فاخر وجذاب
                        textAlign = TextAlign.End,
                        style = MaterialTheme.typography.headlineMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "رحلتك نحو الهدوء والاسترخاء تبدأ من هنا. اكتشف أجمل الوجهات الساحلية المختارة بعناية.",
                        fontSize = 16.sp,
                        color = Color(0xFFE3E7E7), // أبيض شفاف مريح للعين
                        textAlign = TextAlign.End,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }

                // الكولوم السفلي للأزرار عشان تضمن إنها دايماً تحت في الشاشة
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(16.dp) // مسافة ثابتة ومتناسقة بين الزرين
                ) {
                    // زر إنشاء حساب جديد
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(85.dp) // تقليل الارتفاع قليلاً ليكون شكل الزر رشيق وأنيق
                            .background(shape = RoundedCornerShape(18.dp), color = Color(0xFF014D7D))
                            .clickable { onNavigateToSignUp() }, // جعل الزر قابل للضغط والتنقل
                        contentAlignment = Alignment.CenterEnd
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 20.dp),
                            horizontalAlignment = Alignment.End,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "إنشاء حساب جديد",
                                    fontSize = 20.sp,
                                    color = Color.White,
                                    style = MaterialTheme.typography.bodyLarge
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Icon(
                                    painter = painterResource(R.drawable.outline_assignment_add_24),
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.height(28.dp)
                                )
                            }
                            Text(
                                text = "انضم إلينا واستكشف عالم الهدوء",
                                fontSize = 13.sp,
                                color = Color(0xFFE3E7E7),
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }

                    // زر تسجيل الدخول
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(85.dp)
                            .background(
                                shape = RoundedCornerShape(18.dp),
                                color = Color.White.copy(alpha = 0.85f) // جعل الخلفية بيضاء زجاجية لتظهر برقي فوق الصورة
                            )
                            .clickable { onNavigateToLogin() },
                        contentAlignment = Alignment.CenterEnd
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 20.dp),
                            horizontalAlignment = Alignment.End,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "تسجيل الدخول", // تعديل النص ليكون منطقي (الزر التاني تسجيل دخول)
                                    fontSize = 20.sp,
                                    color = Color(0xFF014D7D),
                                    style = MaterialTheme.typography.bodyLarge
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Icon(
                                    painter = painterResource(R.drawable.baseline_login_24),
                                    contentDescription = null,
                                    tint = Color(0xFF014D7D),
                                    modifier = Modifier
                                        .height(28.dp)
                                        .rotate(180f)
                                )
                            }
                            Text(
                                text = "مرحباً بعودتك إلى شاطئك المفضل",
                                fontSize = 13.sp,
                                color = Color.DarkGray,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp)) // مسافة أمان سفلية
                }
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun WelcomeScreenPreview() {
    WelcomeScreen()
}