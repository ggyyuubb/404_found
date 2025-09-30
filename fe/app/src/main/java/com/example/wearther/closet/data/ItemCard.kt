package com.example.wearther.closet.data

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter

@Composable
fun ItemCard(
    imageUrl: String,
    category: String,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(0.75f), // 조금 더 세로로 길게
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Box {
            // 옷 이미지
            Image(
                painter = rememberAsyncImagePainter(imageUrl),
                contentDescription = "옷 이미지",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            // 카테고리 태그 (하단 전체 바)
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .background(Color.Black.copy(alpha = 0.5f))
                    .padding(vertical = 6.dp)
            ) {
                androidx.compose.material3.Text(
                    text = category,
                    color = Color.White,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            // 삭제 버튼
            // 삭제 버튼
            IconButton(
                onClick = { onDelete() },
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
                    .background(Color.White.copy(alpha = 0.9f), shape = CircleShape)
                    .size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "삭제",
                    tint = Color(0xFF555555), // ✅ 세련된 다크그레이
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}
