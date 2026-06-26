package com.learn.behindsee2.firebase

data class User(
    val uid: String = "",
    val name: String = "",
    val email: String = "",
    val role: String = "", // "buyer" or "seller"
    val phoneNumber: String = "",
    val vodafoneCash: String = "",
    val instaPay: String = ""
)
