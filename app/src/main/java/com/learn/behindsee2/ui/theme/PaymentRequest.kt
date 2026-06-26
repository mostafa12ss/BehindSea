package com.learn.behindsee2.ui.theme

import com.google.firebase.Timestamp

data class PaymentRequest(
    val requestId: String = "",
    val tenantId: String = "",
    val tenantName: String = "",
    val ownerId: String = "",
    val propertyId: String = "",
    val propertyName: String = "",
    val amount: String = "",
    val paymentMethod: String = "",
    val screenshotUrl: String = "",
    val status: String = "pending", // pending, approved, rejected
    val startDate: String = "",
    val endDate: String = "",
    val timestamp: Timestamp? = null
)
