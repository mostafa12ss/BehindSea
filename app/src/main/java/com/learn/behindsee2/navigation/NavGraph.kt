package com.learn.behindsee2.navigation

import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.learn.behindsee2.HomeScreen
import com.learn.behindsee2.LoginScreen
import com.learn.behindsee2.ui.theme.*
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@Composable
fun NavGraph(navController: NavHostController) {
    val context = LocalContext.current

    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route
    ) {
        composable(Screen.Splash.route) {
            SplashScreen(
                onNavigateToWelcome = {
                    navController.navigate(Screen.Welcome.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                },
                onNavigateToHome = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Welcome.route) {
            WelcomeScreen(
                onNavigateToSignUp = { navController.navigate(Screen.SignUp.route) },
                onNavigateToLogin = { navController.navigate(Screen.Login.route) }
            )
        }

        composable(Screen.Login.route) {
            LoginScreen(
                onNavigateToSignup = { navController.navigate(Screen.SignUp.route) },
                onLoginSuccess = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.SignUp.route) {
            SignUpScreen(
                onBackToLogin = { navController.popBackStack() },
                onSignUpSuccess = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.SignUp.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Home.route) {
            HomeScreen(
                onNavigateToPropertyDetails = { propertyId ->
                    navController.navigate(Screen.PropertyDetails.createRoute(propertyId))
                },
                onNavigateToProfile = { navController.navigate(Screen.Profile.route) },
                onNavigateToMessages = { navController.navigate(Screen.MessagesList.route) },
                onNavigateToAddProperty = { navController.navigate(Screen.Over.route) }
            )
        }

        composable(Screen.MessagesList.route) {
            MessagesListScreen(
                onChatClick = { roomId, currentUserId, receiverId, receiverName, imageUrl ->
                    val safeImg = if (imageUrl.isNullOrBlank()) "no_image" else imageUrl
                    navController.navigate(Screen.Chat.createRoute(roomId, currentUserId, receiverId, receiverName, safeImg))
                },
                onBackClick = { navController.popBackStack() },
                onNavigateToHome = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Home.route) { inclusive = true }
                    }
                },
                onNavigateToAddProperty = { navController.navigate(Screen.Over.route) },
                onNavigateToProfile = { navController.navigate(Screen.Profile.route) }
            )
        }

        composable(Screen.Over.route) {
            OverScreen(
                onBackClick = { navController.popBackStack() },
                onNavigateToHome = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Home.route) { inclusive = true }
                    }
                },
                onNavigateToMessages = { navController.navigate(Screen.MessagesList.route) },
                onProfileClick = { navController.navigate(Screen.Profile.route) },
                onNavigateToPropertyDetails = { propertyId ->
                    navController.navigate(Screen.PropertyDetails.createRoute(propertyId))
                }
            )
        }

        composable(Screen.Profile.route) {
            ProfileScreen(
                onBackClick = { navController.popBackStack() },
                onNavigateToHome = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Home.route) { inclusive = true }
                    }
                },
                onNavigateToMessages = { navController.navigate(Screen.MessagesList.route) },
                onNavigateToAddProperty = { navController.navigate(Screen.Over.route) }
            )
        }

        composable(
            route = "property_details/{propertyId}",
            arguments = listOf(navArgument("propertyId") { type = NavType.StringType })
        ) { backStackEntry ->
            val propertyId = backStackEntry.arguments?.getString("propertyId") ?: ""

            PropertyDetailsScreen(
                propertyId = propertyId,
                onBackClick = { navController.popBackStack() },
                onBookingSubmit = { startDate, endDate, totalPrice ->
                    val encodedStart = URLEncoder.encode(startDate, StandardCharsets.UTF_8.toString())
                    val encodedEnd = URLEncoder.encode(endDate, StandardCharsets.UTF_8.toString())
                    navController.navigate("payment_screen/$propertyId/$totalPrice?startDate=$encodedStart&endDate=$encodedEnd")
                },
                onMessageSeller = { roomId, currentUserId, receiverId, receiverName, imageUrl ->
                    val safeImg = if (imageUrl.isNullOrBlank()) "no_image" else imageUrl
                    navController.navigate(Screen.Chat.createRoute(roomId, currentUserId, receiverId, receiverName, safeImg))
                }
            )
        }

        composable(
            route = "payment_screen/{propertyId}/{requiredAmount}?startDate={startDate}&endDate={endDate}",
            arguments = listOf(
                navArgument("propertyId") { type = NavType.StringType },
                navArgument("requiredAmount") { type = NavType.StringType },
                navArgument("startDate") { type = NavType.StringType; defaultValue = "" },
                navArgument("endDate") { type = NavType.StringType; defaultValue = "" }
            )
        ) { backStackEntry ->
            val propertyId = backStackEntry.arguments?.getString("propertyId") ?: ""
            val requiredAmount = backStackEntry.arguments?.getString("requiredAmount") ?: "0"
            val startDate = backStackEntry.arguments?.getString("startDate") ?: ""
            val endDate = backStackEntry.arguments?.getString("endDate") ?: ""

            PaymentScreen(
                propertyId = propertyId,
                requiredAmount = requiredAmount,
                startDate = startDate,
                endDate = endDate,
                onBackClick = { navController.popBackStack() },
                onPaymentSubmitted = {
                    Toast.makeText(context, "تم إرسال إيصال الدفع بنجاح! جاري المراجعة 💸", Toast.LENGTH_LONG).show()
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Home.route) { inclusive = true }
                    }
                }
            )
        }

        composable(
            route = Screen.Chat.route,
            arguments = listOf(
                navArgument("roomId") { type = NavType.StringType },
                navArgument("currentUserId") { type = NavType.StringType },
                navArgument("receiverId") { type = NavType.StringType },
                navArgument("receiverName") { type = NavType.StringType },
                navArgument("receiverImageUrl") { type = NavType.StringType; nullable = true }
            )
        ) { backStackEntry ->
            val rawImg = backStackEntry.arguments?.getString("receiverImageUrl")
            val finalImg = if (rawImg == "no_image") null else rawImg

            ChatScreen(
                roomId = backStackEntry.arguments?.getString("roomId") ?: "",
                currentUserId = backStackEntry.arguments?.getString("currentUserId") ?: "",
                receiverId = backStackEntry.arguments?.getString("receiverId") ?: "",
                receiverName = backStackEntry.arguments?.getString("receiverName") ?: "",
                receiverImageUrl = finalImg,
                onBackClick = { navController.popBackStack() }
            )
        }
    }
}
