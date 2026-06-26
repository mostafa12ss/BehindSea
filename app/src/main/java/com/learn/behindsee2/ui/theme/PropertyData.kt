package com.learn.behindsee2.ui.theme

data class PropertyData(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val address: String = "",
    val price: Double = 0.0,
    val imageUrl: String = "",
    val videoUrl: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val wifi: Boolean = false,
    val ac: Boolean = false,
    val parking: Boolean = false,
    val pool: Boolean = false,
    val instantBooking: Boolean = false,
    val availability: Int = 0 ,
    val ownerId: String = "",
    val categoryName: String = "",
    var ownerName: String = "مالك العقار",
    var ownerImageUrl: String? = null
)