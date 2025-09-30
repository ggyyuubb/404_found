package com.example.wearther.ui.screens.closet.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.ViewList
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// 정렬 옵션 enum
enum class SortOption(val displayName: String) {
    CATEGORY("카테고리순"),
    NEWEST_FIRST("추가순 (최신순)"),
    OLDEST_FIRST("추가순 (오래된순)"),
    MANUAL("직접정렬순")
}

@Composable
fun Header(
    totalItems: Int,
    currentSortOption: SortOption,
    onSortChange: (SortOption) -> Unit,
    isGridView: Boolean,                // ✅ 추가
    onToggleView: () -> Unit            // ✅ 추가
) {
    var showDropdown by remember { mutableStateOf(false) }

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
            // 🔽 정렬 드롭다운
            Box {
                Row(
                    modifier = Modifier.clickable {
                        showDropdown = !showDropdown
                    },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = currentSortOption.displayName,
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                    Icon(
                        imageVector = if (showDropdown) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = "정렬 변경",
                        tint = Color.Gray,
                        modifier = Modifier.size(16.dp)
                    )
                }

                DropdownMenu(
                    expanded = showDropdown,
                    onDismissRequest = { showDropdown = false },
                    modifier = Modifier.background(Color.White)
                ) {
                    SortOption.values().forEach { option ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = option.displayName,
                                    fontSize = 14.sp,
                                    color = if (option == currentSortOption) Color.Black else Color.Gray,
                                    fontWeight = if (option == currentSortOption) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            onClick = {
                                onSortChange(option)
                                showDropdown = false
                            }
                        )
                    }
                }
            }

            // 🔄 뷰 전환 버튼
            IconButton(
                onClick = { onToggleView() },   // ✅ 여기서 토글
                modifier = Modifier
                    .size(32.dp)
                    .background(
                        Color.Gray.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(4.dp)
                    )
            ) {
                Icon(
                    imageVector = if (isGridView) Icons.Default.ViewList else Icons.Default.GridView, // ✅ 아이콘 토글
                    contentDescription = if (isGridView) "리스트 뷰" else "그리드 뷰",
                    tint = Color.Gray,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}
