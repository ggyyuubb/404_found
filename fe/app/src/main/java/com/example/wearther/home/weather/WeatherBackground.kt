package com.example.wearther.home.weather

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

@Composable
fun WeatherBackground(
    weatherMain: String?,
    isDarkTheme: Boolean
) {
    val brush = if (isDarkTheme) {
        // 🌙 다크모드 → 어두운 톤
        when (weatherMain) {
            "Clear" -> Brush.verticalGradient(listOf(Color(0xFF1E3C72), Color(0xFF2A5298))) // 딥 블루
            "Clouds" -> Brush.verticalGradient(listOf(Color(0xFF232526), Color(0xFF414345))) // 다크 그레이
            "Rain" -> Brush.verticalGradient(listOf(Color(0xFF0F2027), Color(0xFF2C5364)))   // 블루-그레이
            "Snow" -> Brush.verticalGradient(listOf(Color(0xFF373B44), Color(0xFF4286f4)))   // 어두운 블루+화이트
            else -> Brush.verticalGradient(listOf(Color(0xFF141E30), Color(0xFF243B55)))     // 기본 네이비 톤
        }
    } else {
        // ☀️ 라이트모드 → 밝은 톤
        when (weatherMain) {
            "Clear" -> Brush.verticalGradient(listOf(Color(0xFF87CEFA), Color.White))       // 하늘색
            "Clouds" -> Brush.verticalGradient(listOf(Color.LightGray, Color.Gray))         // 흐림
            "Rain" -> Brush.verticalGradient(listOf(Color(0xFF3A6073), Color(0xFF16222A)))  // 비
            "Snow" -> Brush.verticalGradient(listOf(Color(0xFFE0F7FA), Color.White))        // 눈
            else -> Brush.verticalGradient(listOf(Color(0xFF2193b0), Color(0xFF6dd5ed)))    // 기본 블루
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(brush)
    )
}
