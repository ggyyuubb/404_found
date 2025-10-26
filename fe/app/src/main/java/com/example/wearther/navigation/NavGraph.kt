package com.example.wearther.navigation

import android.net.Uri
// Toast import 제거 (SimpleAddPostScreen 삭제 후 불필요)
// import android.widget.Toast
import androidx.compose.foundation.background // background import 유지
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons // Icons import 유지
import androidx.compose.material.icons.filled.* // filled icons import 유지
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment // Alignment import 유지
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color // Color import 유지
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp // dp import 유지
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController // NavController import 유지
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.example.wearther.closet.upload.*
import com.example.wearther.home.weather.HomeScreen
import com.example.wearther.home.weather.WeatherViewModel
import com.example.wearther.setting.screen.EditNicknameScreen
import com.example.wearther.setting.screen.RegisterScreen
import com.example.wearther.setting.screen.SettingScreen
import com.example.wearther.closet.screen.ClosetScreen
// CommunityScreen 관련 import
import com.example.wearther.community.screen.AddPostScreen // AddPostScreen import 추가
import com.example.wearther.community.screen.CommunityScreen
import com.example.wearther.community.screen.SearchUserScreen
import com.example.wearther.community.screen.UserProfileScreen
import com.example.wearther.community.screen.PostDetailScreen
import com.example.wearther.community.vm.CommunityViewModel // CommunityViewModel import 추가

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NavGraph() {
    val navController = rememberNavController()
    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStackEntry?.destination?.route

    val context = LocalContext.current
    val weatherViewModel: WeatherViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                return WeatherViewModel(context) as T
            }
        }
    )

    // 업로드 플로우를 위한 상태들
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var croppedImageUri by remember { mutableStateOf<Uri?>(null) }
    var aiAnalysisResult by remember { mutableStateOf<AIAnalysisResult?>(null) }

    Scaffold(
        bottomBar = {
            // 하단 네비게이션 바를 숨길 화면들
            val hideBottomBar = currentRoute == "splash" ||
                    currentRoute == "image_crop" ||
                    currentRoute == "ai_analysis" ||
                    currentRoute == "category_selection" ||
                    currentRoute == "add_post" || // add_post 경로 확인
                    currentRoute == "search_user" ||
                    currentRoute?.startsWith("user_profile/") == true ||
                    currentRoute?.startsWith("post_detail/") == true

            if (!hideBottomBar) {
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
                // 스플래시 화면
                composable("splash") {
                    SplashScreen(
                        navController = navController,
                        viewModel = weatherViewModel
                    )
                }

                // 홈 화면
                composable("home") {
                    HomeScreen() // HomeScreen은 ViewModel이 필요 없을 수 있음 (확인 필요)
                }

                // 옷장 화면
                composable("closet") {
                    ClosetScreen(
                        onNavigateToUpload = { uri ->
                            selectedImageUri = uri
                            navController.navigate("image_crop")
                        }
                    )
                }

                // --- 옷장 업로드 플로우 ---
                composable("image_crop") {
                    selectedImageUri?.let { uri ->
                        ImageCropScreen(
                            imageUri = uri,
                            onNavigateBack = {
                                navController.popBackStack("closet", inclusive = false)
                                selectedImageUri = null
                            },
                            onCropComplete = { croppedUri ->
                                croppedImageUri = croppedUri
                                navController.navigate("ai_analysis")
                            }
                        )
                    } ?: run { /* URI 없을 때 처리 (예: 이전 화면으로 복귀) */ }
                }

                composable("ai_analysis") {
                    croppedImageUri?.let { uri ->
                        AIAnalysisLoadingScreen(
                            imageUri = uri,
                            onAnalysisComplete = { result ->
                                aiAnalysisResult = result
                                navController.navigate("category_selection") {
                                    popUpTo("ai_analysis") { inclusive = true }
                                }
                            }
                        )
                    } ?: run { /* URI 없을 때 처리 */ }
                }

                composable("category_selection") {
                    croppedImageUri?.let { uri ->
                        CategorySelectionScreen(
                            selectedImageUri = uri,
                            aiResult = aiAnalysisResult,
                            onNavigateBack = {
                                // 이미지 크롭 단계로 돌아가도록 수정 (선택 사항)
                                navController.navigate("image_crop") {
                                    popUpTo("ai_analysis") { inclusive = true } // ai_analysis 스택 제거
                                }
                            },
                            onUploadSuccess = {
                                // 상태 초기화
                                selectedImageUri = null
                                croppedImageUri = null
                                aiAnalysisResult = null
                                // 옷장 화면으로 복귀
                                navController.navigate("closet") {
                                    popUpTo("closet") { inclusive = true }
                                }
                            }
                        )
                    } ?: run { /* URI 없을 때 처리 */ }
                }
                // --- 옷장 업로드 플로우 끝 ---

                // --- 커뮤니티 관련 화면 ---
                composable("community") {
                    CommunityScreen(navController)
                }

                // [ 💡 수정: SimpleAddPostScreen -> AddPostScreen으로 변경 💡 ]
                composable("add_post") {
                    // SimpleAddPostScreen(navController = navController) // <- 삭제

                    // CommunityViewModel 가져오기
                    // (Hilt/Koin 사용 시 @HiltViewModel() 등으로 더 간단하게 가져올 수 있음)
                    val communityViewModel: CommunityViewModel = viewModel(
                        factory = object : ViewModelProvider.Factory {
                            @Suppress("UNCHECKED_CAST")
                            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                                return CommunityViewModel.provide(context) as T
                            }
                        }
                    )
                    // 실제 업로드 기능이 있는 AddPostScreen 호출
                    AddPostScreen(
                        navController = navController,
                        viewModel = communityViewModel // ViewModel 전달
                    )
                }

                composable(
                    route = "post_detail/{postId}",
                    arguments = listOf(
                        navArgument("postId") { type = NavType.StringType } // StringType 확인
                    )
                ) { backStackEntry ->
                    val postId = backStackEntry.arguments?.getString("postId") ?: ""
                    PostDetailScreen(
                        navController = navController,
                        postId = postId // String 타입 postId 전달 확인
                    )
                }

                composable("search_user") {
                    SearchUserScreen(navController = navController)
                }

                composable(
                    route = "user_profile/{userId}",
                    arguments = listOf(
                        navArgument("userId") { type = NavType.StringType }
                    )
                ) { backStackEntry ->
                    val userId = backStackEntry.arguments?.getString("userId") ?: ""
                    UserProfileScreen(
                        navController = navController,
                        userId = userId
                    )
                }
                // --- 커뮤니티 관련 화면 끝 ---

                // --- 설정/인증 관련 화면 ---
                composable("settings") {
                    SettingScreen(navController)
                }

                composable("register") {
                    RegisterScreen(navController)
                }

                composable("edit_nickname") {
                    EditNicknameScreen(navController)
                }
                // --- 설정/인증 관련 화면 끝 ---
            }
        }
    }
}
