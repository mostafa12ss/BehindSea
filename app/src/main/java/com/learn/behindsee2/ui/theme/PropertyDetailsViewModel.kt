package com.learn.behindsee2.ui.theme

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

class PropertyDetailsViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()

    // متغير للاحتفاظ بالـ Listener لإغلاقه لاحقاً ومنع تسريب الذاكرة
    private var propertyListener: ListenerRegistration? = null

    var property by mutableStateOf<PropertyData?>(null)
        private set

    var isLoading by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    fun fetchPropertyById(propertyId: String) {
        if (propertyId.isEmpty()) return

        // إذا كان هناك مستمع قديم شغال، نقوم بإغلاقه أولاً
        propertyListener?.remove()

        isLoading = true
        propertyListener = db.collection("properties").document(propertyId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    errorMessage = "حدث خطأ أثناء تحميل البيانات"
                    isLoading = false
                    return@addSnapshotListener
                }

                if (snapshot != null && snapshot.exists()) {
                    val prop = snapshot.toObject(PropertyData::class.java)
                    if (prop != null) {
                        // 🟢 خطوة جلب بيانات المالك ديناميكياً من كولكشن الـ users
                        db.collection("users").document(prop.ownerId).get()
                            .addOnSuccessListener { userDoc ->
                                if (userDoc.exists()) {
                                    // ملء الحقول الافتراضية المضافة في الموديل
                                    prop.ownerName = userDoc.getString("name") ?: "مالك العقار"
                                    prop.ownerImageUrl = userDoc.getString("profileImageUrl")
                                }
                                property = prop
                                isLoading = false
                            }
                            .addOnFailureListener {
                                // حتى لو فشل جلب بيانات المالك، بنعرض بيانات العقار للمستخدم
                                property = prop
                                isLoading = false
                            }
                    } else {
                        errorMessage = "فشل في تحويل بيانات العقار"
                        isLoading = false
                    }
                } else {
                    errorMessage = "العقار غير موجود"
                    isLoading = false
                }
            }
    }

    // 🟢 تنظيف وإغلاق الـ Listener فور الخروج من الشاشة وتدمير الـ ViewModel
    override fun onCleared() {
        super.onCleared()
        propertyListener?.remove()
    }
}