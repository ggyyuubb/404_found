package com.example.wearther.navigation

import android.content.Context // 컨텍스트 접근용
import android.net.Uri // URI 객체 처리용
import android.util.Log // 로그 출력용
import androidx.compose.foundation.layout.* // 레이아웃 관련 함수들
import androidx.compose.material3.* // 머터리얼3 UI 요소들
import androidx.compose.runtime.* // Compose 상태관리 관련
import androidx.compose.ui.Modifier // Modifier 사용을 위한 임포트
import androidx.compose.ui.platform.LocalContext // Compose 내에서 Context 접근용
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType // 인자 타입 정의용
import androidx.navigation.compose.* // NavController, NavHost 등 컴포즈 네비게이션
import androidx.navigation.navArgument // navArgument 정의용
import com.example.wearther.closet.upload.SplashScreen
import com.example.wearther.community.FriendsScreen
import com.example.wearther.home.weather.HomeScreen
import com.example.wearther.home.weather.WeatherViewModel
import com.example.wearther.setting.screen.EditNicknameScreen
import com.example.wearther.setting.screen.RegisterScreen
import com.example.wearther.setting.screen.SettingScreen
import com.example.wearther.ui.screens.closet.ClosetScreen
import java.net.URLDecoder // URI 디코딩
import java.nio.charset.StandardCharsets // UTF-8 인코딩 명시

@OptIn(ExperimentalMaterial3Api::class) // BottomNavigationBar나 Scaffold 등 일부 머터리얼3 요소에 필요
@Composable
fun NavGraph() {
    val navController = rememberNavController() // NavController 생성 (Compose에서 화면 간 이동을 관리)

    val currentBackStackEntry by navController.currentBackStackEntryAsState() // 현재 백스택 상태를 가져옴
    val currentRoute = currentBackStackEntry?.destination?.route // 현재 화면의 route 이름 추출

    val weatherViewModel: WeatherViewModel = viewModel()

    Scaffold(
        bottomBar = {
            if (currentRoute != "splash") { // splash 화면을 제외한 경우에만 하단 네비게이션 바 표시
                BottomNavigationBar(navController)
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize() // 전체 화면 채우기
                .padding(innerPadding) // Scaffold 내부 여백 반영
        ) {
            NavHost(
                navController = navController, // NavController 지정
                startDestination = "splash", // 앱 실행 시 첫 화면 설정
                modifier = Modifier.fillMaxSize() // NavHost 영역 전체 화면 사용
            ) {
                // Splash 화면 라우트 등록
                composable("splash") {
                    SplashScreen(navController, weatherViewModel) // 전달
                }

                composable("home") {
                    HomeScreen(viewModel = weatherViewModel) // 전달
                }

                // 옷장 화면 라우트 등록
                composable("closet") {
                    ClosetScreen()
                }

                // 친구 화면 라우트 등록
                composable("friends") {
                    FriendsScreen(navController)
                }

                // 설정 화면 라우트 등록
                composable("settings") {
                    SettingScreen(navController)
                }

                // 회원가입 화면 라우트 등록
                composable("register") {
                    RegisterScreen(navController)
                }

                // 닉네임 편집 화면 라우트 등록
                composable("edit_nickname") {
                    EditNicknameScreen(navController)
                }

            }
        }
    }
}
