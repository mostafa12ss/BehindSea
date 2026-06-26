package com.learn.behindsee2

import android.widget.Toast
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.learn.behindsee2.navigation.BottomNavigationBar
import com.learn.behindsee2.navigation.Screen
import com.learn.behindsee2.ui.theme.Behindsee2Theme
import com.learn.behindsee2.ui.theme.PropertyData

// 1️⃣ الـ Data Classes
data class Category(
    val name: String = "",
    val iconRes: Int? = null
)

val categoriesList = listOf(
    Category(name = "شاليه", iconRes = R.drawable.outline_chalet_24),
    Category(name = "فيلا", iconRes = R.drawable.outline_villa_24),
    Category(name = "كوخ", iconRes = R.drawable.outline_home_24),
    Category(name = "منزل", iconRes = R.drawable.home)
)

// 2️⃣ الـ ViewModel لإدارة البيانات
class HomeViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private var propertiesListener: ListenerRegistration? = null
    private var userListener: ListenerRegistration? = null

    var allProperties = mutableStateListOf<PropertyData>()
        private set

    var filteredProperties = mutableStateListOf<PropertyData>()
        private set

    var selectedCategory by mutableStateOf<String?>("الكل")
    var searchQuery by mutableStateOf("")

    var userRole by mutableStateOf("buyer")
    var userProfileUrl by mutableStateOf<String?>("")

    init {
        fetchPropertiesFromFirebase()
        fetchUserRole()
    }

    private fun fetchUserRole() {
        val uid = auth.currentUser?.uid ?: return
        userListener = db.collection("users").document(uid).addSnapshotListener { snapshot, _ ->
            if (snapshot != null && snapshot.exists()) {
                userRole = snapshot.getString("role") ?: "buyer"
                userProfileUrl = snapshot.getString("profileImageUrl")
            }
        }
    }

    private fun fetchPropertiesFromFirebase() {
        propertiesListener = db.collection("properties")
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) return@addSnapshotListener

                val propertiesList = snapshot.toObjects(PropertyData::class.java)
                allProperties.clear()
                allProperties.addAll(propertiesList)

                applyFilter()
            }
    }

    fun applyFilter() {
        val result = allProperties.filter { property ->
            val matchesCategory = selectedCategory == "الكل" || property.categoryName == selectedCategory
            val matchesSearch = searchQuery.isEmpty() ||
                    property.address.contains(searchQuery, ignoreCase = true) ||
                    property.name.contains(searchQuery, ignoreCase = true)
            matchesCategory && matchesSearch
        }
        filteredProperties.clear()
        filteredProperties.addAll(result)
    }

    override fun onCleared() {
        super.onCleared()
        propertiesListener?.remove()
        userListener?.remove()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreenContent(
    modifier: Modifier = Modifier,
    categories: List<Category> = emptyList(),
    filteredProperties: List<PropertyData> = emptyList(),
    searchQuery: String = "",
    selectedCategory: String? = "الكل",
    userRole: String = "buyer",
    userProfileUrl: String? = null,
    onQueryChange: (String) -> Unit = {},
    onCategorySelected: (Category) -> Unit = {},
    onNavigateToAddProperty: () -> Unit = {},
    onNavigateToPropertyDetails: (String) -> Unit = {},
    onNavigateToMessages: () -> Unit = {},
    onProfileClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val fullCategoriesList = remember(categories) {
        listOf(Category("الكل", null)) + categories
    }

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Scaffold(
            modifier = modifier.fillMaxSize().statusBarsPadding(),
            topBar = {
                TopAppBar(
                    navigationIcon = {
                        IconButton(onClick = { }) {
                            Icon(
                                painter = painterResource(R.drawable.outline_menu_24),
                                tint = Color(0xFF004D61),
                                contentDescription = "Menu",
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    },
                    title = {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(end = 16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = stringResource(id = R.string.app_title),
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF004D61)
                            )
                            if (!userProfileUrl.isNullOrEmpty()) {
                                AsyncImage(
                                    model = userProfileUrl,
                                    contentDescription = "Profile Picture",
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(CircleShape)
                                        .clickable { onProfileClick() },
                                    contentScale = ContentScale.Crop,
                                    placeholder = ColorPainter(Color.LightGray)
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.AccountCircle,
                                    contentDescription = "Default Profile",
                                    tint = Color(0xFF004D61),
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(CircleShape)
                                        .clickable { onProfileClick() }
                                )
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
                )
            },
            bottomBar = {
                BottomNavigationBar(
                    userRole = userRole,
                    currentRoute = Screen.Home.route,
                    onHomeClick = { /* Already here */ },
                    onAddClick = onNavigateToAddProperty,
                    onMessagesClick = onNavigateToMessages,
                    onProfileClick = onProfileClick
                )
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize()
                    .background(Color(0xFFF8FBFC))
                    .verticalScroll(rememberScrollState())
            ) {
                SearchBar(query = searchQuery, onQueryChange = onQueryChange, modifier = Modifier.padding(16.dp))
                CategoryFilter(categories = fullCategoriesList, selectedCategoryName = selectedCategory, onCategorySelected = onCategorySelected)
                PropertyListing(
                    properties = filteredProperties,
                    onPropertyClick = { property -> onNavigateToPropertyDetails(property.id) },
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
    }
}

// 5️⃣ الشاشة الذكية المتصلة بالـ ViewModel
@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    homeViewModel: HomeViewModel = viewModel(),
    onNavigateToAddProperty: () -> Unit = {},
    onNavigateToPropertyDetails: (String) -> Unit = {},
    onNavigateToMessages: () -> Unit = {},
    onNavigateToProfile: () -> Unit = {}
) {
    HomeScreenContent(
        modifier = modifier,
        categories = categoriesList,
        filteredProperties = homeViewModel.filteredProperties,
        searchQuery = homeViewModel.searchQuery,
        selectedCategory = homeViewModel.selectedCategory,
        userRole = homeViewModel.userRole,
        userProfileUrl = homeViewModel.userProfileUrl,
        onQueryChange = {
            homeViewModel.searchQuery = it
            homeViewModel.applyFilter()
        },
        onCategorySelected = { category ->
            homeViewModel.selectedCategory = category.name
            homeViewModel.applyFilter()
        },
        onNavigateToAddProperty = onNavigateToAddProperty,
        onNavigateToPropertyDetails = onNavigateToPropertyDetails,
        onNavigateToMessages = onNavigateToMessages,
        onProfileClick = onNavigateToProfile
    )
}

// 6️⃣ شريط البحث
@Composable
fun SearchBar(query: String, onQueryChange: (String) -> Unit, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            OutlinedTextField(
                value = query,
                onValueChange = onQueryChange,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text(text = stringResource(id = R.string.search_placeholder), modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Start, fontSize = 14.sp) },
                trailingIcon = { Icon(painter = painterResource(R.drawable.baseline_search_24), contentDescription = null, modifier = Modifier.size(20.dp)) },
                shape = RoundedCornerShape(20.dp),
                colors = OutlinedTextFieldDefaults.colors(unfocusedBorderColor = Color(0xFFF0F0F0), focusedBorderColor = Color(0xFFF0F0F0), unfocusedContainerColor = Color(0xFFF9F9F9), focusedContainerColor = Color(0xFFF9F9F9)),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                CustomFilterChip(text = stringResource(id = R.string.filter_date), icon = painterResource(R.drawable.outline_calendar_today_24), modifier = Modifier.weight(1f))
                CustomFilterChip(text = stringResource(id = R.string.filter_location), icon = painterResource(R.drawable.outline_location_searching_24), modifier = Modifier.weight(1f))
                CustomFilterChip(text = stringResource(id = R.string.filter_price), icon = painterResource(R.drawable.baseline_price_change_24), containerColor = Color(0xFFF2E7D5), modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
fun CustomFilterChip(text: String, icon: Painter, containerColor: Color = Color.White, modifier: Modifier = Modifier) {
    Surface(modifier = modifier.height(36.dp), shape = RoundedCornerShape(18.dp), color = containerColor, border = BorderStroke(1.dp, Color(0xFFEEEEEE))) {
        Row(modifier = Modifier.padding(horizontal = 8.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
            Icon(painter = icon, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color.Gray)
            Spacer(modifier = Modifier.width(4.dp))
            Text(text = text, fontSize = 11.sp, color = Color.DarkGray, maxLines = 1)
        }
    }
}

// 7️⃣ قائمة الأقسام
@Composable
fun CategoryFilter(categories: List<Category>, selectedCategoryName: String?, onCategorySelected: (Category) -> Unit, modifier: Modifier = Modifier) {
    LazyRow(
        modifier = modifier.padding(vertical = 8.dp).fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        itemsIndexed(categories) { _, category ->
            val isSelected = category.name == selectedCategoryName
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable { onCategorySelected(category) }) {
                Icon(
                    painter = if (category.iconRes != null) painterResource(id = category.iconRes) else painterResource(id = R.drawable.outline_all_inclusive_24),
                    contentDescription = category.name,
                    modifier = Modifier.size(24.dp),
                    tint = if (isSelected) Color(0xFF004D61) else Color.Gray
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = category.name, fontSize = 12.sp, color = if (isSelected) Color(0xFF004D61) else Color.Gray, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal)
                if (isSelected) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Box(modifier = Modifier.width(20.dp).height(2.dp).background(Color(0xFF004D61)))
                }
            }
        }
    }
}

@Composable
fun PropertyListing(properties: List<PropertyData>, onPropertyClick: (PropertyData) -> Unit, modifier: Modifier = Modifier) {
    if (properties.isEmpty()) {
        Box(modifier = modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
            Text(text = "لا توجد عروض مطابقة للبحث حالياً", color = Color.Gray, fontSize = 14.sp)
        }
    } else {
        Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(16.dp)) {
            properties.forEach { property -> PropertyCard(property = property, onClick = { onPropertyClick(property) }) }
        }
    }
}

// 8️⃣ كارت عرض العقارات
@Composable
fun PropertyCard(property: PropertyData, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column {
            Box(modifier = Modifier.height(200.dp).fillMaxWidth()) {
                AsyncImage(
                    model = property.imageUrl,
                    contentDescription = property.name,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                    placeholder = ColorPainter(Color.LightGray),
                    error = ColorPainter(Color.LightGray)
                )
                Surface(
                    modifier = Modifier.padding(12.dp).align(Alignment.TopStart),
                    shape = RoundedCornerShape(12.dp),
                    color = Color.White.copy(alpha = 0.9f)
                ) {
                    Row(modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "4.9", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(imageVector = Icons.Default.Star, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color(0xFFFFB400))
                    }
                }
                IconButton(onClick = { }, modifier = Modifier.padding(8.dp).align(Alignment.TopEnd).background(Color.Black.copy(alpha = 0.2f), CircleShape)) {
                    Icon(painter = painterResource(id = R.drawable.outline_heart_plus_24), contentDescription = "Favorite", modifier = Modifier.size(20.dp), tint = Color.White)
                }
            }
            Column(modifier = Modifier.padding(16.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text(text = property.name, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.Black)

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = property.price.toInt().toString(), fontSize = 18.sp, color = Color(0xFF2A7DA0), fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = stringResource(id = R.string.currency), fontSize = 12.sp, color = Color(0xFF2A7DA0))
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(text = "/ ${stringResource(id = R.string.night)}", fontSize = 11.sp, color = Color.Gray)
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = property.address, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Start, fontSize = 12.sp, color = Color.Gray)
                Spacer(modifier = Modifier.height(12.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Start, verticalAlignment = Alignment.CenterVertically) {
                    val dummyTags = listOf("عقار مميز", "إطلالة بحرية")
                    dummyTags.forEachIndexed { index, tag ->
                        Surface(modifier = Modifier.padding(end = 8.dp), shape = RoundedCornerShape(8.dp), color = if (index == 0) Color(0xFFBCE6F5) else Color(0xFFF2E7D5)) {
                            Text(text = tag, modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp), fontSize = 11.sp, color = if (index == 0) Color(0xFF004D61) else Color(0xFF8B5E3C))
                        }
                    }
                }
            }
        }
    }
}

// 9️⃣ الـ Preview
@Preview(showBackground = true, showSystemUi = true)
@Composable
fun HomeScreenPreview() {
    Behindsee2Theme {
        HomeScreenContent(
            categories = categoriesList,
            filteredProperties = emptyList(),
            selectedCategory = "الكل"
        )
    }
}
