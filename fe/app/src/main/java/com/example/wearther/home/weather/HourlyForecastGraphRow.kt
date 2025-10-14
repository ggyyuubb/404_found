package com.example.wearther.home.weather

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import java.time.Instant
import java.time.ZoneId

@Composable
fun HourlyForecastGraphRow(
    hourlyData: List<HourlyWeather>,
    sheetTextColor: Color,
    itemWidthDp: Dp = 70.dp,
    itemSpacingDp: Dp = 8.dp,
) {
    val listState = rememberLazyListState()

    val temps = hourlyData.map { it.temp }
    val minTemp = temps.minOrNull() ?: 0.0
    val maxTemp = temps.maxOrNull() ?: 0.0
    val tempRange = (maxTemp - minTemp).takeIf { it >= 1.0 } ?: 5.0

    val itemWidthPx = with(LocalDensity.current) { itemWidthDp.toPx() }
    val itemSpacingPx = with(LocalDensity.current) { itemSpacingDp.toPx() }
    val graphHeight = with(LocalDensity.current) { 60.dp.toPx() }
    val paddingTop = 10f
    val paddingBottom = 10f

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Color.White.copy(alpha = 0.15f),
                RoundedCornerShape(16.dp)
            )
            .padding(vertical = 8.dp)
    ) {
        LazyRow(
            state = listState,
            horizontalArrangement = Arrangement.spacedBy(itemSpacingDp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 8.dp)
        ) {
            itemsIndexed(hourlyData) { index, forecast ->
                val forecastTime = Instant.ofEpochSecond(forecast.dt)
                    .atZone(ZoneId.of("Asia/Seoul"))
                val hourText = "${forecastTime.hour}시"
                val weatherMain = forecast.weather.firstOrNull()?.main ?: "Clear"
                val palette = getWeatherPalette(weatherMain)   // ✅ 팔레트

                Column(
                    modifier = Modifier
                        .width(itemWidthDp)
                        .padding(horizontal = 4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        hourText,
                        style = MaterialTheme.typography.bodySmall,
                        color = sheetTextColor
                    )
                    Spacer(Modifier.height(4.dp))

                    // ✅ 날씨 아이콘 (팔레트 색상 적용)
                    Icon(
                        imageVector = weatherToEmoji(weatherMain),
                        contentDescription = "날씨 아이콘",
                        tint = palette.iconColor,
                        modifier = Modifier.size(32.dp)
                    )

                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = String.format("%.1f°C", forecast.temp),
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                        color = sheetTextColor
                    )

                    Spacer(Modifier.height(8.dp))
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.height(48.dp)
                    ) {
                        // ✅ 비 올 확률
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            val popPalette = getWeatherPalette("pop")
                            Icon(
                                imageVector = weatherToEmoji("pop"),
                                contentDescription = "강수확률",
                                tint = popPalette.iconColor,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${(forecast.pop * 100).toInt()}%",
                                style = MaterialTheme.typography.bodySmall,
                                color = sheetTextColor
                            )
                        }

                        Spacer(Modifier.height(4.dp))

                        // ✅ 강수량
                        val isRainyWeather = weatherMain.lowercase() in listOf("rain", "drizzle", "thunderstorm", "snow")
                        if (isRainyWeather) {
                            val currentRain = forecast.rain?.oneHour ?: 0.0
                            if (currentRain > 0.0) {
                                val prevRain = hourlyData.getOrNull(index - 1)?.rain?.oneHour ?: 0.0
                                val nextRain = hourlyData.getOrNull(index + 1)?.rain?.oneHour ?: 0.0
                                val totalRain = currentRain + prevRain + nextRain

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    val rainPalette = getWeatherPalette("rainamount")
                                    Icon(
                                        imageVector = weatherToEmoji("rainamount"),
                                        contentDescription = "강수량",
                                        tint = rainPalette.iconColor,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "${String.format("%.1f", totalRain)}mm",
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

        // ⬇️ 온도 그래프 (변경 없음)
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
                .padding(8.dp)
        ) {
            val scrollOffsetPx = listState.firstVisibleItemScrollOffset.toFloat()
            val firstVisibleIndex = listState.firstVisibleItemIndex

            val points = hourlyData.mapIndexed { index, forecast ->
                val normalized = (forecast.temp - minTemp) / tempRange
                val x = index * (itemWidthPx + itemSpacingPx) + (itemWidthPx / 2) -
                        firstVisibleIndex * (itemWidthPx + itemSpacingPx) - scrollOffsetPx
                val y = (graphHeight - paddingTop - paddingBottom) *
                        (1f - normalized.toFloat().coerceIn(0f, 1f)) + paddingTop
                Offset(x, y)
            }.filter { it.x in -itemWidthPx..size.width + itemWidthPx }

            val path = androidx.compose.ui.graphics.Path().apply {
                if (points.isNotEmpty()) {
                    moveTo(points.first().x, points.first().y)
                    for (i in 1 until points.size) {
                        val prev = points[i - 1]
                        val curr = points[i]
                        val midX = (prev.x + curr.x) / 2
                        cubicTo(
                            midX, prev.y,
                            midX, curr.y,
                            curr.x, curr.y
                        )
                    }
                }
            }

            val gradient = Brush.horizontalGradient(
                listOf(Color(0xFF64B5F6), Color(0xFFBA68C8))
            )

            drawPath(
                path = path,
                brush = gradient,
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 5f)
            )

            points.forEachIndexed { index, offset ->
                val isNow = index == 0
                drawCircle(
                    color = if (isNow) Color(0xFF4DB6AC) else Color(0xFF90CAF9),
                    radius = if (isNow) 9f else 6f,
                    center = offset
                )
            }
        }
    }
}
