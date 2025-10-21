package com.example.wearther.ui.screens.closet.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Divider
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
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
    if (categories.isEmpty()) return

    val subCategoryMap = mapOf(
        "상의" to listOf("전체", "민소매", "반소매", "긴소매", "후드", "셔츠", "스웨터"),
        "하의" to listOf("전체", "반바지", "면바지", "데님", "트레이닝바지", "슬랙스", "스커트"),
        "아우터" to listOf("전체", "점퍼", "블레이저", "가디건", "코트", "롱패딩", "숏패딩", "후드집업", "플리스"),
        "원피스" to listOf("전체", "원피스")
    )
    val subCategories = subCategoryMap[activeCategory] ?: emptyList()

    val activeColor = Color(0xFF0D47A1) // 어두운 파랑
    val inactiveColor = Color.Gray
    val dividerColor = Color(0x1F000000) // 연한 회색

    // =============== 상위 카테고리 ===============
    val selectedMainIndex = categories.indexOf(activeCategory).let { if (it < 0) 0 else it }

    ScrollableTabRow(
        selectedTabIndex = selectedMainIndex,
        edgePadding = 2.dp, // ✅ 좌우 여백 줄임
        containerColor = Color.White,
        contentColor = activeColor,
        divider = {
            Divider(
                color = dividerColor,
                thickness = 1.dp,
                modifier = Modifier.fillMaxWidth()
            )
        },
        indicator = { tabPositions ->
            TabRowDefaults.Indicator(
                modifier = Modifier.tabIndicatorOffset(tabPositions[selectedMainIndex]),
                height = 1.dp,
                color = activeColor
            )
        }
    ) {
        categories.forEachIndexed { index, category ->
            val selected = index == selectedMainIndex
            Tab(
                selected = selected,
                onClick = { if (!selected) onCategoryChange(category) },
                selectedContentColor = activeColor,
                unselectedContentColor = inactiveColor,
                text = {
                    Text(
                        text = category,
                        fontSize = 14.sp,
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp) // ✅ 글씨 간격 줄임
                    )
                }
            )
        }
    }

    // =============== 하위 카테고리 ===============
    if (subCategories.isNotEmpty()) {
        val selectedSubIndex = subCategories.indexOf(activeSubCategory).let { if (it < 0) 0 else it }

        ScrollableTabRow(
            selectedTabIndex = selectedSubIndex,
            edgePadding = 2.dp, // ✅ 좌우 여백 줄임
            containerColor = Color.White,
            contentColor = activeColor,
            divider = {
                Divider(
                    color = dividerColor,
                    thickness = 1.dp,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            indicator = { tabPositions ->
                TabRowDefaults.Indicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedSubIndex]),
                    height = 1.dp, // 서브 탭은 얇게
                    color = activeColor
                )
            }
        ) {
            subCategories.forEachIndexed { index, sub ->
                val selected = index == selectedSubIndex
                Tab(
                    selected = selected,
                    onClick = { if (!selected) onSubCategoryChange(sub) },
                    selectedContentColor = activeColor,
                    unselectedContentColor = inactiveColor,
                    text = {
                        Text(
                            text = sub,
                            fontSize = 13.sp,
                            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp) // ✅ 더 밀착감 있게
                        )
                    }
                )
            }
        }
    }
}
