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
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.wearther.home.recommendation.HomeBottomSheetContent
import com.example.wearther.home.recommendation.RecommendationViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    recommendationViewModel: RecommendationViewModel = viewModel()
) {
    val context = LocalContext.current
    val isDarkTheme = isSystemInDarkTheme()

    // ✅ WeatherViewModel 생성
    val weatherViewModel: WeatherViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                return WeatherViewModel(context) as T
            }
        }
    )

    val sheetBackgroundColor = if (isDarkTheme) Color(0xFF333333) else Color(0xFFF0F0F0)
    val sheetTextColor = if (isDarkTheme) Color.White else Color.Black

    val weather by weatherViewModel.weatherData.collectAsState()
    val locationText by weatherViewModel.locationText.collectAsState()
    val savedLocations by weatherViewModel.savedLocations.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    val locationGranted = hasLocationPermission(context)

    if (!locationGranted) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("위치 권한이 필요합니다.", color = Color.Red)
        }
        return
    }

    // 최초 실행 시
    LaunchedEffect(locationGranted) {
        if (locationGranted) {
            weatherViewModel.fetchCurrentLocationWeather()
            val sharedPreferences = context.getSharedPreferences("myPrefs", Context.MODE_PRIVATE)
            val jwt = sharedPreferences.getString("jwt", null)

            if (!jwt.isNullOrEmpty()) {
                val city = locationText.split(" ").firstOrNull() ?: "Seoul"
                Log.d("JWT_HOME", "✅ JWT: $jwt, 📍도시: $city")
                recommendationViewModel.fetchRecommendations(jwt, city)
            } else {
                Log.e("JWT_HOME", "❌ JWT 없음")
            }
        }
    }

    val bottomSheetState = rememberStandardBottomSheetState(
        initialValue = SheetValue.PartiallyExpanded,
        skipHiddenState = false
    )

    LaunchedEffect(bottomSheetState.currentValue) {
        if (bottomSheetState.currentValue == SheetValue.Hidden) {
            bottomSheetState.partialExpand()
        }
    }

    BottomSheetScaffold(
        scaffoldState = rememberBottomSheetScaffoldState(bottomSheetState = bottomSheetState),
        sheetPeekHeight = 80.dp,
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
        Box(modifier = Modifier.fillMaxSize()) {
            // 🔹 배경 먼저 그리기
            WeatherBackground(
                weatherMain = weather?.current?.weather?.firstOrNull()?.main,
                isDarkTheme = isDarkTheme
            )

            // 🔹 실제 내용
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(16.dp)
            ) {
                Row(
                    Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = {
                        coroutineScope.launch {
                            weatherViewModel.clearWeather()
                            getCurrentLocation(context)?.let { location ->
                                weatherViewModel.fetchWeather(location.latitude, location.longitude)
                                weatherViewModel.fetchAddress(context, location.latitude, location.longitude)

                                val sharedPreferences = context.getSharedPreferences("myPrefs", Context.MODE_PRIVATE)
                                val jwt = sharedPreferences.getString("jwt", null)
                                if (!jwt.isNullOrEmpty()) {
                                    val city = locationText.split(" ").firstOrNull() ?: "Seoul"
                                    recommendationViewModel.clearRecommendations()
                                    recommendationViewModel.fetchRecommendations(jwt, city)
                                }

                                snackbarHostState.showSnackbar("새로고침 되었습니다")
                            }
                        }
                    }) {
                        Icon(Icons.Default.Refresh, contentDescription = "새로고침")
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    Text(
                        text = java.time.LocalDate.now().format(
                            java.time.format.DateTimeFormatter.ofPattern("yyyy년 M월 d일")
                        ),
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                        color = sheetTextColor
                    )
                }

                weather?.let {
                    WeatherContent(
                        data = it,
                        locationText = locationText,
                        sheetTextColor = sheetTextColor,
                        savedLocations = savedLocations,
                        onLocationSelect = { location ->
                            weatherViewModel.selectLocation(location)
                            coroutineScope.launch {
                                val sharedPreferences = context.getSharedPreferences("myPrefs", Context.MODE_PRIVATE)
                                val jwt = sharedPreferences.getString("jwt", null)
                                if (!jwt.isNullOrEmpty()) {
                                    val city = location.name.split(",").firstOrNull()?.trim() ?: "Seoul"
                                    recommendationViewModel.clearRecommendations()
                                    recommendationViewModel.fetchRecommendations(jwt, city)
                                }
                            }
                        },
                        onAddLocation = { newLocation -> weatherViewModel.addLocation(newLocation) },
                        onDeleteLocation = { location -> weatherViewModel.deleteLocation(location.id) },
                        onSearchLocation = { query -> coroutineScope.async { weatherViewModel.searchLocations(query) }.await() }
                    )
                } ?: Text("날씨 정보를 불러오는 중입니다...", color = sheetTextColor)
            }
        }
    }
}
