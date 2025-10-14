package com.example.wearther.closet.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.ViewList
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.wearther.closet.data.ClosetImage
import com.example.wearther.closet.data.ItemCard

@Composable
fun ClosetViewSwitcher(
    items: List<ClosetImage>,
    onDelete: (ClosetImage) -> Unit
) {
    var isGridView by remember { mutableStateOf(true) }
    var selectedItem by remember { mutableStateOf<ClosetImage?>(null) }

    Column {
        // 뷰 전환 버튼
        Row(
            modifier = Modifier.fillMaxWidth(),
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

        // 뷰 전환
        if (isGridView) {
            // 2열 그리드
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 16.dp)
                    .fillMaxSize(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                items(items) { item ->
                    ItemCard(
                        imageUrl = item.url,
                        clothingType = item.clothing_type,
                        onClick = { selectedItem = item },
                        onDelete = { onDelete(item) }
                    )
                }
            }
        } else {
            // 1열 리스트
            LazyColumn(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                items(items) { item ->
                    ItemCard(
                        imageUrl = item.url,
                        clothingType = item.clothing_type,
                        onClick = { selectedItem = item },
                        onDelete = { onDelete(item) }
                    )
                }
            }
        }
    }

    // 상세 다이얼로그
    selectedItem?.let { item ->
        ClothingDetailDialog(
            item = item,
            onDismiss = { selectedItem = null },
            onDelete = {
                onDelete(item)
                selectedItem = null
            }
        )
    }
}