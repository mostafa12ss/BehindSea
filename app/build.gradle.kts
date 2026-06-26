plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    //google fire base
//    id("com.android.application")
    id("com.google.gms.google-services")
}

android {
    namespace = "com.learn.behindsee2"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        applicationId = "com.learn.behindsee2"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
        viewBinding = true
    }
}

dependencies {
    // 1️⃣ مكتبات الأندرويد والـ Compose الأساسية
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.runtime)
    implementation(libs.material)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)

    // ❌ تم حذف سطر libs.firebase.storage.ktx من هنا لأنه مسبب المشكلة

    // 2️⃣ مكتبات الفايربيس الصحيحة المربوطة بالـ BoM تلقائياً
    implementation(platform("com.google.firebase:firebase-bom:33.1.0"))
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-firestore")
    implementation("com.google.firebase:firebase-storage") // 👈 ده السطر الصح والنظيف للستوريدج!

    // 3️⃣ المكتبات الخارجية (الصور والموقع والـ ViewModel)
    implementation("io.coil-kt:coil-compose:2.6.0") // لعرض الصور في الكومبوز
    implementation("com.github.bumptech.glide:glide:4.15.1")
    implementation("com.google.android.gms:play-services-auth:21.2.0")
    implementation("com.google.android.gms:play-services-location:21.2.0") // للموقع الجغرافي
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.2")
    implementation(libs.androidx.navigation.compose) // للـ ViewModel

    // 4️⃣ ملفات التست (الاختبارات)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}