package com.example.wearther.closet.data

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage

@Composable
fun ClosetListItem(
    imageUrl: String,
    bigCategory: String?,
    subCategory: String?,
    colorNames: List<String>? = null,
    material: String? = null,
    uploaded_at: String? = null,
    onClick: () -> Unit
) {
    // ⭐️ 로그: 받은 값 확인
    android.util.Log.d("ClosetListItem", "받은 uploaded_at: '$uploaded_at'")
    android.util.Log.d("ClosetListItem", "isNullOrBlank: ${uploaded_at.isNullOrBlank()}")

    // ⭐️ 날짜 포맷 변환: "2025-10-26 14:30:00" → "25/10/26"
    val displayDate = remember(uploaded_at) {
        android.util.Log.d("ClosetListItem", "remember 블록 실행: '$uploaded_at'")

        if (uploaded_at.isNullOrBlank()) {
            android.util.Log.d("ClosetListItem", "→ 비어있음, '정보 없음' 반환")
            "정보 없음"
        } else {
            try {
                // "2025-10-26 14:30:00" 또는 "2025-10-26T14:30:00Z" 형식 처리
                val datePart = uploaded_at
                    .split(" ")[0]      // 공백으로 분리 → "2025-10-26"
                    .split("T")[0]      // T로 분리 (ISO 8601 대비)

                android.util.Log.d("ClosetListItem", "datePart: '$datePart'")

                val parts = datePart.split("-")
                android.util.Log.d("ClosetListItem", "parts: $parts")

                if (parts.size == 3) {
                    // "2025" → "25", "10" → "10", "26" → "26"
                    val result = "${parts[0].takeLast(2)}/${parts[1]}/${parts[2]}"
                    android.util.Log.d("ClosetListItem", "→ 최종 결과: '$result'")
                    result
                } else {
                    android.util.Log.d("ClosetListItem", "→ parts.size != 3, '정보 없음' 반환")
                    "정보 없음"
                }
            } catch (e: Exception) {
                android.util.Log.e("ClosetListItem", "파싱 오류: ${e.message}")
                "정보 없음"
            }
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = imageUrl,
            contentDescription = "옷 이미지",
            modifier = Modifier
                .width(72.dp)
                .aspectRatio(0.75f)  // ⭐️ 0.65 → 0.75로 변경
                .clip(RectangleShape),
            contentScale = ContentScale.Crop
        )

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {

            val categoryText = when {
                !bigCategory.isNullOrBlank() && !subCategory.isNullOrBlank() ->
                    "$bigCategory · $subCategory"
                !bigCategory.isNullOrBlank() -> bigCategory
                !subCategory.isNullOrBlank() -> subCategory
                else -> "미분류"
            }

            Text(
                text = categoryText,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurface
            )

            if (!material.isNullOrBlank()) {
                Text(
                    text = "소재: $material",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            val names = colorNames.orEmpty().filter { it.isNotBlank() }
            if (names.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    names.take(3).forEach { name ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(12.dp)
                                    .border(1.dp, Color(0x44000000), RoundedCornerShape(2.dp))
                                    .background(nameToColor(name), RoundedCornerShape(2.dp))
                            )
                            Text(
                                text = name,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // ⭐️ 날짜 표시 (25/10/26 형식)
            Spacer(Modifier.height(4.dp))
            Text(
                text = "업로드: $displayDate",
                fontSize = 12.sp,
                color = if (displayDate == "정보 없음") {
                    Color.Gray.copy(alpha = 0.8f)
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                }
            )
        }
    }
}