package com.example.wearther.home.recommendation

import android.content.Context
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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

@OptIn(ExperimentalMaterial3Api::class)
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

    // ▼▼ 추가: 서브 바텀시트 열림 상태 & 시트 상태 ▼▼
    var showPastSheet by remember { mutableStateOf(false) }
    val pastSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    // ▲▲ 추가 끝 ▲▲

    LaunchedEffect(response) {
        if (response != null) isLoading = false
    }

    if (isLoading) {
        RecommendationLoadingComponent(locationText = locationText)
        return
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xFFEEEEEE),
                        Color(0xFFFFFFFF),
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

        // 날씨 코멘트 (기존 그대로)
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
                    Box(modifier = Modifier.wrapContentWidth()) {
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

        // ======= 추천 카드 영역 (기존 그대로) =======
        if (response == null) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
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
        }

        val items =
            if (response != null)
                listOfNotNull(response?.getOuter(), response?.getTop(), response?.getBottom())
                    .filter { !it.url.isNullOrBlank() }
            else emptyList()

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
        } else {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StaticFallbackCard(imageRes = R.drawable.hoodie, label = "후드집업")
                StaticFallbackCard(imageRes = R.drawable.denim,  label = "데님")
            }
        }
        // ======= /추천 카드 영역 =======

        // ▼▼ 과거 코디 버튼 (그라데이션 + 둥근 모서리 + 이모지/느낌표) ▼▼
        Spacer(modifier = Modifier.height(16.dp))

        val pastBtnBrush = Brush.horizontalGradient(
            listOf(Color(0xFF6A8DFF), Color(0xFF8E67FF))
        )

        Button(
            onClick = { showPastSheet = true },
            shape = RoundedCornerShape(14.dp),
            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Transparent,
                contentColor = Color.White
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .background(pastBtnBrush, RoundedCornerShape(14.dp))
                .border(1.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(14.dp))
        ) {
            Text(
                text = "👉  과거에 추천받은 코디가 있네, 확인하기!",
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp
                )
            )
        }
        // ▲▲ 버튼 끝 ▲▲

        Spacer(modifier = Modifier.height(24.dp))

        // 피드백 컴포넌트 (기존)
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

    // ▼▼ 서브 바텀시트(작게) — PastOutfitSection 표시 ▼▼
    if (showPastSheet) {
        // 통일할 배경색
        val pastContainerColor = MaterialTheme.colorScheme.surface

        ModalBottomSheet(
            onDismissRequest = { showPastSheet = false },
            sheetState = pastSheetState,
            dragHandle = null, // 기본 핸들 제거 (중복 방지)
            shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
            containerColor = pastContainerColor,  // 시트 배경색 지정
            tonalElevation = 2.dp
        ) {
            // 커스텀 드래그핸들(한 개만)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp, bottom = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .width(40.dp)
                        .height(4.dp)
                        .background(
                            color = Color.LightGray.copy(alpha = 0.8f),
                            shape = RoundedCornerShape(50)
                        )
                )
            }

            // 내용 (배경색을 시트와 동일하게)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 420.dp)
                    .background(pastContainerColor)
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                PastOutfitSection(
                    textColor = textColor,
                    containerColor = pastContainerColor  // 동일 컬러 전달
                )
                Spacer(Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = { showPastSheet = false }) {
                        Text("닫기")
                    }
                }
            }
        }
    }
    // ▲▲ 서브 바텀시트 끝 ▲▲
}

@Composable
private fun StaticFallbackCard(
    imageRes: Int,
    label: String
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(6.dp),
        modifier = Modifier.size(width = 160.dp, height = 200.dp)
    ) {
        Box {
            Image(
                painter = painterResource(id = imageRes),
                contentDescription = label,
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
                    text = label,
                    color = Color.Black,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(8.dp),
                    fontSize = 14.sp
                )
            }
        }
    }
}
