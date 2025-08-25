package com.example.wearther.ui.screens.closet.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Divider
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

    Column(modifier = Modifier.background(Color.White)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            categories.forEach { category ->
                Column(
                    modifier = Modifier.clickable {
                        if (activeCategory != category) {
                            onCategoryChange(category)
                        }
                    }
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

        Divider(color = Color.Gray.copy(alpha = 0.2f), thickness = 1.dp)

        if (subCategories.isNotEmpty()) {
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                items(subCategories) { sub ->
                    Column(
                        modifier = Modifier.clickable {
                            if (activeSubCategory != sub) {
                                onSubCategoryChange(sub)
                            }
                        }
                    ) {
                        Text(
                            text = sub,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Normal,
                            color = if (activeSubCategory == sub) Color.Black else Color.Gray
                        )
                        if (activeSubCategory == sub) {
                            Spacer(modifier = Modifier.height(3.dp))
                            Box(
                                modifier = Modifier
                                    .height(1.5.dp)
                                    .width(36.dp)
                                    .background(Color.Black)
                            )
                        }
                    }
                }
            }

            Divider(color = Color.Gray.copy(alpha = 0.2f), thickness = 1.dp)
        }
    }
}
