package com.example.wearther.home.recommendation

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

@Composable
fun RecommendationLoadingComponent(
    locationText: String? = null
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 로딩 인디케이터
        CircularProgressIndicator(
            modifier = Modifier.size(40.dp),
            color = MaterialTheme.colorScheme.primary,
            strokeWidth = 4.dp
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 로딩 텍스트 (점 애니메이션)
        var dotCount by remember { mutableIntStateOf(0) }
        LaunchedEffect(Unit) {
            while (true) {
                delay(500)
                dotCount = (dotCount + 1) % 4
            }
        }

        Text(
            text = if (locationText.isNullOrBlank())
                "위치 정보를 확인하는 중${".".repeat(dotCount)}"
            else
                "추천 데이터를 준비 중${".".repeat(dotCount)}",
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            style = MaterialTheme.typography.bodyMedium
        )
    }
}