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

    // ✅ jwt 또는 위치가 바뀌면 실행됨 (처음 화면 진입 시 자동 실행됨)
    // ❌ [임시 비활성화] 실제 위치 기반 요청 (AI 모델이 '서울', '대구'만 인식하는 문제로 주석 처리)
    /*
    LaunchedEffect(jwt, locationText) {
        Log.d("DEBUG", "🚀 HomeBottomSheetContent 진입") // ✅ 디버깅 로그
        Log.d("JWT_CHECK", "🎯 JWT: $jwt") // ✅ JWT 확인 로그
        Log.d("LOCATION_CHECK", "📍 현재 위치 텍스트: $locationText") // ✅ 위치 확인 로그

        if (!jwt.isNullOrEmpty()) { // ✅ JWT가 있을 경우에만 추천 요청
            val city = locationText.split(" ").firstOrNull() ?: "Seoul" // ✅ 위치 문자열 중 도시명 추출 (예: 서울 강남구 → 서울)
            Log.d("RECOMMEND", "📦 추천 요청 도시: $city") // ✅ 요청 로그
            viewModel.fetchRecommendations(jwt, city) // ✅ 추천 데이터 요청
        } else {
            Log.e("DEBUG", "❌ JWT 없음") // ✅ 토큰 없음 오류 로그
        }
    }
    */

    // ✅ [임시 대체] 도시명을 '서울'로 고정하여 추천 요청 (AI 임시 모델 대응용)
    LaunchedEffect(jwt, locationText) {
        Log.d("DEBUG", "🚀 HomeBottomSheetContent 진입")
        Log.d("JWT_CHECK", "🎯 JWT: $jwt")
        Log.d("LOCATION_CHECK", "📍 현재 위치 텍스트: $locationText")

        if (!jwt.isNullOrEmpty()) {
            val city = locationText.split(" ").firstOrNull()?.takeIf { it.isNotBlank() } ?: "Seoul"
            Log.d("RECOMMEND", "📦 추천 요청 도시: $city")
            viewModel.fetchRecommendations(jwt, city)
        } else {
            Log.e("DEBUG", "❌ JWT 없음")
        }
    }

    val response by viewModel.response.collectAsState()
    var isLoading by remember { mutableStateOf(false) }

    // ✅ 로딩 중이면 로딩 UI
    if (isLoading) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CircularProgressIndicator()
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "추천 데이터를 불러오는 중입니다...", color = Color.Gray)
        }
        return
    }

    // response가 null인 경우 처리 (중복 제거)
    if (response == null) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "추천 데이터를 준비 중입니다.", color = Color.Gray)
        }
        return
    }

    // 안전하게 추천 데이터 가져오기
    val top = try {
        response?.getTop()
    } catch (e: Exception) {
        Log.e("HomeBottomSheet", "getTop() failed: ${e.message}", e)
        null
    }

    val bottom = try {
        response?.getBottom()
    } catch (e: Exception) {
        Log.e("HomeBottomSheet", "getBottom() failed: ${e.message}", e)
        null
    }

    val outer = try {
        response?.getOuter()
    } catch (e: Exception) {
        Log.e("HomeBottomSheet", "getOuter() failed: ${e.message}", e)
        null
    }

    val items = listOfNotNull(outer, top, bottom)
        .filter { !it.url.isNullOrBlank() }

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
            val advice = getWeatherAdvice(weatherCode, temp)

            Spacer(modifier = Modifier.height(8.dp))

            listOf(advice.weatherComment, advice.tempComment)
                .filter { it.isNotBlank() }
                .forEach { comment ->
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
            // 추천 데이터가 없을 때 더 친화적인 메시지
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
                        text = "잠시 후 다시 확인해주세요",
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

        // 에러 메시지 표시
        if (!errorMessage.isNullOrBlank()) {
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