package com.learn.behindsee2.ui.theme

import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.util.UUID

class PaymentViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()

    var isSubmitting by mutableStateOf(false)
    var statusMessage by mutableStateOf("")
    
    var ownerPaymentInfo by mutableStateOf<UserProfile?>(null)
    var isLoadingOwner by mutableStateOf(false)

    fun loadOwnerPaymentInfo(propertyId: String) {
        isLoadingOwner = true
        firestore.collection("properties").document(propertyId).get()
            .addOnSuccessListener { propDoc ->
                val ownerId = propDoc.getString("ownerId") ?: ""
                if (ownerId.isNotEmpty()) {
                    firestore.collection("users").document(ownerId).get()
                        .addOnSuccessListener { userDoc ->
                            ownerPaymentInfo = userDoc.toObject(UserProfile::class.java)
                            isLoadingOwner = false
                        }
                        .addOnFailureListener {
                            statusMessage = "فشل تحميل بيانات الدفع للمالك"
                            isLoadingOwner = false
                        }
                } else {
                    isLoadingOwner = false
                }
            }
            .addOnFailureListener {
                isLoadingOwner = false
                statusMessage = "فشل في الوصول لبيانات العقار"
            }
    }

    fun submitPaymentRequest(
        propertyId: String,
        amount: String,
        paymentMethod: String,
        imageUri: Uri,
        startDate: String,
        endDate: String,
        onComplete: (Boolean) -> Unit
    ) {
        val currentUser = auth.currentUser ?: return
        val currentUserId = currentUser.uid
        isSubmitting = true

        firestore.collection("properties").document(propertyId).get()
            .addOnSuccessListener { propDoc ->
                val property = propDoc.toObject(PropertyData::class.java)
                val ownerId = property?.ownerId ?: ""
                val propertyName = property?.name ?: "عقار"
                val tenantName = currentUser.displayName ?: "مستأجر"

                val requestId = UUID.randomUUID().toString()
                val storageRef = storage.reference.child("payment_screenshots/$currentUserId/$requestId.jpg")

                storageRef.putFile(imageUri)
                    .addOnSuccessListener {
                        storageRef.downloadUrl.addOnSuccessListener { downloadUrl ->
                            val screenshotUrlString = downloadUrl.toString()

                            val paymentRequest = PaymentRequest(
                                requestId = requestId,
                                tenantId = currentUserId,
                                tenantName = tenantName,
                                ownerId = ownerId,
                                propertyId = propertyId,
                                propertyName = propertyName,
                                amount = amount,
                                paymentMethod = paymentMethod,
                                screenshotUrl = screenshotUrlString,
                                status = "pending",
                                startDate = startDate,
                                endDate = endDate,
                                timestamp = Timestamp.now()
                            )

                            // 🟢 FIX: Collection name matched to ProfileViewModel
                            firestore.collection("payments")
                                .document(requestId)
                                .set(paymentRequest)
                                .addOnSuccessListener {
                                    isSubmitting = false
                                    statusMessage = "تم إرسال طلب الحجز بنجاح، في انتظار مراجعة المالك!"
                                    onComplete(true)
                                }
                                .addOnFailureListener {
                                    isSubmitting = false
                                    statusMessage = "فشل حفظ بيانات الدفع"
                                    onComplete(false)
                                }
                        }.addOnFailureListener {
                            isSubmitting = false
                            statusMessage = "فشل الحصول على رابط الصورة"
                            onComplete(false)
                        }
                    }
                    .addOnFailureListener {
                        isSubmitting = false
                        statusMessage = "فشل رفع صورة الإيصال"
                        onComplete(false)
                    }
            }
            .addOnFailureListener {
                isSubmitting = false
                statusMessage = "فشل التحقق من بيانات العقار"
                onComplete(false)
            }
    }
}
