package com.example.wearther.home.recommendation

import android.content.Context
import android.util.Log
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.wearther.R
import com.example.wearther.home.weather.WeatherViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch

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
    val coroutineScope = rememberCoroutineScope()

    // 응답 들어오면 로딩 종료
    LaunchedEffect(response) {
        if (response != null) isLoading = false
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

    // 추천 데이터
    val items = listOfNotNull(
        response?.getOuter(),
        response?.getTop(),
        response?.getBottom()
    ).filter { !it.url.isNullOrBlank() }

    // 🔹 손잡이 색 가져오기
    val handleColor = Color(0xFFEEEEEE)
// 🔹 배경 (손잡이 색 → 흰색 그라데이션)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xFFEEEEEE),
                        Color(0xFFFFFFFF), // 화이트
                        Color(0xFFEEEEEE)
                    )
                )
            )
            .padding(16.dp)
    ) {
        Spacer(modifier = Modifier.height(12.dp))


        // 오늘의 날씨 제목
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                painter = painterResource(id = R.drawable.today_weather),
                contentDescription = "오늘의 날씨",
                tint = Color.Unspecified,
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "오늘의 날씨는...",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = textColor
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 날씨 코멘트
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
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    verticalAlignment = Alignment.Bottom
                ) {
                    Box(
                        modifier = Modifier.wrapContentWidth()
                    ) {
                        Surface(
                            color = Color.White,
                            shape = RoundedCornerShape(16.dp),
                            shadowElevation = 4.dp,
                            modifier = Modifier.padding(start = 12.dp)
                        ) {
                            Text(
                                text = comment,
                                color = Color.Black,
                                fontSize = 14.sp,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                            )
                        }

                        // 꼬리
                        Canvas(
                            modifier = Modifier
                                .size(13.dp)
                                .align(Alignment.BottomStart)
                                .offset(x = 2.dp, y = (-6).dp)
                        ) {
                            val path = Path().apply {
                                moveTo(size.width, 0f)
                                lineTo(0f, size.height / 2)
                                lineTo(size.width, size.height)
                                close()
                            }
                            drawPath(path, Color.White)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 추천 코디 제목
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                painter = painterResource(id = R.drawable.clothes_rack),
                contentDescription = "추천 코디",
                tint = Color.Unspecified,
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "오늘의 추천 코디는...",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = textColor
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 추천 카드
        if (items.isNotEmpty()) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                items.forEach { item ->
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(6.dp),
                        modifier = Modifier.size(width = 160.dp, height = 200.dp)
                    ) {
                        Box {
                            AsyncImage(
                                model = item.url,
                                contentDescription = item.category,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                            Surface(
                                color = Color.White.copy(alpha = 0.85f),
                                modifier = Modifier
                                    .align(Alignment.BottomCenter)
                                    .fillMaxWidth()
                            ) {
                                Text(
                                    text = item.category ?: "의류",
                                    color = Color.Black,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(8.dp),
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 🔹 개선된 피드백 컴포넌트 호출
        RecommendationFeedbackComponent(
            jwt = jwt,
            locationText = locationText,
            viewModel = viewModel
        )

        if (!errorMessage.isNullOrBlank() && response == null) {
            Spacer(modifier = Modifier.height(16.dp))
            Surface(
                color = Color(0xFFFFEBEE),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = errorMessage.orEmpty(),
                    color = Color(0xFFD32F2F),
                    fontSize = 14.sp,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
    }
}
