package com.learn.behindsee2.ui.theme

data class UserProfile(
    val uid: String = "",
    val name: String = "",
    val email: String = "",
    val profileImageUrl: String = "",
    val role: String = "buyer", // seller أو buyer
    val phoneNumber: String = "",
    val vodafoneCash: String = "",
    val instaPay: String = ""
)