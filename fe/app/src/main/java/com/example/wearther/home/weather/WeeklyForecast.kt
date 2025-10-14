package com.example.wearther.home.weather

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import java.time.Instant
import java.time.ZoneId

@Composable
fun WeeklyForecast(
    dailyWeather: List<DailyWeather>?,
    textColor: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
    ) {
        // ✅ 섹션 제목
        Text(
            text = "📅 7일간 날씨 예보",
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            color = textColor,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )

        // ✅ 데이터 없을 때 에러 메시지
        if (dailyWeather.isNullOrEmpty()) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Text(
                    text = "주간 예보 데이터를 불러올 수 없습니다.\nAPI 설정을 확인해주세요.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    modifier = Modifier.padding(16.dp)
                )
            }
        } else {
            // ✅ 주간 예보 카드들
            dailyWeather.take(7).forEachIndexed { index, day ->
                WeeklyForecastCard(
                    day = day,
                    index = index,
                    textColor = textColor
                )
            }
        }

        // ✅ 바텀시트와 겹치지 않도록 여유 공간
        Spacer(modifier = Modifier.height(100.dp))
    }
}

@Composable
private fun WeeklyForecastCard(
    day: DailyWeather,
    index: Int,
    textColor: Color
) {
    val dayOfWeek = Instant.ofEpochSecond(day.dt)
        .atZone(ZoneId.of("Asia/Seoul"))
        .dayOfWeek
        .toString()
        .substring(0, 3)

    val displayDay = when (index) {
        0 -> "오늘"
        1 -> "내일"
        else -> dayOfWeek
    }

    val weatherIcon = weatherToEmoji(day.weather.firstOrNull()?.main ?: "Clear")
    val precipitationPercent = (day.pop * 100).toInt()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = when (index) {
                0 -> MaterialTheme.colorScheme.primaryContainer // 오늘 강조
                else -> MaterialTheme.colorScheme.surfaceVariant
            }
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // ✅ 왼쪽: 날짜 + 강수확률
            Column {
                Text(
                    text = displayDay,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = if (index == 0) FontWeight.Bold else FontWeight.Normal
                    ),
                    color = textColor
                )
                if (precipitationPercent > 0) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Outlined.WaterDrop,
                            contentDescription = "강수확률",
                            tint = textColor.copy(alpha = 0.7f),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "$precipitationPercent%",
                            style = MaterialTheme.typography.bodySmall,
                            color = textColor.copy(alpha = 0.7f)
                        )
                    }
                }
            }

            // ✅ 중앙: 날씨 아이콘
            Icon(
                imageVector = weatherIcon,
                contentDescription = "날씨 아이콘",
                tint = Color.Unspecified,
                modifier = Modifier
                    .padding(horizontal = 8.dp)
                    .size(28.dp)
            )

            // ✅ 오른쪽: 온도 정보
            Column(
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = "${String.format("%.0f", day.temp.max)}°",
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                    color = textColor
                )
                Text(
                    text = "${String.format("%.0f", day.temp.min)}°",
                    style = MaterialTheme.typography.bodyMedium,
                    color = textColor.copy(alpha = 0.7f)
                )
            }
        }
    }
}
