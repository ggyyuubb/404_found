package com.example.wearther.closet.screen

import androidx.compose.foundation.background // ⭐️ background 임포트
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.ViewList
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.wearther.closet.data.ClosetImage
import com.example.wearther.closet.data.ClosetSortUtils
import com.example.wearther.closet.data.ClosetListItem

@Composable
fun ClosetViewSwitcher(
    items: List<ClosetImage>,
    onDelete: (ClosetImage) -> Unit
) {
    var isGridView by remember { mutableStateOf(true) }
    var selectedItem by remember { mutableStateOf<ClosetImage?>(null) }

    // ⭐️ 선으로 사용할 색상
    val lineColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)

    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.End
        ) {
            IconButton(onClick = { isGridView = !isGridView }) {
                Icon(
                    imageVector = if (isGridView) Icons.Default.ViewList else Icons.Default.GridView,
                    contentDescription = "뷰 전환"
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (isGridView) {
            // ⭐️ [수정됨] 그리드 뷰에 '선' 추가
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier
                    .fillMaxSize()
                    .background(lineColor), // ⭐️ 1. 그리드 전체 배경을 '선 색상'으로
                horizontalArrangement = Arrangement.spacedBy(1.dp), // ⭐️ 2. 1dp 틈 (선)
                verticalArrangement = Arrangement.spacedBy(1.dp),   // ⭐️ 2. 1dp 틈 (선)
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                items(items) { item ->
                    // (ItemCard.kt의 배경색은 surface여야 함)
                    val subKo = ClosetSortUtils.toKorean(item.category)
                    val mainKo = ClosetSortUtils.getMainCategory(subKo)

                    ItemCard(
                        imageUrl = item.url,
                        bigCategory = mainKo,
                        subCategory = subKo,
                        colorNames = item.colors,
                        onClick = { selectedItem = item }
                    )
                }
            }
        } else {
            // ⭐️ [유지] 리스트 뷰는 Divider로 선을 그림
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize(),
                verticalArrangement = Arrangement.Top,
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                items(items) { item ->
                    val subKo = ClosetSortUtils.toKorean(item.category)
                    val mainKo = ClosetSortUtils.getMainCategory(subKo)

                    ClosetListItem(
                        imageUrl = item.url,
                        bigCategory = mainKo,
                        subCategory = subKo,
                        colorNames = item.colors,
                        material = item.material,
                        uploaded_at = item.uploaded_at, // ⭐️ 이 줄을 추가해야 합니다!
                        onClick = { selectedItem = item }
                    )
                    // ⭐️ 리스트 아이템 사이에 구분선
                    Divider(color = lineColor, thickness = 1.dp)
                }
            }
        }
    }

    selectedItem?.let { item ->
        ClothingDetailDialog(
            item = item,
            onDismiss = { selectedItem = null },
            onUpdate = { imageId, category, colors, material, temperature ->
                // ViewModel의 updateImage 호출 (이미 있는 로직)
            }
        )
    }
}