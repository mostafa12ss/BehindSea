package com.learn.behindsee2.navigation

import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.learn.behindsee2.R

@Composable
fun BottomNavigationBar(
    userRole: String,
    currentRoute: String?,
    onHomeClick: () -> Unit,
    onAddClick: () -> Unit,
    onMessagesClick: () -> Unit,
    onProfileClick: () -> Unit
) {
    val context = LocalContext.current
    NavigationBar(containerColor = Color.White, tonalElevation = 8.dp) {
        NavigationBarItem(
            selected = currentRoute == Screen.Home.route,
            onClick = onHomeClick,
            icon = {
                Surface(
                    shape = CircleShape,
                    color = if (currentRoute == Screen.Home.route) Color(0xFF2A7DA0) else Color.Transparent,
                    modifier = Modifier.size(40.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            painter = painterResource(id = R.drawable.outline_home_24),
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                            tint = if (currentRoute == Screen.Home.route) Color.White else Color.Gray
                        )
                    }
                }
            },
            label = {
                Text(
                    text = stringResource(id = R.string.nav_discover),
                    fontSize = 10.sp,
                    color = if (currentRoute == Screen.Home.route) Color(0xFF2A7DA0) else Color.Gray,
                    fontWeight = if (currentRoute == Screen.Home.route) FontWeight.Bold else FontWeight.Normal
                )
            }
        )
        NavigationBarItem(
            selected = currentRoute == Screen.MessagesList.route,
            onClick = onMessagesClick,
            icon = {
                Icon(
                    painter = painterResource(id = R.drawable.baseline_chat_24),
                    contentDescription = null,
                    tint = if (currentRoute == Screen.MessagesList.route) Color(0xFF2A7DA0) else Color.Gray
                )
            },
            label = { Text(stringResource(id = R.string.nav_messages), fontSize = 10.sp) }
        )
        NavigationBarItem(
            selected = currentRoute == Screen.Over.route,
            onClick = {
                if (userRole == "seller" || userRole == "تاجر") {
                    onAddClick()
                } else {
                    Toast.makeText(context, "هذا حساب مستأجر، لا يمكن إضافة عرض إلا من حساب تاجر", Toast.LENGTH_LONG).show()
                }
            },
            icon = {
                Icon(
                    painter = painterResource(id = R.drawable.outline_add_business_24),
                    contentDescription = null,
                    tint = if (currentRoute == Screen.Over.route) Color(0xFF2A7DA0) else Color.Gray
                )
            },
            label = { Text("إضافة عرض", fontSize = 10.sp) }
        )
        NavigationBarItem(
            selected = currentRoute == Screen.Profile.route,
            onClick = onProfileClick,
            icon = {
                Icon(
                    imageVector = Icons.Default.AccountCircle,
                    contentDescription = null,
                    tint = if (currentRoute == Screen.Profile.route) Color(0xFF2A7DA0) else Color.Gray
                )
            },
            label = { Text(stringResource(id = R.string.nav_profile), fontSize = 10.sp) }
        )
    }
}
