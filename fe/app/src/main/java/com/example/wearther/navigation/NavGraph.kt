package com.example.wearther.navigation

import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
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
import com.example.wearther.community.screen.CommunityScreen
import com.example.wearther.community.screen.SearchUserScreen
import com.example.wearther.community.screen.UserProfileScreen
import com.example.wearther.community.screen.PostDetailScreen

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
                    currentRoute == "add_post" ||
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
                    HomeScreen()
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

                // 1단계: 이미지 크롭
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
                    }
                }

                // 2단계: AI 분석 로딩
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
                    }
                }

                // 3단계: 카테고리 확인 및 수정
                composable("category_selection") {
                    croppedImageUri?.let { uri ->
                        CategorySelectionScreen(
                            selectedImageUri = uri,
                            aiResult = aiAnalysisResult,
                            onNavigateBack = {
                                navController.navigate("image_crop") {
                                    popUpTo("category_selection") { inclusive = true }
                                }
                            },
                            onUploadSuccess = {
                                // 모든 상태 초기화
                                selectedImageUri = null
                                croppedImageUri = null
                                aiAnalysisResult = null

                                navController.navigate("closet") {
                                    popUpTo("closet") { inclusive = true }
                                }
                            }
                        )
                    }
                }

                // 커뮤니티 메인 화면
                composable("community") {
                    CommunityScreen(navController)
                }

                // 게시글 작성 화면
                composable("add_post") {
                    SimpleAddPostScreen(navController = navController)
                }

                // 게시글 상세 화면
                composable(
                    route = "post_detail/{postId}",
                    arguments = listOf(
                        navArgument("postId") { type = NavType.IntType }
                    )
                ) { backStackEntry ->
                    val postId = backStackEntry.arguments?.getInt("postId") ?: 0
                    PostDetailScreen(
                        navController = navController,
                        postId = postId
                    )
                }

                // 사용자 검색 화면
                composable("search_user") {
                    SearchUserScreen(navController = navController)
                }

                // 사용자 프로필 화면
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

                // 설정 화면
                composable("settings") {
                    SettingScreen(navController)
                }

                // 회원가입 화면
                composable("register") {
                    RegisterScreen(navController)
                }

                // 닉네임 수정 화면
                composable("edit_nickname") {
                    EditNicknameScreen(navController)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SimpleAddPostScreen(navController: NavController) {
    var description by remember { mutableStateOf("") }
    var temperature by remember { mutableStateOf("") }
    var weather by remember { mutableStateOf("") }

    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("게시글 작성") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "뒤로가기")
                    }
                },
                actions = {
                    TextButton(
                        onClick = {
                            if (description.isNotEmpty()) {
                                Toast.makeText(
                                    context,
                                    "게시글이 등록되었습니다",
                                    Toast.LENGTH_SHORT
                                ).show()
                                navController.popBackStack()
                            } else {
                                Toast.makeText(
                                    context,
                                    "내용을 입력해주세요",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        },
                        enabled = description.isNotEmpty()
                    ) {
                        Text(
                            "등록",
                            style = MaterialTheme.typography.titleMedium,
                            color = if (description.isNotEmpty())
                                MaterialTheme.colorScheme.primary
                            else Color.Gray
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 이미지 영역
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .background(Color(0xFFF3F4F6), MaterialTheme.shapes.medium),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        Icons.Default.CameraAlt,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = Color(0xFF9CA3AF)
                    )
                    Text(
                        "이미지 기능 준비 중",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF6B7280)
                    )
                }
            }

            // 설명 입력
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("오늘의 코디를 소개해주세요") },
                placeholder = { Text("예: 오늘 날씨 완전 좋아요! 가을 코디 추천합니다 🍂") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp),
                maxLines = 5,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = Color(0xFFE5E7EB)
                )
            )

            // 날씨 정보 입력
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = temperature,
                    onValueChange = { temperature = it },
                    label = { Text("온도") },
                    placeholder = { Text("18°C") },
                    leadingIcon = {
                        Icon(
                            Icons.Default.Thermostat,
                            contentDescription = null,
                            tint = Color(0xFF9CA3AF)
                        )
                    },
                    modifier = Modifier.weight(1f),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = Color(0xFFE5E7EB)
                    )
                )
                OutlinedTextField(
                    value = weather,
                    onValueChange = { weather = it },
                    label = { Text("날씨") },
                    placeholder = { Text("맑음") },
                    leadingIcon = {
                        Icon(
                            Icons.Default.WbSunny,
                            contentDescription = null,
                            tint = Color(0xFF9CA3AF)
                        )
                    },
                    modifier = Modifier.weight(1f),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = Color(0xFFE5E7EB)
                    )
                )
            }

            // 안내 카드
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFF0F9FF)
                ),
                shape = MaterialTheme.shapes.medium
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Lightbulb,
                        contentDescription = null,
                        tint = Color(0xFF0EA5E9),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "오늘 날씨에 어울리는 코디를 공유해보세요!",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF0369A1)
                    )
                }
            }
        }
    }
}