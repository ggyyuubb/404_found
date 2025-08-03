package com.example.wearther.ui.screens.closet.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun CategoryTabs(
    categories: List<String>,
    activeCategory: String,
    onCategoryChange: (String) -> Unit
) {
    Column(modifier = Modifier.background(Color.White)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            categories.forEach { category ->
                Column(
                    modifier = Modifier.clickable { onCategoryChange(category) }
                ) {
                    Text(
                        text = category,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (activeCategory == category) Color.Black else Color.Gray
                    )
                    if (activeCategory == category) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Box(
                            modifier = Modifier
                                .height(2.dp)
                                .width(40.dp)
                                .background(Color.Black)
                        )
                    }
                }
            }
        }
        Divider(
            color = Color.Gray.copy(alpha = 0.2f),
            thickness = 1.dp
        )
    }
}
