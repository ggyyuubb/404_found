package com.example.wearther.navigation

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.*
import com.example.wearther.closet.upload.SplashScreen
import com.example.wearther.closet.upload.CategorySelectionScreen
import com.example.wearther.community.FriendsScreen
import com.example.wearther.home.weather.HomeScreen
import com.example.wearther.home.weather.WeatherViewModel
import com.example.wearther.setting.screen.EditNicknameScreen
import com.example.wearther.setting.screen.RegisterScreen
import com.example.wearther.setting.screen.SettingScreen
import com.example.wearther.closet.screen.ClosetScreen
import com.example.wearther.closet.upload.UploadApi
import com.example.wearther.closet.upload.UploadResponse

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NavGraph() {
    val navController = rememberNavController()
    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStackEntry?.destination?.route

    // ✅ Context를 받는 WeatherViewModel 생성
    val context = LocalContext.current
    val weatherViewModel: WeatherViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                return WeatherViewModel(context) as T
            }
        }
    )

    // 선택된 이미지 URI를 저장할 상태 (Navigation 간 공유)
    var selectedImageUri by remember { mutableStateOf<android.net.Uri?>(null) }

    Scaffold(
        bottomBar = {
            if (currentRoute != "splash" &&
                currentRoute != "category_selection") { // 카테고리 선택 화면에서는 하단바 숨김
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
                    SplashScreen(navController, weatherViewModel)
                }

                composable("home") {
                    HomeScreen() // ✅ viewModel 파라미터 제거 (내부에서 생성)
                }

                // 옷장 화면 - 다이얼로그 방식으로 변경
                composable("closet") {
                    ClosetScreen(
                        onNavigateToUpload = { uri ->
                            // 선택된 URI 저장하고 카테고리 선택 화면으로 이동
                            selectedImageUri = uri
                            navController.navigate("category_selection")
                        }
                    )
                }

                // 카테고리 선택 화면
                composable("category_selection") {
                    selectedImageUri?.let { uri ->
                        CategorySelectionScreen(
                            selectedImageUri = uri,
                            onNavigateBack = {
                                navController.popBackStack()
                            },
                            onUploadSuccess = { // onCategorySelected → onUploadSuccess로 변경
                                navController.navigate("closet") {
                                    popUpTo("closet") { inclusive = true }
                                }
                                // URI 초기화
                                selectedImageUri = null
                            }
                        )
                    } ?: run {
                        // URI가 없으면 옷장으로 돌아가기
                        LaunchedEffect(Unit) {
                            navController.navigate("closet") {
                                popUpTo("closet") { inclusive = true }
                            }
                        }
                    }
                }

                composable("friends") {
                    FriendsScreen(navController)
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