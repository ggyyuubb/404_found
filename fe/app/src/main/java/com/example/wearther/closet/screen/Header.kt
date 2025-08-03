package com.example.wearther.ui.screens.closet.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun Header(
    totalItems: Int,
    onSortChange: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "${totalItems.toString().replace(Regex("(\\d)(?=(\\d{3})+(?!\\d))"), "$1,")}개",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.clickable { onSortChange() },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "직접추천순",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = "정렬 변경",
                    tint = Color.Gray,
                    modifier = Modifier.size(16.dp)
                )
            }

            IconButton(
                onClick = { /* 그리드 뷰 변경 */ },
                modifier = Modifier
                    .size(32.dp)
                    .background(
                        Color.Gray.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(4.dp)
                    )
            ) {
                Icon(
                    imageVector = Icons.Default.GridView,
                    contentDescription = "그리드 뷰",
                    tint = Color.Gray,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}
