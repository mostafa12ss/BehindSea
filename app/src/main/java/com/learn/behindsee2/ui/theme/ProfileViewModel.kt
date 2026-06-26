package com.learn.behindsee2.ui.theme

import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.userProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.SetOptions
import com.google.firebase.storage.FirebaseStorage
import java.util.UUID

class ProfileViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()

    // 🟢 Fix: Listener management to prevent memory leaks
    private var userListener: ListenerRegistration? = null
    private var propertiesListener: ListenerRegistration? = null
    private var incomingBookingsListener: ListenerRegistration? = null
    private var myBookingsListener: ListenerRegistration? = null

    var userProfile by mutableStateOf<UserProfile?>(null)
    var isLoading by mutableStateOf(false)
    var isSaving by mutableStateOf(false)
    var statusMessage by mutableStateOf("")

    var isUploadingImage by mutableStateOf(false)

    var myProperties = mutableStateListOf<PropertyData>()
    var incomingBookings = mutableStateListOf<PaymentRequest>()
    var myBookings = mutableStateListOf<PaymentRequest>()

    val accountCreationDate: Long?
        get() = auth.currentUser?.metadata?.creationTimestamp

    init {
        loadUserData()
    }

    fun loadUserData() {
        val currentUser = auth.currentUser
        val uid = currentUser?.uid ?: return

        isLoading = true
        userListener = firestore.collection("users").document(uid).addSnapshotListener { snapshot, error ->
            isLoading = false
            if (error != null) {
                statusMessage = "حدث خطأ أثناء تحميل البيانات"
                return@addSnapshotListener
            }

            if (snapshot != null && snapshot.exists()) {
                userProfile = snapshot.toObject(UserProfile::class.java)
                if (userProfile?.role == "seller") {
                    fetchSellerData(uid)
                } else {
                    fetchBuyerData(uid)
                }
            } else {
                val newProfile = UserProfile(
                    uid = uid,
                    name = currentUser.displayName ?: "مستخدم جديد",
                    email = currentUser.email ?: "",
                    role = "buyer"
                )
                firestore.collection("users").document(uid)
                    .set(newProfile)
                    .addOnSuccessListener {
                        userProfile = newProfile
                    }
            }
        }
    }

    private fun fetchSellerData(uid: String) {
        propertiesListener?.remove()
        propertiesListener = firestore.collection("properties").whereEqualTo("ownerId", uid)
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null) {
                    myProperties.clear()
                    myProperties.addAll(snapshot.toObjects(PropertyData::class.java))
                }
            }

        incomingBookingsListener?.remove()
        incomingBookingsListener = firestore.collection("payments").whereEqualTo("ownerId", uid)
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null) {
                    incomingBookings.clear()
                    incomingBookings.addAll(snapshot.toObjects(PaymentRequest::class.java))
                }
            }
    }

    private fun fetchBuyerData(uid: String) {
        myBookingsListener?.remove()
        myBookingsListener = firestore.collection("payments").whereEqualTo("tenantId", uid)
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null) {
                    myBookings.clear()
                    myBookings.addAll(snapshot.toObjects(PaymentRequest::class.java))
                }
            }
    }

    fun updatePaymentInfo(vodafoneCash: String, instaPay: String) {
        val uid = auth.currentUser?.uid ?: return
        isSaving = true
        val data = mapOf("vodafoneCash" to vodafoneCash, "instaPay" to instaPay)
        firestore.collection("users").document(uid).set(data, SetOptions.merge())
            .addOnSuccessListener {
                isSaving = false
                statusMessage = "تم تحديث بيانات الدفع بنجاح"
            }
            .addOnFailureListener {
                isSaving = false
                statusMessage = "فشل تحديث بيانات الدفع"
            }
    }

    fun deleteProperty(propertyId: String) {
        firestore.collection("properties").document(propertyId).delete()
            .addOnSuccessListener { statusMessage = "تم حذف العقار بنجاح" }
            .addOnFailureListener { statusMessage = "فشل حذف العقار" }
    }

    fun updateBookingStatus(requestId: String, newStatus: String) {
        firestore.collection("payments").document(requestId).update("status", newStatus)
            .addOnSuccessListener { statusMessage = if (newStatus == "approved") "تم قبول الحجز ✅" else "تم رفض الحجز ❌" }
            .addOnFailureListener { statusMessage = "فشل تحديث حالة الحجز" }
    }

    fun updateUserName(newName: String, onComplete: (Boolean) -> Unit) {
        val currentUser = auth.currentUser
        val uid = currentUser?.uid ?: return
        val cleanName = newName.trim()

        if (cleanName == userProfile?.name) {
            statusMessage = "لم يتم إجراء أي تعديل"
            onComplete(false)
            return
        }

        if (cleanName.length < 3) {
            statusMessage = "الاسم يجب أن يكون 3 أحرف أو أكثر"
            onComplete(false)
            return
        }

        isSaving = true
        val data = mapOf("name" to cleanName)

        firestore.collection("users").document(uid)
            .set(data, SetOptions.merge())
            .addOnSuccessListener {
                val profileUpdates = userProfileChangeRequest {
                    displayName = cleanName
                }
                currentUser.updateProfile(profileUpdates)
                    .addOnCompleteListener { task ->
                        isSaving = false
                        if (task.isSuccessful) {
                            userProfile = userProfile?.copy(name = cleanName)
                            statusMessage = "تم حفظ التعديلات بنجاح"
                            onComplete(true)
                        } else {
                            statusMessage = "تم الحفظ وفشلت المزامنة الفورية للـ Auth"
                            onComplete(true)
                        }
                    }
            }
            .addOnFailureListener {
                isSaving = false
                statusMessage = "فشل تحديث الاسم في قاعدة البيانات"
                onComplete(false)
            }
    }

    fun uploadProfileImage(imageUri: Uri) {
        val currentUser = auth.currentUser
        val uid = currentUser?.uid ?: return

        isUploadingImage = true
        val imageRef = storage.reference.child("profile_images/$uid/${UUID.randomUUID()}.jpg")
        imageRef.putFile(imageUri)
            .addOnSuccessListener {
                imageRef.downloadUrl.addOnSuccessListener { downloadUrl ->
                    val imageUrlString = downloadUrl.toString()
                    val data = mapOf("profileImageUrl" to imageUrlString)
                    firestore.collection("users").document(uid)
                        .set(data, SetOptions.merge())
                        .addOnSuccessListener {
                            val profileUpdates = userProfileChangeRequest {
                                photoUri = Uri.parse(imageUrlString)
                            }
                            currentUser.updateProfile(profileUpdates)
                                .addOnCompleteListener { task ->
                                    isUploadingImage = false
                                    if (task.isSuccessful) {
                                        userProfile = userProfile?.copy(profileImageUrl = imageUrlString)
                                        statusMessage = "تم تحديث الصورة الشخصية بنجاح"
                                    } else {
                                        statusMessage = "تم حفظ الصورة وفشلت المزامنة الفورية"
                                    }
                                }
                        }
                        .addOnFailureListener {
                            isUploadingImage = false
                            statusMessage = "فشل حفظ رابط الصورة"
                        }
                }.addOnFailureListener {
                    isUploadingImage = false
                    statusMessage = "فشل استخراج رابط الصورة"
                }
            }
            .addOnFailureListener {
                isUploadingImage = false
                statusMessage = "فشل رفع الصورة إلى السيرفر"
            }
    }

    override fun onCleared() {
        super.onCleared()
        userListener?.remove()
        propertiesListener?.remove()
        incomingBookingsListener?.remove()
        myBookingsListener?.remove()
    }
}
