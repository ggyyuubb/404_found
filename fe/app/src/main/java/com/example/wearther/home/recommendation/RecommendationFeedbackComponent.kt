package com.example.wearther.home.recommendation

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ThumbDown
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White.copy(alpha = 0.6f), shape = RoundedCornerShape(16.dp))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 안내 문구
        Text(
            "코디 추천에 만족하시나요?",
            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 16.sp)
        )
        Spacer(modifier = Modifier.height(12.dp))

        // 👍 👎 버튼 (파스텔톤)
        Row(horizontalArrangement = Arrangement.Center, modifier = Modifier.fillMaxWidth()) {
            IconButton(onClick = { feedbackType = "up"; feedbackText = "" }) {
                Icon(
                    imageVector = Icons.Default.ThumbUp,
                    contentDescription = "좋아요",
                    tint = if (feedbackType == "up") Color(0xFF81C784) else Color(0xFFB2DFDB), // 파스텔 그린
                    modifier = Modifier.size(32.dp)
                )
            }
            Spacer(modifier = Modifier.width(32.dp))
            IconButton(onClick = { feedbackType = "down"; feedbackText = "" }) {
                Icon(
                    imageVector = Icons.Default.ThumbDown,
                    contentDescription = "싫어요",
                    tint = if (feedbackType == "down") Color(0xFFE57373) else Color(0xFFFFCDD2), // 파스텔 레드
                    modifier = Modifier.size(32.dp)
                )
            }
        }

        // 코멘트 입력창
        if (feedbackType != null) {
            Spacer(modifier = Modifier.height(16.dp))
            val label =
                if (feedbackType == "up") "무슨 점이 맘에 드셨나요?" else "무슨 점이 맘에 들지 않았나요?"

            Text(label, style = MaterialTheme.typography.bodyMedium)
            Spacer(modifier = Modifier.height(8.dp))

            // 🔹 둥근 카드 스타일 입력창
            Surface(
                shape = RoundedCornerShape(12.dp),
                tonalElevation = 2.dp,
                shadowElevation = 2.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                TextField(
                    value = feedbackText,
                    onValueChange = { if (it.length <= 20) feedbackText = it },
                    placeholder = { Text("최대 20자 입력") },
                    singleLine = true,
                    colors = TextFieldDefaults.colors( // ✅ Material3 방식
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        disabledContainerColor = Color.White,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }


        Spacer(modifier = Modifier.height(12.dp))

            // 보내기 버튼
            Button(
                onClick = {
                    Log.d("FEEDBACK", "👍👎: $feedbackType / 내용: $feedbackText")
                    scope.launch {
                        snackbarHostState.showSnackbar("피드백을 전송했습니다!")
                    }

                    if (!jwt.isNullOrEmpty() && !locationText.isNullOrBlank()) {
                        viewModel?.fetchRecommendations(jwt, locationText)
                    }

                    feedbackType = null
                    feedbackText = ""
                },
                enabled = feedbackText.isNotBlank(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("보내기")
            }
        }
    }

