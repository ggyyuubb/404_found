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
import com.example.wearther.closet.data.nameToColor

@Composable
fun ItemCard(
    imageUrl: String,
    bigCategory: String?,
    subCategory: String?,
    colorNames: List<String>? = null,
    onClick: () -> Unit
) {
    val onSurface = MaterialTheme.colorScheme.onSurface

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RectangleShape,
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Column(Modifier.fillMaxWidth()) {
            // ⭐️ 이미지 부분을 정확한 비율로 지정
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(0.75f)  // ⭐️ 이미지 비율 명시 (3:4)
            ) {
                Image(
                    painter = rememberAsyncImagePainter(imageUrl),
                    contentDescription = "옷 이미지",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }

            val categoryText = when {
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

            val names = colorNames.orEmpty().filter { it.isNotBlank() }
            if (names.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.Start
                ) {
                    val toShow = names.take(4)
                    toShow.forEach { name ->
                        val c = nameToColor(name)
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .border(1.dp, Color(0x22000000))
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