package com.example.wearther.home.weather

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.wearther.home.weather.HourlyWeather
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

    Column {


        // ⬇️ 시간별 예보 + 강수량 UI
        LazyRow(
            state = listState,
            horizontalArrangement = Arrangement.spacedBy(itemSpacingDp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp)
        ) {
            itemsIndexed(hourlyData) { index, forecast ->
                val forecastTime = Instant.ofEpochSecond(forecast.dt)
                    .atZone(ZoneId.of("Asia/Seoul"))
                val hourText = "${forecastTime.hour}시"
                val weatherMain = forecast.weather.firstOrNull()?.main ?: "Clouds"
                val weatherEmoji = weatherToEmoji(weatherMain)

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
                    Text(weatherEmoji, style = MaterialTheme.typography.headlineMedium)
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = String.format("%.1f°C", forecast.temp),
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = sheetTextColor
                    )

                    Spacer(Modifier.height(8.dp))
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "💧${(forecast.pop * 100).toInt()}%",
                            style = MaterialTheme.typography.bodySmall,
                            color = sheetTextColor
                        )
                        if ((forecast.pop * 100).toInt() >= 30 && (forecast.rain?.oneHour
                                ?: 0.0) > 0.0
                        ) {
                            Text(
                                text = "🌧️${
                                    String.format(
                                        "%.1f",
                                        forecast.rain?.oneHour ?: 0.0
                                    )
                                }mm",
                                style = MaterialTheme.typography.bodySmall,
                                color = sheetTextColor
                            )
                        }
                    }
                }
            }
        }
        // ⬇️ 온도 그래프 선 그리기
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp) // 그래프 높이
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

                for (i in 0 until points.size - 1) {
                    drawLine(
                        color = Color.Blue.copy(alpha = 0.8f),
                        start = points[i],
                        end = points[i + 1],
                        strokeWidth = 4f
                    )
                }

                points.forEach {
                    drawCircle(
                        color = Color.Blue,
                        radius = 6f,
                        center = it
                    )
                }
            }
        }
}
