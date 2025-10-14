package com.example.wearther.home.weather

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// 🌈 날씨 팔레트: 아이콘 색상 + 배경 브러시 세트
data class WeatherPalette(
    val iconColor: Color,
    val lightBrush: Brush,
    val darkBrush: Brush
)

// ✅ 날씨별 팔레트 매핑 - 실제 하늘색 기반
fun getWeatherPalette(main: String?): WeatherPalette {
    return when (main?.lowercase()) {
        "clear" -> WeatherPalette(
            iconColor = Color(0xFFFFA726), // 태양 - 밝은 오렌지
            lightBrush = Brush.verticalGradient(
                listOf(
                    Color(0xFF87CEEB), // 맑은 하늘색 (Sky Blue)
                    Color(0xFF4A90E2)  // 지평선 블루
                )
            ),
            darkBrush = Brush.verticalGradient(
                listOf(
                    Color(0xFF0C1445), // 밤하늘 상단
                    Color(0xFF1E3A5F)  // 밤하늘 하단
                )
            )
        )
        "clouds" -> WeatherPalette(
            iconColor = Color(0xFF78909C), // 구름 - 블루그레이
            lightBrush = Brush.verticalGradient(
                listOf(
                    Color(0xFFB0C4DE), // 흐린 하늘 상단 (Light Steel Blue)
                    Color(0xFF87CEEB)  // 흐린 하늘 하단
                )
            ),
            darkBrush = Brush.verticalGradient(
                listOf(
                    Color(0xFF2C3E50), // 어두운 구름
                    Color(0xFF34495E)
                )
            )
        )
        "rain", "drizzle" -> WeatherPalette(
            iconColor = Color(0xFF42A5F5), // 비 - 라이트 블루
            lightBrush = Brush.verticalGradient(
                listOf(
                    Color(0xFF778899), // 비 오는 하늘 상단 (Light Slate Gray)
                    Color(0xFF708090)  // 비 오는 하늘 하단 (Slate Gray)
                )
            ),
            darkBrush = Brush.verticalGradient(
                listOf(
                    Color(0xFF1C2833), // 비 내리는 밤
                    Color(0xFF283747)
                )
            )
        )
        "snow" -> WeatherPalette(
            iconColor = Color(0xFFFFFFFF), // 눈 - 화이트
            lightBrush = Brush.verticalGradient(
                listOf(
                    Color(0xFFE0F2F7), // 눈 오는 하늘 상단
                    Color(0xFFB3E5FC)  // 눈 오는 하늘 하단 (Light Blue)
                )
            ),
            darkBrush = Brush.verticalGradient(
                listOf(
                    Color(0xFF263238), // 눈 내리는 밤
                    Color(0xFF37474F)
                )
            )
        )
        "thunderstorm" -> WeatherPalette(
            iconColor = Color(0xFFFFD700), // 번개 - 골드
            lightBrush = Brush.verticalGradient(
                listOf(
                    Color(0xFF4A5568), // 폭풍우 하늘 상단
                    Color(0xFF2D3748)  // 폭풍우 하늘 하단
                )
            ),
            darkBrush = Brush.verticalGradient(
                listOf(
                    Color(0xFF0F1419), // 폭풍 밤
                    Color(0xFF1A202C)
                )
            )
        )
        "mist", "fog", "haze" -> WeatherPalette(
            iconColor = Color(0xFF90A4AE), // 안개 - 블루그레이
            lightBrush = Brush.verticalGradient(
                listOf(
                    Color(0xFFCFD8DC), // 안개 낀 하늘 상단
                    Color(0xFFB0BEC5)  // 안개 낀 하늘 하단
                )
            ),
            darkBrush = Brush.verticalGradient(
                listOf(
                    Color(0xFF37474F), // 어두운 안개
                    Color(0xFF455A64)
                )
            )
        )
        "pop" -> WeatherPalette(
            iconColor = Color(0xFF29B6F6), // 강수확률 - 하늘색
            lightBrush = Brush.verticalGradient(listOf(Color.Transparent, Color.Transparent)),
            darkBrush = Brush.verticalGradient(listOf(Color.Transparent, Color.Transparent))
        )
        "rainamount" -> WeatherPalette(
            iconColor = Color(0xFF0277BD), // 강수량 - 더 진한 블루
            lightBrush = Brush.verticalGradient(listOf(Color.Transparent, Color.Transparent)),
            darkBrush = Brush.verticalGradient(listOf(Color.Transparent, Color.Transparent))
        )
        "uv" -> WeatherPalette(
            iconColor = Color(0xFFE65100), // UV - 진한 주황색 (태양광과 구분)
            lightBrush = Brush.verticalGradient(listOf(Color.Transparent, Color.Transparent)),
            darkBrush = Brush.verticalGradient(listOf(Color.Transparent, Color.Transparent))
        )
        "wind" -> WeatherPalette(
            iconColor = Color(0xFF00ACC1), // 풍속 - 청록색 (바람)
            lightBrush = Brush.verticalGradient(listOf(Color.Transparent, Color.Transparent)),
            darkBrush = Brush.verticalGradient(listOf(Color.Transparent, Color.Transparent))
        )
        else -> WeatherPalette(
            iconColor = Color(0xFF42A5F5), // 기본 하늘색
            lightBrush = Brush.verticalGradient(
                listOf(
                    Color(0xFF87CEEB), // 기본 하늘 상단
                    Color(0xFF4A90E2)  // 기본 하늘 하단
                )
            ),
            darkBrush = Brush.verticalGradient(
                listOf(
                    Color(0xFF0C1445), // 기본 밤하늘
                    Color(0xFF1E3A5F)
                )
            )
        )
    }
}

// ✅ 배경 Composable
@Composable
fun WeatherBackground(
    weatherMain: String?,
    isDarkTheme: Boolean
) {
    val palette = getWeatherPalette(weatherMain)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(if (isDarkTheme) palette.darkBrush else palette.lightBrush)
    )
}