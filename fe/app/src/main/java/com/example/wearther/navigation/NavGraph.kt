package com.example.wearther.navigation

import android.view.View
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.fragment.app.FragmentContainerView
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.*
import com.example.wearther.closet.upload.SplashScreen
import com.example.wearther.closet.upload.CategorySelectionScreen
import com.example.wearther.home.weather.HomeScreen
import com.example.wearther.home.weather.WeatherViewModel
import com.example.wearther.setting.screen.EditNicknameScreen
import com.example.wearther.setting.screen.RegisterScreen
import com.example.wearther.setting.screen.SettingScreen
import com.example.wearther.closet.screen.ClosetScreen
import com.example.wearther.community.screen.CommunityScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NavGraph() {
    val navController = rememberNavController()
    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStackEntry?.destination?.route

    // Context를 받는 WeatherViewModel 생성
    val context = LocalContext.current
    val weatherViewModel: WeatherViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                return WeatherViewModel(context) as T
            }
        }
    )

    // 선택된 이미지 URI를 저장할 상태
    var selectedImageUri by remember { mutableStateOf<android.net.Uri?>(null) }

    Scaffold(
        bottomBar = {
            if (currentRoute != "splash" &&
                currentRoute != "category_selection") {
                BottomNavigationBar(navController)
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            NavHost(
                navController = navController,
                startDestination = "splash",
                modifier = Modifier.fillMaxSize()
            ) {
                composable("splash") {
                    SplashScreen(
                        navController = navController,
                        viewModel = weatherViewModel
                    )
                }

                composable("home") {
                    HomeScreen()
                }

                composable("closet") {
                    ClosetScreen(
                        onNavigateToUpload = { uri ->
                            selectedImageUri = uri
                            navController.navigate("category_selection")
                        }
                    )
                }

                composable("category_selection") {
                    selectedImageUri?.let { uri ->
                        CategorySelectionScreen(
                            selectedImageUri = uri,
                            onNavigateBack = {
                                navController.popBackStack()
                            },
                            onUploadSuccess = {
                                navController.navigate("closet") {
                                    popUpTo("closet") { inclusive = true }
                                }
                                selectedImageUri = null
                            }
                        )
                    }
                }

                composable("community") {
                    CommunityScreen(navController)
                }

                composable("settings") {
                    SettingScreen(navController)
                }

                composable("register") {
                    RegisterScreen(navController)
                }

                composable("edit_nickname") {
                    EditNicknameScreen(navController)
                }
            }
        }
    }
}