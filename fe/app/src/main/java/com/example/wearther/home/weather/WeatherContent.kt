// WeatherContent.kt - 간소화된 버전 (위치 선택 기능 포함)
package com.example.wearther.home.weather

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp

@Composable
fun WeatherContent(
    data: WeatherResponse,
    locationText: String,
    sheetTextColor: Color,
    itemWidthDp: Dp = 70.dp,
    itemSpacingDp: Dp = 8.dp,
    savedLocations: List<SavedLocation> = listOf(
        SavedLocation("current", "현재 위치", 0.0, 0.0, true)
    ),
    onLocationSelect: (SavedLocation) -> Unit = {},
    onAddLocation: (SavedLocation) -> Unit = {},
    onDeleteLocation: (SavedLocation) -> Unit = {}, // 👈 삭제 파라미터 추가
    onSearchLocation: suspend (String) -> List<SavedLocation> = { emptyList() }
) {
    val now = Instant.now().atZone(ZoneId.of("Asia/Seoul")).toLocalDateTime()
    //val nowText = now.format(DateTimeFormatter.ofPattern("현재 시각: HH시 mm분"))
    val todayTemps = data.daily?.firstOrNull()

    val endTime = now.plusHours(24)
    val filteredHourly = data.hourly.filter {
        val forecastTime = Instant.ofEpochSecond(it.dt)
            .atZone(ZoneId.of("Asia/Seoul"))
            .toLocalDateTime()
        forecastTime.isAfter(now) && !forecastTime.isAfter(endTime)
    }

    // 위치 선택 모달 상태
    var showLocationModal by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 16.dp)
    ) {
        item {
            // 📍 이모지와 위치 텍스트 (클릭 가능)
            Row(
                modifier = Modifier
                    .wrapContentWidth()
                    .clickable { showLocationModal = true }
                    .padding(horizontal = 16.dp)
                    .background(
                        color = Color.White.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(12.dp)
                    )
                    .padding(horizontal = 16.dp, vertical = 6.dp),

                // 2. verticalAlignment는 Row의 명시적인 인자로
                verticalAlignment = Alignment.CenterVertically // ⬅️ 이 위치에 있어야 합니다.
            ) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = "위치 변경",
                    tint = sheetTextColor,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = locationText.ifBlank { "위치 정보를 불러오는 중..." },
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = sheetTextColor
                    )
                )
            }
        }

        item {
            /*Text(
                text = nowText,
                style = MaterialTheme.typography.bodyLarge.copy(color = sheetTextColor),
                modifier = Modifier.padding(horizontal = 16.dp)
            )*/
        }

        item { Spacer(modifier = Modifier.height(16.dp)) }

        item {
            CurrentWeatherCard(data = data,
                textColor = sheetTextColor)
        }

        if (filteredHourly.isNotEmpty()) {
            item {
                HourlyForecastGraphRow(
                    hourlyData = filteredHourly,
                    sheetTextColor = sheetTextColor,
                    itemWidthDp = itemWidthDp,
                    itemSpacingDp = itemSpacingDp
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
            WeeklyForecast(
                dailyWeather = data.daily,
                textColor = sheetTextColor
            )
        }
    }

    // 위치 선택 모달
    if (showLocationModal) {
        LocationSelectionModal(
            savedLocations = savedLocations,
            onLocationSelect = { location ->
                showLocationModal = false
                onLocationSelect(location)
            },
            onAddLocation = onAddLocation,
            onDeleteLocation = onDeleteLocation, // 👈 삭제 파라미터 전달
            onDismiss = { showLocationModal = false },
            onSearchLocation = onSearchLocation
        )
    }
}