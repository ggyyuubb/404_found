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

// ✅ 날씨별 팔레트 매핑 - 색상 비율 증가
fun getWeatherPalette(main: String?): WeatherPalette {
    return when (main?.lowercase()) {
        "clear" -> WeatherPalette(
            iconColor = Color(0xFFFFA726), // 태양 - 밝은 오렌지
            lightBrush = Brush.verticalGradient(
                listOf(
                    Color(0xFFE3F2FD),     // 아주 연한 하늘색
                    Color(0xFF87CEEB),     // 맑은 하늘색
                    Color(0xFF4A90E2)      // 진한 하늘색
                )
            ),
            darkBrush = Brush.verticalGradient(
                listOf(
                    Color.Black,
                    Color(0xFF1E3A5F)
                )
            )
        )
        "clouds" -> WeatherPalette(
            iconColor = Color(0xFF78909C), // 구름 - 블루그레이
            lightBrush = Brush.verticalGradient(
                listOf(
                    Color(0xFFECEFF1),     // 연한 그레이
                    Color(0xFFB0C4DE),     // 흐린 하늘색
                    Color(0xFF87CEEB)      // 하늘색
                )
            ),
            darkBrush = Brush.verticalGradient(
                listOf(
                    Color.Black,
                    Color(0xFF34495E)
                )
            )
        )
        "rain", "drizzle" -> WeatherPalette(
            iconColor = Color(0xFF42A5F5), // 비 - 라이트 블루
            lightBrush = Brush.verticalGradient(
                listOf(
                    Color(0xFFE1F5FE),     // 연한 블루
                    Color(0xFF778899),     // 비 오는 하늘
                    Color(0xFF708090)      // 어두운 슬레이트
                )
            ),
            darkBrush = Brush.verticalGradient(
                listOf(
                    Color.Black,
                    Color(0xFF283747)
                )
            )
        )
        "snow" -> WeatherPalette(
            iconColor = Color(0xFFFFFFFF), // 눈 - 화이트
            lightBrush = Brush.verticalGradient(
                listOf(
                    Color(0xFFF1F8FF),     // 거의 흰색
                    Color(0xFFE0F2F7),     // 눈 오는 하늘
                    Color(0xFFB3E5FC)      // 연한 블루
                )
            ),
            darkBrush = Brush.verticalGradient(
                listOf(
                    Color.Black,
                    Color(0xFF37474F)
                )
            )
        )
        "thunderstorm" -> WeatherPalette(
            iconColor = Color(0xFFFFD700), // 번개 - 골드
            lightBrush = Brush.verticalGradient(
                listOf(
                    Color(0xFFCFD8DC),     // 연한 그레이
                    Color(0xFF4A5568),     // 폭풍우 하늘
                    Color(0xFF2D3748)      // 어두운 폭풍
                )
            ),
            darkBrush = Brush.verticalGradient(
                listOf(
                    Color.Black,
                    Color(0xFF1A202C)
                )
            )
        )
        "mist", "fog", "haze" -> WeatherPalette(
            iconColor = Color(0xFF90A4AE), // 안개 - 블루그레이
            lightBrush = Brush.verticalGradient(
                listOf(
                    Color(0xFFF5F5F5),     // 아주 연한 그레이
                    Color(0xFFCFD8DC),     // 안개 낀 하늘
                    Color(0xFFB0BEC5)      // 그레이블루
                )
            ),
            darkBrush = Brush.verticalGradient(
                listOf(
                    Color.Black,
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
            iconColor = Color(0xFFE65100), // UV - 진한 주황색
            lightBrush = Brush.verticalGradient(listOf(Color.Transparent, Color.Transparent)),
            darkBrush = Brush.verticalGradient(listOf(Color.Transparent, Color.Transparent))
        )
        "wind" -> WeatherPalette(
            iconColor = Color(0xFF00ACC1), // 풍속 - 청록색
            lightBrush = Brush.verticalGradient(listOf(Color.Transparent, Color.Transparent)),
            darkBrush = Brush.verticalGradient(listOf(Color.Transparent, Color.Transparent))
        )
        else -> WeatherPalette(
            iconColor = Color(0xFF42A5F5), // 기본 하늘색
            lightBrush = Brush.verticalGradient(
                listOf(
                    Color(0xFFE3F2FD),     // 아주 연한 하늘색
                    Color(0xFF87CEEB),     // 기본 하늘색
                    Color(0xFF4A90E2)      // 진한 하늘색
                )
            ),
            darkBrush = Brush.verticalGradient(
                listOf(
                    Color.Black,
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