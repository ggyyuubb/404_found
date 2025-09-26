package com.example.wearther.home.recommendation

import android.content.Context
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.wearther.home.weather.WeatherViewModel
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun HomeBottomSheetContent(
    textColor: Color,
    viewModel: RecommendationViewModel,
    weatherViewModel: WeatherViewModel = viewModel()
) {
    val context = LocalContext.current
    val sharedPreferences = context.getSharedPreferences("myPrefs", Context.MODE_PRIVATE)
    val jwt = sharedPreferences.getString("jwt", null)

    val errorMessage by viewModel.errorMessage.collectAsState()
    val temp by viewModel.temp.collectAsState()
    val weatherCode by viewModel.weatherCode.collectAsState()
    val locationText by weatherViewModel.locationText.collectAsState()
    val response by viewModel.response.collectAsState()

    var isLoading by remember { mutableStateOf(false) }

    // 위치 변경 감지 → 추천 요청
    LaunchedEffect(locationText) {
        Log.d("RECOMMEND_REFRESH", "위치 변경 감지: $locationText")

        if (!locationText.isNullOrBlank()
            && locationText != "위치를 가져오는 중..."
            && locationText != "현재 위치"
        ) {
            if (!jwt.isNullOrEmpty()) {
                viewModel.clearRecommendations()
                isLoading = true
                try {
                    viewModel.fetchRecommendations(jwt, locationText)
                } catch (e: Exception) {
                    Log.e("RECOMMEND_ERROR", "추천 요청 실패: ${e.message}", e)
                }
            } else {
                Log.w("RECOMMEND_SKIP", "JWT 없음")
            }
        } else {
            Log.w("RECOMMEND_SKIP", "유효하지 않은 위치 정보: '$locationText'")
        }
    }

    // 응답 들어오면 로딩 종료
    LaunchedEffect(response) {
        if (response != null) {
            isLoading = false
        }
    }

    // 불러오는 중
    if (isLoading) {
        RecommendationLoadingComponent(locationText = locationText)
        return
    }

    // 응답 없음
    if (response == null) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = if (locationText.isNullOrBlank())
                    "위치 정보를 확인하는 중입니다."
                else
                    "추천 결과가 도착하지 않았습니다.",
                color = Color.Gray
            )
        }
        return
    }

    // 추천 데이터 안전하게 가져오기
    val top = runCatching { response?.getTop() }.getOrNull()
    val bottom = runCatching { response?.getBottom() }.getOrNull()
    val outer = runCatching { response?.getOuter() }.getOrNull()

    val items = listOfNotNull(outer, top, bottom).filter { !it.url.isNullOrBlank() }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "오늘의 날씨는...",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = textColor
        )

        Spacer(modifier = Modifier.height(12.dp))

        if (temp != null && weatherCode != null) {
            val weatherResponse by weatherViewModel.weatherData.collectAsState()
            val advice = weatherResponse?.let {
                generateWeatherAdvice(
                    current = it.current,
                    hourly = it.hourly,
                    daily = it.daily,
                    alerts = it.alerts
                )
            }

            advice?.comments?.filter { it.isNotBlank() }?.forEach { comment ->
                Surface(
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(12.dp),
                    tonalElevation = 2.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    Text(
                        text = comment,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                        fontSize = 14.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
        }

        Text(
            text = "오늘의 추천 코디는...",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = textColor
        )

        Spacer(modifier = Modifier.height(12.dp))

        if (items.isEmpty()) {
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "추천 코디를 준비 중입니다",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 16.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (locationText.isNullOrBlank()) "위치 정보 확인 후 다시 시도해주세요"
                        else "잠시 후 다시 확인해주세요",
                        color = Color.Gray,
                        fontSize = 14.sp
                    )
                }
            }
        } else {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items.forEach { item ->
                    Box(
                        modifier = Modifier
                            .size(width = 160.dp, height = 200.dp)
                            .clip(RoundedCornerShape(12.dp))
                    ) {
                        AsyncImage(
                            model = item.url ?: "",
                            contentDescription = item.category ?: "의류",
                            modifier = Modifier.fillMaxSize()
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .align(Alignment.BottomCenter)
                                .background(Color.Black.copy(alpha = 0.5f))
                                .padding(6.dp)
                        ) {
                            Text(
                                text = item.category ?: "의류",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.align(Alignment.Center)
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
        RecommendationFeedbackComponent(
            jwt = jwt,
            locationText = locationText,
            viewModel = viewModel
        )

        if (!errorMessage.isNullOrBlank() && response == null) {
            Spacer(modifier = Modifier.height(16.dp))
            Surface(
                color = MaterialTheme.colorScheme.errorContainer,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = errorMessage.orEmpty(),
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(12.dp)
                )
            }
        }
    }
}