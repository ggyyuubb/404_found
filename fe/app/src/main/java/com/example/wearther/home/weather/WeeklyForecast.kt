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
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Outlined.CalendarMonth,
                contentDescription = null,
                tint = textColor.copy(alpha = 0.8f),
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "주간 날씨",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = textColor
            )
        }

        if (dailyWeather.isNullOrEmpty()) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    text = "날씨 정보를 불러올 수 없습니다",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    modifier = Modifier.padding(20.dp)
                )
            }
        } else {
            dailyWeather.take(7).forEachIndexed { index, day ->
                WeeklyForecastCard(
                    day = day,
                    index = index,
                    textColor = textColor
                )
            }
        }

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
        .getDisplayName(java.time.format.TextStyle.SHORT, java.util.Locale.KOREAN)

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
            .padding(horizontal = 16.dp, vertical = 6.dp),
        colors = CardDefaults.cardColors(
            containerColor = when (index) {
                0 -> textColor.copy(alpha = 0.08f) // 오늘 은은하게 강조
                else -> textColor.copy(alpha = 0.03f)
            }
        ),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 0.dp // ✅ 그림자 제거
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // ✅ 왼쪽: 날짜
            Text(
                text = displayDay,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = if (index == 0) FontWeight.Bold else FontWeight.Medium
                ),
                color = textColor,
                modifier = Modifier.width(60.dp)
            )

            // ✅ 중앙: 날씨 아이콘 + 강수확률 (고정 너비로 정렬)
            Box(
                modifier = Modifier.width(120.dp), // ✅ 고정 너비
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = weatherIcon,
                        contentDescription = "날씨",
                        tint = textColor,
                        modifier = Modifier.size(32.dp)
                    )

                    // ✅ 강수확률 표시 영역 (항상 동일한 공간 차지)
                    Box(
                        modifier = Modifier.width(50.dp), // ✅ 고정 너비
                        contentAlignment = Alignment.CenterStart
                    ) {
                        if (precipitationPercent > 0) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(start = 8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.WaterDrop,
                                    contentDescription = null,
                                    tint = Color(0xFF4A90E2),
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = "$precipitationPercent%",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = textColor.copy(alpha = 0.6f)
                                )
                            }
                        }
                    }
                }
            }

            // ✅ 오른쪽: 온도
            Row(
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.width(70.dp)
            ) {
                Text(
                    text = "${String.format("%.0f", day.temp.max)}°",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = textColor
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "/",
                    style = MaterialTheme.typography.bodyMedium,
                    color = textColor.copy(alpha = 0.3f)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "${String.format("%.0f", day.temp.min)}°",
                    style = MaterialTheme.typography.bodyMedium,
                    color = textColor.copy(alpha = 0.5f)
                )
            }
        }
    }
}