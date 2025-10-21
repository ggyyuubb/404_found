package com.example.wearther.home.weather

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.* // Outlined 아이콘 불러오기
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

// ✅ 날씨/지표 → 아이콘 매핑
fun weatherToEmoji(main: String): ImageVector {
    return when (main.lowercase()) {
        // 날씨 상태
        "clear" -> Icons.Outlined.WbSunny         // ☀️ 맑음
        "clouds" -> Icons.Outlined.Cloud          // ☁️ 구름
        "rain" -> Icons.Outlined.Umbrella         // ☔ 비 (우산이 제일 직관적)
        "drizzle" -> Icons.Outlined.WaterDrop     // 💧 이슬비
        "thunderstorm" -> Icons.Outlined.FlashOn  // ⚡ 천둥
        "snow" -> Icons.Outlined.AcUnit           // ❄️ 눈
        "mist", "fog", "haze" -> Icons.Outlined.BlurOn // 🌫️ 안개
        "wind", "breeze" -> Icons.Outlined.Air    // 💨 바람

        // 추가 지표
        "pop" -> Icons.Outlined.WaterDrop         // 💧 강수확률
        "rainamount" -> Icons.Outlined.Opacity    // 💦 강수량
        "uv" -> Icons.Outlined.LightMode          // 🔆 자외선
        "humidity" -> Icons.Outlined.Opacity      // 💦 습도
        "windspeed" -> Icons.Outlined.Air         // 💨 풍속

        else -> Icons.Outlined.HelpOutline        // ❓ 알 수 없음
    }
}