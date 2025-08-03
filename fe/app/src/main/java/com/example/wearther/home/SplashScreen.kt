// 📁 ui/screens/SplashScreen.kt
// ✅ 앱 시작 시 날씨를 요청하며, 최소 1초간 로고를 표시합니다.
//    이후 홈 화면("home")으로 자동 이동하며, 스플래시 화면은 백스택에서 제거됩니다.

package com.example.wearther.closet.upload

import android.content.Context
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.wearther.R
import com.example.wearther.home.weather.WeatherViewModel
import com.example.wearther.home.weather.getCurrentLocation
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    navController: NavController,
    viewModel: WeatherViewModel = viewModel() // ✅ WeatherViewModel 주입
) {
    val context = LocalContext.current
    val weather by viewModel.weatherData.collectAsState()

    var isLoading by remember { mutableStateOf(false) }
    val startTime = remember { System.currentTimeMillis() } // ⏱ 시작 시각 저장

    // ✅ 날씨 요청 시작 (1회만)
    LaunchedEffect(Unit) {
        if (!isLoading) {
            isLoading = true
            val location = getCurrentLocation(context)
            if (location != null) {
                viewModel.fetchWeather(location.latitude, location.longitude)
                viewModel.fetchAddress(context, location.latitude, location.longitude)
            }
        }
    }

    // ✅ 날씨가 준비되면 → 최소 1초 이상 지났는지 확인 후 home으로 이동
    LaunchedEffect(weather) {
        if (weather != null) {
            val elapsed = System.currentTimeMillis() - startTime
            val delayNeeded = 1000L - elapsed
            if (delayNeeded > 0) delay(delayNeeded) // ⏳ 1초 미만이면 기다림

            navController.navigate("home") {
                popUpTo("splash") { inclusive = true }
            }
        }
    }

    // ✅ 화면 중앙에 앱 로고를 표시하는 UI
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.splash_logo),
            contentDescription = "Splash Logo",
            modifier = Modifier.size(500.dp)
        )
    }
}
