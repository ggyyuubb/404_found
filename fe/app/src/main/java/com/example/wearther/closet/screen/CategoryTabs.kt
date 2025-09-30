package com.example.wearther.ui.screens.closet.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Divider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
    onCategoryChange: (String) -> Unit,
    activeSubCategory: String,
    onSubCategoryChange: (String) -> Unit
) {
    val subCategoryMap = mapOf(
        "상의" to listOf("전체", "민소매", "반소매", "긴소매", "후드", "셔츠", "스웨터"),
        "하의" to listOf("전체", "데님", "트레이닝", "슬랙스", "숏팬츠", "스커트"),
        "아우터" to listOf("전체", "후드집업", "자켓", "코트", "무스탕&퍼", "플리스", "패딩"),
        "원피스" to listOf("전체", "맥시", "나시원피스", "셔츠원피스")
    )

    val subCategories = subCategoryMap[activeCategory] ?: emptyList()
    val activeColor = Color(0xFF1565C0) // 🔹 조금 더 짙은 블루

    Column(modifier = Modifier.background(Color.White)) {
        // 🔹 상위 카테고리 탭
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(categories) { category ->
                val isActive = activeCategory == category
                Surface(
                    modifier = Modifier
                        .clickable { if (!isActive) onCategoryChange(category) },
                    shape = RoundedCornerShape(6.dp),
                    color = Color.Transparent,
                    border = if (isActive) BorderStroke(2.dp, activeColor) else null
                ) {
                    Text(
                        text = category,
                        fontSize = 14.sp,
                        fontWeight = if (isActive) FontWeight.Bold else FontWeight.Medium,
                        color = if (isActive) activeColor else Color.Gray,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
                    )
                }
            }
        }

        Divider(color = Color.Gray.copy(alpha = 0.2f), thickness = 1.dp)

        // 🔹 하위 카테고리 탭 (상위와 동일 스타일)
        if (subCategories.isNotEmpty()) {
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(subCategories) { sub ->
                    val isActive = activeSubCategory == sub
                    Surface(
                        modifier = Modifier
                            .clickable { if (!isActive) onSubCategoryChange(sub) },
                        shape = RoundedCornerShape(6.dp),
                        color = Color.Transparent,
                        border = if (isActive) BorderStroke(2.dp, activeColor) else null
                    ) {
                        Text(
                            text = sub,
                            fontSize = 13.sp,
                            fontWeight = if (isActive) FontWeight.SemiBold else FontWeight.Normal,
                            color = if (isActive) activeColor else Color.Gray,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                        )
                    }
                }
            }

            Divider(color = Color.Gray.copy(alpha = 0.2f), thickness = 1.dp)
        }
    }
}
