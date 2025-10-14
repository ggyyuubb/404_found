package com.example.wearther.home.weather

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun CurrentWeatherCard(data: WeatherResponse) {
    val todayTemps = data.daily?.firstOrNull()
    val mainWeather = data.current.weather.firstOrNull()?.main ?: "clear"
    val palette = getWeatherPalette(mainWeather)   // ✅ 팔레트 가져오기

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.0f) // 투명
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 🔹 아이콘 + 현재 기온
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = weatherToEmoji(mainWeather),   // 중앙 아이콘 함수
                    contentDescription = "현재 날씨",
                    modifier = Modifier.size(80.dp),
                    tint = palette.iconColor                    // ✅ 팔레트 색상
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = "${data.current.temp.toInt()}°C",
                    style = MaterialTheme.typography.displayLarge.copy(fontWeight = FontWeight.Bold),
                    color = Color.Black
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 🔹 체감, 최고, 최저
            Text(
                text = "체감 ${data.current.feels_like.toInt()}° · 최고 ${todayTemps?.temp?.max?.toInt()}° / 최저 ${todayTemps?.temp?.min?.toInt()}°",
                style = MaterialTheme.typography.bodyLarge.copy(color = Color.DarkGray)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // 🔹 생활 지표 (UV, 습도, 바람)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    val uvPalette = getWeatherPalette("uv")
                    Icon(
                        imageVector = weatherToEmoji("uv"),
                        contentDescription = "UV",
                        tint = uvPalette.iconColor,
                        modifier = Modifier.size(28.dp)
                    )
                    Text("UV ${data.current.uvi.toInt()}")
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    val humidityPalette = getWeatherPalette("humidity")
                    Icon(
                        imageVector = weatherToEmoji("humidity"),
                        contentDescription = "습도",
                        tint = humidityPalette.iconColor,
                        modifier = Modifier.size(28.dp)
                    )
                    Text("${data.current.humidity}%")
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    val windPalette = getWeatherPalette("wind")
                    Icon(
                        imageVector = weatherToEmoji("wind"),
                        contentDescription = "바람",
                        tint = windPalette.iconColor,
                        modifier = Modifier.size(28.dp)
                    )
                    Text("${data.current.wind_speed} m/s")
                }
            }
        }
    }
}
