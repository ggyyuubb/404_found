package com.example.wearther.closet.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter

@Composable
fun ItemCard(
    imageUrl: String,
    bigCategory: String?,
    subCategory: String?,
    colorNames: List<String>? = null,   // ✅ 색상 이름 리스트 (예: ["black","navy"])
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    val onSurface = MaterialTheme.colorScheme.onSurface

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(0.65f)
            .clickable { onClick() },
        shape = RectangleShape,
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Column(Modifier.fillMaxSize()) {
            // 이미지
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                Image(
                    painter = rememberAsyncImagePainter(imageUrl),
                    contentDescription = "옷 이미지",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                IconButton(
                    onClick = { onDelete() },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .background(Color.White.copy(alpha = 0.9f), shape = CircleShape)
                        .size(30.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "삭제",
                        tint = Color(0xFF555555)
                    )
                }
            }

            // 분류 텍스트
            val categoryText =
                when {
                    !bigCategory.isNullOrBlank() && !subCategory.isNullOrBlank() -> "$bigCategory - $subCategory"
                    !bigCategory.isNullOrBlank() -> bigCategory
                    !subCategory.isNullOrBlank() -> subCategory
                    else -> "X"
                }

            Text(
                text = categoryText,
                color = onSurface,
                fontWeight = FontWeight.Medium,
                fontSize = 13.sp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            // ===== 색상 스와치 =====
            val names = colorNames.orEmpty().filter { it.isNotBlank() }
            if (names.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.Start
                ) {
                    // 최대 4개만 보여주고 나머지는 +N
                    val toShow = names.take(4)
                    toShow.forEach { name ->
                        val c = nameToColor(name)
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .border(1.dp, Color(0x22000000)) // 미묘한 테두리
                                .background(c)
                        )
                        Spacer(Modifier.width(6.dp))
                    }
                    val remain = names.size - toShow.size
                    if (remain > 0) {
                        Text(
                            text = "+$remain",
                            color = Color.Gray,
                            fontSize = 12.sp
                        )
                    }
                }
            } else {
                Text(
                    text = "X",
                    color = Color.Gray,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
    }
}

/** 영문 색상명 → 실제 Color */
private fun nameToColor(name: String): Color = when (name.trim().lowercase()) {
    "beige" -> Color(0xFFD9C3A3)
    "black" -> Color(0xFF000000)
    "blue" -> Color(0xFF3667C8)
    "brown" -> Color(0xFF7A4E2B)
    "gray", "grey" -> Color(0xFF808080)
    "green" -> Color(0xFF2E7D32)
    "orange" -> Color(0xFFF57C00)
    "pink" -> Color(0xFFE91E63)
    "purple" -> Color(0xFF7E57C2)
    "red" -> Color(0xFFD32F2F)
    "white" -> Color(0xFFFFFFFF)
    "yellow" -> Color(0xFFFFEB3B)
    "navy" -> Color(0xFF0F2D52)
    "khaki" -> Color(0xFF6B7B59)
    "ivory" -> Color(0xFFFFF8E1)
    else -> Color(0xFFE0E0E0) // 알 수 없는 값 → 연회색
}
