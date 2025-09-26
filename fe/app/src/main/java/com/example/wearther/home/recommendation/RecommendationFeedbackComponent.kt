package com.example.wearther.home.recommendation

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ThumbDown
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

@Composable
fun RecommendationFeedbackComponent(
    jwt: String? = null,
    locationText: String? = null,
    viewModel: RecommendationViewModel? = null,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() }
) {
    var feedbackType by remember { mutableStateOf<String?>(null) }
    var feedbackText by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 안내 문구
        Text("코디 추천에 만족하시나요?", style = MaterialTheme.typography.bodyMedium)
        Spacer(modifier = Modifier.height(12.dp))

        // 👍 👎 버튼
        Row(horizontalArrangement = Arrangement.Center, modifier = Modifier.fillMaxWidth()) {
            IconButton(onClick = { feedbackType = "up"; feedbackText = "" }) {
                Icon(
                    imageVector = Icons.Default.ThumbUp,
                    contentDescription = "좋아요",
                    tint = if (feedbackType == "up") MaterialTheme.colorScheme.primary else Color.Gray
                )
            }
            Spacer(modifier = Modifier.width(32.dp))
            IconButton(onClick = { feedbackType = "down"; feedbackText = "" }) {
                Icon(
                    imageVector = Icons.Default.ThumbDown,
                    contentDescription = "싫어요",
                    tint = if (feedbackType == "down") MaterialTheme.colorScheme.error else Color.Gray
                )
            }
        }

        // 코멘트 입력창
        if (feedbackType != null) {
            Spacer(modifier = Modifier.height(12.dp))
            val label =
                if (feedbackType == "up") "무슨 점이 맘에 드셨나요?" else "무슨 점이 맘에 들지 않았나요?"

            Text(label, style = MaterialTheme.typography.bodyMedium)
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = feedbackText,
                onValueChange = { if (it.length <= 20) feedbackText = it },
                placeholder = { Text("최대 20자 입력") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    // ✅ 피드백 로그
                    Log.d("FEEDBACK", "👍👎: $feedbackType / 내용: $feedbackText")

                    // ✅ 피드백 전송 완료 메시지
                    scope.launch {
                        snackbarHostState.showSnackbar("피드백을 전송했습니다!")
                    }

                    // ✅ 추천 다시 불러오기 (clearRecommendations 호출 ❌)
                    if (!jwt.isNullOrEmpty() && !locationText.isNullOrBlank()) {
                        viewModel?.fetchRecommendations(jwt, locationText)
                    }

                    // 초기화
                    feedbackType = null
                    feedbackText = ""
                },
                enabled = feedbackText.isNotBlank()
            ) {
                Text("보내기")
            }

        }
    }
}
