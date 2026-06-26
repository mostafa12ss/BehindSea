package com.learn.behindsee2.ui.theme

import android.content.Context
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.learn.behindsee2.R
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    onNavigateToWelcome: () -> Unit,
    onNavigateToHome: () -> Unit // دالة للتنقل التلقائي بعد انتهاء وقت الشاشة
) {
    val context = LocalContext.current
    // 1. تعريف قيم الأنيميشن (لتأثير التكبير والظهور الناعم للوجو)
    val scale = remember { Animatable(0.5f) } // سيبدأ اللوجو من نصف حجمه الطبيعي
    val alpha = remember { Animatable(0f) }   // سيبدأ اللوجو من الاختفاء التام

    // 2. إدارة وقت الشاشة والحركة برمجياً
    LaunchedEffect(key1 = true) {
        // تشغيل الأنيميشن بالتوازي خلال 1 ثانية (1000 مللي ثانية)
        scale.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 1000)
        )
        alpha.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 1000)
        )

        // الانتظار لمدة ثانيتين إضافيتين وهي ثابتة ومكتملة الوضوح
        delay(2000)

        // الانتقال تلقائياً لشاشة تسجيل الدخول
        val sharedPreferences = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)

        // 3. الفحص: هل دخل قبل كده؟ القيمة الافتراضية false
        val isNotFirstTime = sharedPreferences.getBoolean("is_not_first_time", false)

        if (isNotFirstTime) {
            // لو دخل قبل كده ➔ واديه الـ Home علطول
            onNavigateToHome()
        } else {
            // لو أول مرة ➔ حفظ القيمة كـ true عشان المرة الجاية، ثم توجيهه للـ Welcome
            sharedPreferences.edit().putBoolean("is_not_first_time", true).apply()
            onNavigateToWelcome()
        }
    }
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        // صورة الخلفية كاملة الشاشة
        Image(
            painter = painterResource(id = R.drawable.splash),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // كارت اللوجو المحسن مع ربط الأنيميشن به
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxWidth(0.39F)  // تم تكبير العرض والارتفاع قليلاً لتوفير مساحة مريحة للأيقونة بداخله
                .fillMaxHeight(0.18f)
                .scale(scale.value)   // ربط أنيميشن التكبير
                .alpha(alpha.value)   // ربط أنيميشن الشفافية والظهور
                .shadow(
                    elevation = 4.dp, // زيادة الارتفاع ليعطي عمقاً أكبر فوق الخلفية
                    shape = RoundedCornerShape(20.dp),
                    spotColor = Color(0x33000000),
                    ambientColor = Color(0x33000000)
                )
                .clip(shape = RoundedCornerShape(20.dp))
                .background(Color.White.copy(alpha = 0.35f)) // درجة الشفافية المناسبة لشكل الزجاج المتجمد
                .padding(16.dp) // بادينج داخلي لحماية الأيقونة من ملامسة الحواف المقطوعة
        ) {
            Icon(
                painter = painterResource(id = R.drawable.icon2),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                tint = Color(0xFF2A7DA0) // اللون الأزرق المعتمد للوجو
            )
        }

    }
}

//@Preview(showBackground = true)
//@Composable
//fun SplashScreenPreview() {
//    SplashScreen()
//}