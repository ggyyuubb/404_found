// 📁 ui/screens/HomeScreen.kt
// ✅ 앱의 메인 화면. 현재 위치를 기반으로 날씨 정보를 불러오고, 바텀시트에는 추천 코디를 표시하는 화면입니다.
// ✅ 위치 권한이 필요하며, 날씨와 추천 데이터를 ViewModel을 통해 가져옵니다.

package com.example.wearther.home.weather

import android.content.Context
import android.util.Log
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.wearther.home.recommendation.HomeBottomSheetContent
import com.example.wearther.home.recommendation.RecommendationViewModel
import kotlinx.coroutines.launch
import androidx.lifecycle.viewmodel.compose.viewModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: WeatherViewModel, // ✅ NavGraph에서 받은 거 그대로
    recommendationViewModel: RecommendationViewModel = viewModel()
) {
    val context = LocalContext.current
    val isDarkTheme = isSystemInDarkTheme()

    // ✅ 테마에 따른 바텀시트 색상 설정
    val sheetBackgroundColor = if (isDarkTheme) Color(0xFF333333) else Color(0xFFF0F0F0)
    val sheetTextColor = if (isDarkTheme) Color.White else Color.Black

    // ✅ 날씨 상태 및 위치 텍스트 관찰
    val weather by viewModel.weatherData.collectAsState()
    val locationText by viewModel.locationText.collectAsState()

    // ✅ 스낵바 상태 및 코루틴 스코프 정의
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    // ✅ 현재 위치 권한 여부 확인
    val locationGranted = hasLocationPermission(context)

    // ✅ 권한이 없는 경우 안내 메시지를 표시
    if (!locationGranted) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("위치 권한이 필요합니다.", color = Color.Red)
        }
        return // 컴포저블 실행 중단
    }

    // ✅ 권한이 허용된 경우 날씨와 추천 데이터를 최초 1회 불러오기
    LaunchedEffect(locationGranted) {
        if (locationGranted) {
            val sharedPreferences = context.getSharedPreferences("myPrefs", Context.MODE_PRIVATE)
            val jwt = sharedPreferences.getString("jwt", null)

            // ✅ 추천 요청 - 도시명은 주소에서 추출
            if (!jwt.isNullOrEmpty()) {
                val city = locationText.split(" ").firstOrNull() ?: "Seoul"
                Log.d("JWT_HOME", "✅ JWT: $jwt, 📍도시: $city")
                recommendationViewModel.fetchRecommendations(jwt, city)
            } else {
                Log.e("JWT_HOME", "❌ JWT 없음")
            }
        }
    }

    // ✅ 바텀시트 상태 정의 (숨김 금지)
    val bottomSheetState = rememberStandardBottomSheetState(
        initialValue = SheetValue.PartiallyExpanded,
        skipHiddenState = false // 숨김 가능하지만 아래에서 차단
    )

    // ✅ 바텀시트가 숨겨졌을 경우 다시 일부 확장
    LaunchedEffect(bottomSheetState.currentValue) {
        if (bottomSheetState.currentValue == SheetValue.Hidden) {
            bottomSheetState.partialExpand()
        }
    }

    // ✅ 바텀시트가 포함된 전체 화면 구조
    BottomSheetScaffold(
        scaffoldState = rememberBottomSheetScaffoldState(bottomSheetState = bottomSheetState),
        sheetPeekHeight = 80.dp, // 바텀시트 기본 손잡이 높이
        sheetContent = {
            HomeBottomSheetContent(
                textColor = sheetTextColor,
                viewModel = recommendationViewModel
            )
        },
        sheetContainerColor = sheetBackgroundColor,
        sheetShape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            // ✅ 상단 리프레시 버튼 및 날짜
            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = {
                    coroutineScope.launch {
                        viewModel.clearWeather()
                        getCurrentLocation(context)?.let { location ->
                            viewModel.fetchWeather(location.latitude, location.longitude)
                            viewModel.fetchAddress(context, location.latitude, location.longitude)
                            snackbarHostState.showSnackbar("새로고침 되었습니다")
                        }
                    }
                }) {
                    Icon(Icons.Default.Refresh, contentDescription = "새로고침")
                }

                Spacer(modifier = Modifier.weight(1f))

                // ✅ 오늘 날짜 표시
                Text(
                    text = java.time.LocalDate.now().format(
                        java.time.format.DateTimeFormatter.ofPattern("yyyy년 M월 d일")
                    ),
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
                )
            }

            // ✅ 날씨 정보 표시
            weather?.let {
                WeatherContent(it, locationText, sheetTextColor)
            } ?: Text("날씨 정보를 불러오는 중입니다...", color = sheetTextColor)
        }
    }
}
