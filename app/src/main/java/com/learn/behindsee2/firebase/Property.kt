package com.learn.behindsee2.firebase

data class Property(
    val propertyId: String = "",
    val title: String = "",
    val price: String = "",
    val description: String = "",
    val location: String = "",
    val imageUrl: String = "",
    val vendorId: String = "", // معرف التاجر الذي قام برفع الشقة
    val services: List<String> = emptyList(), // الخدمات مثل (تكييف، حمام سباحة، واي فاي)
    val rating: Double = 0.0
)
