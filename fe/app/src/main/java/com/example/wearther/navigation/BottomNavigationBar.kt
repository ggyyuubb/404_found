// 📁 navigation/BottomNavigationBar.kt
// ✅ 앱 하단에 표시되는 네비게이션 바 컴포저블입니다.
// ✅ 홈, 옷장, 친구, 설정 화면으로 이동할 수 있으며 현재 선택된 탭을 시각적으로 표시합니다.

package com.example.wearther.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState

@Composable
fun BottomNavigationBar(navController: NavController) {
    // ✅ 하단 탭에 표시할 항목들을 정의
    // route: 화면 라우트 이름 (NavGraph에서 정의된 경로와 일치해야 함)
    // title: 탭 제목 (텍스트)
    // icon: 탭 아이콘
    val items = listOf(
        BottomNavItem("home", "홈", Icons.Default.Home), // 홈 화면
        BottomNavItem("closet", "옷장", Icons.Default.ShoppingCart), // 옷장
        BottomNavItem("friends", "친구", Icons.Default.Person), // 친구 목록
        BottomNavItem("settings", "설정", Icons.Default.Settings) // 설정
    )

    // ✅ 현재 백스택에서 가장 위에 있는 라우트를 가져옴
    val navBackStackEntry = navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry.value?.destination?.route

    // ✅ Material3 네비게이션 바 구성
    NavigationBar {
        items.forEach { item ->
            NavigationBarItem(
                icon = { Icon(item.icon, contentDescription = item.title) }, // 아이콘 표시
                label = { Text(item.title) }, // 텍스트 라벨
                selected = currentRoute == item.route, // 현재 선택된 라우트 여부
                onClick = {
                    navController.navigate(item.route) {
                        // ✅ "home" 라우트까지 백스택 유지 (이전 화면 복잡도 방지)
                        popUpTo("home") { inclusive = false }
                        // ✅ 중복 클릭 시 중복 인스턴스 방지
                        launchSingleTop = true
                    }
                }
            )
        }
    }
}

// ✅ 각 탭 항목을 나타내는 데이터 클래스
// - route: 화면 이동용 경로 이름
// - title: 하단에 표시될 텍스트
// - icon: 아이콘 벡터 리소스
data class BottomNavItem(
    val route: String,
    val title: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)
