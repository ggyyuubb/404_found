package com.example.wearther.home.weather

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.wearther.R

@Composable
fun CurrentWeatherCard(data: WeatherResponse) {
    val todayTemps = data.daily?.firstOrNull()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.0f) // 투명도 20%
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
                    painter = painterResource(
                        id = getWeatherIconRes(data.current.weather.firstOrNull()?.main ?: "Clear")
                    ),
                    contentDescription = "현재 날씨",
                    modifier = Modifier.size(80.dp),
                    tint = Color.Unspecified
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
                    Icon(painter = painterResource(R.drawable.uv), contentDescription = "UV", tint = Color.Unspecified, modifier = Modifier.size(28.dp))
                    Text("UV ${data.current.uvi.toInt()}")
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(painter = painterResource(R.drawable.humidity), contentDescription = "습도", tint = Color.Unspecified, modifier = Modifier.size(28.dp))
                    Text("${data.current.humidity}%")
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(painter = painterResource(R.drawable.wind), contentDescription = "바람", tint = Color.Unspecified, modifier = Modifier.size(28.dp))
                    Text("${data.current.wind_speed} m/s")
                }
            }
        }
    }
}

fun getWeatherIconRes(main: String): Int {
    return when (main.lowercase()) {
        "clear" -> R.drawable.sun
        "clouds" -> R.drawable.cloud_2
        "rain" -> R.drawable.rain_2
        "drizzle" -> R.drawable.rain_1
        "thunderstorm" -> R.drawable.thunder
        "snow" -> R.drawable.snow
        "mist", "fog", "haze", "smoke", "dust" -> R.drawable.fog
        else -> R.drawable.weather_unknown
    }
}
