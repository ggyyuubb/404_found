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
        "상의" to listOf("전체", "민소매", "반소매", "긴소매", "후드", "셔츠", "스웨터", ),
        "하의" to listOf("전체", "면바지", "데님", "트레이닝팬츠", "슬랙스", "반바지", "스커트"),
        "아우터" to listOf("전체", "블레이저", "가디건", "코트", "롱패딩", "숏패딩", "후드집업", "플리스", "점퍼"),
        "원피스" to listOf("전체", "원피스")
    )

    val subCategories = subCategoryMap[activeCategory] ?: emptyList()
    val activeColor = Color(0xFF1565C0)

    Column(modifier = Modifier.background(Color.White)) {
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

object CategoryMapper {
    private val categoryMap = mapOf(
        "sleeveless" to "민소매",
        "shortsleeve" to "반소매",
        "longsleeve" to "긴소매",
        "hood" to "후드",
        "shirt" to "셔츠",
        "sweater" to "스웨터",
        "jumper" to "점퍼",
        "cotton pants" to "면바지",
        "denim" to "데님",
        "trainingpants" to "트레이닝팬츠",
        "slacks" to "슬랙스",
        "shorts" to "반바지",
        "skirt" to "스커트",
        "blazer" to "블레이저",
        "cardigan" to "가디건",
        "coat" to "코트",
        "longpedding" to "롱패딩",
        "shortpedding" to "숏패딩",
        "hoodzip" to "후드집업",
        "fleece" to "플리스",
        "dress" to "원피스"
    )

    private val colorMap = mapOf(
        "beige" to "베이지",
        "black" to "블랙",
        "blue" to "블루",
        "brown" to "브라운",
        "gray" to "그레이",
        "green" to "그린",
        "orange" to "오렌지",
        "pink" to "핑크",
        "purple" to "퍼플",
        "red" to "레드",
        "white" to "화이트",
        "yellow" to "옐로우"
    )

    fun toKorean(english: String?): String {
        return categoryMap[english?.lowercase()] ?: english ?: "미분류"
    }

    fun toEnglish(korean: String?): String? {
        return categoryMap.entries.find { it.value == korean }?.key
    }

    fun colorToKorean(englishColor: String?): String {
        return colorMap[englishColor?.lowercase()] ?: englishColor ?: "정보 없음"
    }

    fun colorsToKorean(englishColors: List<String>?): String {
        if (englishColors.isNullOrEmpty()) return "정보 없음"
        return englishColors.joinToString(", ") { colorToKorean(it) }
    }
}