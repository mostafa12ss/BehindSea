package com.learn.behindsee2.navigation

import android.net.Uri

sealed class Screen(val route: String) {
    object Splash : Screen("splash_screen")
    object Welcome : Screen("welcome_screen")
    object Login : Screen("login_screen")
    object SignUp : Screen("signup_screen")
    object Home : Screen("home_screen")
    object Profile : Screen("profile_screen")
    object MessagesList : Screen("messages_list_screen")
    object Over : Screen("over_screen") // شاشة إضافة عرض جديد

    object Chat : Screen("chat_screen/{roomId}/{currentUserId}/{receiverId}/{receiverName}?receiverImageUrl={receiverImageUrl}") {
        fun createRoute(roomId: String, currentUserId: String, receiverId: String, receiverName: String, receiverImageUrl: String? = null): String {
            val encodedUrl = if (receiverImageUrl != null) Uri.encode(receiverImageUrl) else "null"
            return "chat_screen/$roomId/$currentUserId/$receiverId/$receiverName?receiverImageUrl=$encodedUrl"
        }
    }

    object PropertyDetails : Screen("property_details/{propertyId}") {
        fun createRoute(propertyId: String) = "property_details/$propertyId"
    }
}