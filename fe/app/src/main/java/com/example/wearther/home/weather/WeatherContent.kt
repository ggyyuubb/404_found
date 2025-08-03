package com.example.wearther.home.weather

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun WeatherContent(
    data: WeatherResponse,
    locationText: String,
    sheetTextColor: Color,
    itemWidthDp: Dp = 70.dp,
    itemSpacingDp: Dp = 8.dp
) {
    val now = Instant.now().atZone(ZoneId.of("Asia/Seoul")).toLocalDateTime()
    val nowText = now.format(DateTimeFormatter.ofPattern("현재 시각: HH시 mm분"))
    val todayTemps = data.daily?.firstOrNull()

    val endTime = now.plusHours(24)
    val filteredHourly = data.hourly.filter {
        val forecastTime = Instant.ofEpochSecond(it.dt).atZone(ZoneId.of("Asia/Seoul")).toLocalDateTime()
        forecastTime.isAfter(now) && !forecastTime.isAfter(endTime)
    }

    val temps = filteredHourly.map { it.temp }
    val minTemp = temps.minOrNull() ?: 0.0
    val maxTemp = temps.maxOrNull() ?: 0.0
    val tempRange = (maxTemp - minTemp).takeIf { it >= 1.0 } ?: 5.0
    val listState = rememberLazyListState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 16.dp)
    ) {
        item {
            Text(
                text = locationText.ifBlank { "위치 정보를 불러오는 중..." },
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = sheetTextColor
                ),
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }

        item {
            Text(
                text = nowText,
                style = MaterialTheme.typography.bodyLarge.copy(color = sheetTextColor),
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }

        item { Spacer(modifier = Modifier.height(16.dp)) }

        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFBBDEFB))
            ) {
                val currentMain = data.current.weather.firstOrNull()?.main ?: "Unknown"
                val weatherEmoji = weatherToEmoji(currentMain)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "현재 ${String.format("%.1f", data.current.temp)}°C (체감 ${String.format("%.1f", data.current.feels_like)}°)",
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.Black
                            )
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        todayTemps?.let {
                            Text(
                                text = "최고 ${String.format("%.1f", it.temp.max)}° / 최저 ${String.format("%.1f", it.temp.min)}°",
                                style = MaterialTheme.typography.bodyLarge.copy(color = Color.DarkGray)
                            )
                        }
                    }
                    Text(
                        text = weatherEmoji,
                        style = MaterialTheme.typography.displayLarge,
                        modifier = Modifier.padding(start = 16.dp)
                    )
                }
            }
        }

        if (filteredHourly.isNotEmpty()) {
            item {
                LazyRow(
                    state = listState,
                    horizontalArrangement = Arrangement.spacedBy(itemSpacingDp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    itemsIndexed(filteredHourly) { _, forecast ->
                        Column(
                            modifier = Modifier
                                .width(itemWidthDp)
                                .padding(bottom = 60.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            val forecastTime = Instant.ofEpochSecond(forecast.dt)
                                .atZone(ZoneId.of("Asia/Seoul"))
                            val hourText = "${forecastTime.hour}시"
                            val rawMain = forecast.weather.firstOrNull()?.main ?: "Unknown"
                            val adjustedMain = when {
                                (forecast.pop * 100).toInt() >= 60 && (forecast.rain?.get("1h") ?: 0.0) >= 1.0 -> "Rain"
                                (forecast.pop * 100).toInt() in 40..59 -> "Drizzle"
                                rawMain.equals("Thunderstorm", ignoreCase = true) -> "Thunderstorm"
                                rawMain.equals("Mist", true) || rawMain.equals("Fog", true) || rawMain.equals("Haze", true) -> "Fog"
                                rawMain.equals("Wind", true) -> "Wind"
                                rawMain.equals("Clouds", true) -> "Clouds"
                                rawMain.equals("Clear", true) -> "Clear"
                                else -> "Clouds"
                            }
                            val weatherEmoji = weatherToEmoji(adjustedMain)

                            Text(hourText, style = MaterialTheme.typography.bodySmall, color = sheetTextColor)
                            Spacer(Modifier.height(4.dp))
                            Text(weatherEmoji, style = MaterialTheme.typography.headlineMedium)
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = String.format("%.1f°C", forecast.temp),
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                color = sheetTextColor
                            )
                        }
                    }
                }
            }

            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                        .clipToBounds()
                ) {
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .background(Color.LightGray.copy(alpha = 0.1f))
                    )

                    Canvas(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp)
                            .align(Alignment.Center)
                    ) {
                        val itemWidthPx = itemWidthDp.toPx()
                        val itemSpacingPx = itemSpacingDp.toPx()
                        val scrollOffsetPx = listState.firstVisibleItemScrollOffset.toFloat()
                        val firstVisibleIndex = listState.firstVisibleItemIndex
                        val graphHeight = size.height
                        val paddingTop = 20f
                        val paddingBottom = 10f

                        val points = filteredHourly.mapIndexed { index, hourly ->
                            val normalized = (hourly.temp.toFloat() - minTemp.toFloat()) / tempRange.toFloat()
                            val x = index * (itemWidthPx + itemSpacingPx) + (itemWidthPx / 2) - scrollOffsetPx - firstVisibleIndex * (itemWidthPx + itemSpacingPx)
                            val y = (graphHeight - paddingTop - paddingBottom) * (1f - normalized.coerceIn(0f, 1f)) + paddingTop
                            Offset(x, y)
                        }.filter { it.x in -itemWidthPx..size.width + itemWidthPx }

                        for (i in 0 until points.size - 1) {
                            drawLine(
                                color = Color.Blue.copy(alpha = 0.7f),
                                start = points[i],
                                end = points[i + 1],
                                strokeWidth = 4f
                            )
                        }

                        points.forEach { point ->
                            drawCircle(Color.Blue, radius = 8f, center = point)
                        }
                    }
                }
            }

            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(itemSpacingDp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    items(filteredHourly) { forecast ->
                        Box(
                            modifier = Modifier.width(itemWidthDp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "💧${(forecast.pop * 100).toInt()}%",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = sheetTextColor
                                )
                                if ((forecast.pop * 100).toInt() >= 30 && (forecast.rain?.get("1h") ?: 0.0) > 0.0) {
                                    Text(
                                        text = "🌧️${String.format("%.1f", forecast.rain?.get("1h") ?: 0.0)}mm",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = sheetTextColor
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        item {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp)) {
                Text(
                    text = "📅 주간 날씨",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = sheetTextColor,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                data.daily?.take(7)?.forEachIndexed { index, day ->
                    val dayOfWeek = Instant.ofEpochSecond(day.dt)
                        .atZone(ZoneId.of("Asia/Seoul"))
                        .dayOfWeek
                        .toString()
                        .substring(0, 3)

                    val displayDay = if (index == 0) "오늘" else dayOfWeek
                    val emoji = weatherToEmoji(day.weather.firstOrNull()?.main ?: "Clear")

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = displayDay,
                            style = MaterialTheme.typography.bodyLarge.copy(color = sheetTextColor)
                        )
                        Text(
                            text = emoji,
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Text(
                            text = "최고 ${String.format("%.1f", day.temp.max)}° / 최저 ${String.format("%.1f", day.temp.min)}°",
                            style = MaterialTheme.typography.bodyLarge.copy(color = sheetTextColor)
                        )
                    }
                }
            }
        }
    }
}
