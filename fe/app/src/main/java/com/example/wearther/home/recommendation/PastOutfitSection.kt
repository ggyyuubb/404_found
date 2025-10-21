package com.example.wearther.home.recommendation

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.* // weight 포함
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.wearther.R

@Composable
fun PastOutfitSection(
    textColor: Color,                                        // ✅ 누락돼 있던 textColor 파라미터 복구
    containerColor: Color = MaterialTheme.colorScheme.surface
) {
    // ✅ 배경을 서브 바텀시트와 완전히 동일하게 맞춤
    Surface(
        color = containerColor,
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // 타이틀 (이모지 + 텍스트)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("🕘", fontSize = 22.sp)
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "과거 나의 코디",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = textColor
                )
            }

            Spacer(Modifier.height(12.dp))

            // 3장 고정: 긴소매 / 자켓 / 면바지
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.Top
            ) {
                PastCard(
                    modifier = Modifier.weight(1f),
                    imageRes = R.drawable.longsleeve,
                    label = "긴소매"
                )
                PastCard(
                    modifier = Modifier.weight(1f),
                    imageRes = R.drawable.jacket,
                    label = "자켓"
                )
                PastCard(
                    modifier = Modifier.weight(1f),
                    imageRes = R.drawable.cottonpants,
                    label = "면바지"
                )
            }
        }
    }
}

@Composable
private fun PastCard(
    modifier: Modifier = Modifier,
    imageRes: Int,
    label: String
) {
    Card(
        modifier = modifier.height(190.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF9F9F9))
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // 이미지
            Image(
                painter = painterResource(id = imageRes),
                contentDescription = label,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clip(RoundedCornerShape(topStart = 14.dp, topEnd = 14.dp)),
                contentScale = ContentScale.Crop
            )

            // 라벨 바 (그라데이션 + 볼드 텍스트)
            Surface(
                color = Color.Transparent,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            brush = Brush.horizontalGradient(
                                listOf(Color(0xFF2B2B2B), Color(0xFF4A4A4A))
                            ),
                            shape = RoundedCornerShape(
                                bottomStart = 14.dp,
                                bottomEnd = 14.dp
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = label,
                        color = Color.White,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            letterSpacing = 0.1.sp
                        )
                    )
                }
            }
        }
    }
}
