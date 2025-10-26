package com.example.wearther.closet.upload

import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage

@Composable
fun AIResultCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFE8F5E9)
        )
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.AutoAwesome,
                contentDescription = null,
                tint = Color(0xFF2E7D32)
            )
            Spacer(Modifier.width(8.dp))
            Column {
                Text(
                    "AI가 분석한 결과입니다",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2E7D32)
                )
                Text(
                    "정확하지 않다면 아래에서 직접 수정해주세요",
                    fontSize = 12.sp,
                    color = Color(0xFF558B2F)
                )
            }
        }
    }
}

@Composable
fun ImagePreviewCard(imageUri: Uri) {
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .aspectRatio(0.65f),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            AsyncImage(
                model = imageUri,
                contentDescription = "선택된 이미지",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }
    }
}

@Composable
fun TypeSelectionSection(
    typeCategoryMap: Map<String, List<String>>,
    selectedType: String,
    onTypeSelected: (String) -> Unit
) {
    Column {
        Text("옷의 종류", fontWeight = FontWeight.Bold, fontSize = 18.sp)
        Spacer(Modifier.height(12.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            items(typeCategoryMap.keys.toList()) { type ->
                FilterChip(
                    selected = selectedType == type,
                    onClick = { onTypeSelected(type) },
                    label = { Text(type) },
                    shape = RoundedCornerShape(20.dp),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = Color.White
                    )
                )
            }
        }
    }
}

@Composable
fun CategorySelectionSection(
    typeCategoryMap: Map<String, List<String>>,
    selectedType: String,
    selectedCategory: String,
    onCategorySelected: (String) -> Unit
) {
    Column {
        Text("세부 카테고리", fontWeight = FontWeight.Bold, fontSize = 18.sp)
        Spacer(Modifier.height(12.dp))

        val categories = typeCategoryMap[selectedType] ?: emptyList()
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.height(((categories.size / 3 + 1) * 56).dp)
        ) {
            items(categories) { category ->
                FilterChip(
                    selected = selectedCategory == category,
                    onClick = { onCategorySelected(category) },
                    label = { Text(category, fontSize = 14.sp) },
                    shape = RoundedCornerShape(20.dp),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = Color.White,
                        containerColor = Color.Gray.copy(alpha = 0.1f),
                        labelColor = Color.Black
                    ),
                    modifier = Modifier.height(40.dp)
                )
            }
        }
    }
}

@Composable
fun ColorSelectionSection(
    colorOptions: List<String>,
    selectedColors: List<String>,
    onColorsChanged: (List<String>) -> Unit
) {
    Column {
        Text("색상 (복수 선택 가능)", fontWeight = FontWeight.Bold, fontSize = 18.sp)
        Spacer(Modifier.height(12.dp))

        LazyVerticalGrid(
            columns = GridCells.Fixed(5),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.height(((colorOptions.size / 5 + 1) * 48).dp)
        ) {
            items(colorOptions) { color ->
                FilterChip(
                    selected = selectedColors.contains(color),
                    onClick = {
                        onColorsChanged(
                            if (selectedColors.contains(color)) {
                                selectedColors - color
                            } else {
                                selectedColors + color
                            }
                        )
                    },
                    label = { Text(color, fontSize = 12.sp) },
                    shape = RoundedCornerShape(16.dp),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = Color.White,
                        containerColor = Color.Gray.copy(alpha = 0.1f),
                        labelColor = Color.Black
                    ),
                    modifier = Modifier.height(36.dp)
                )
            }
        }
    }
}

@Composable
fun MaterialSelectionSection(
    materialOptions: List<String>,
    selectedMaterial: String,
    onMaterialSelected: (String) -> Unit
) {
    Column {
        Text("소재", fontWeight = FontWeight.Bold, fontSize = 18.sp)
        Spacer(Modifier.height(12.dp))

        LazyVerticalGrid(
            columns = GridCells.Fixed(5),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.height(((materialOptions.size / 5 + 1) * 48).dp)
        ) {
            items(materialOptions) { material ->
                FilterChip(
                    selected = selectedMaterial == material,
                    onClick = { onMaterialSelected(material) },
                    label = { Text(material, fontSize = 12.sp) },
                    shape = RoundedCornerShape(16.dp),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = Color.White,
                        containerColor = Color.Gray.copy(alpha = 0.1f),
                        labelColor = Color.Black
                    ),
                    modifier = Modifier.height(36.dp)
                )
            }
        }
    }
}

@Composable
fun SelectionSummaryCard(
    selectedType: String,
    selectedCategory: String,
    selectedColors: List<String>,
    selectedMaterial: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(16.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF2C2C2C)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = Color(0xFF4CAF50),
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    "선택 완료",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            Text(
                "$selectedType > $selectedCategory",
                fontSize = 14.sp,
                color = Color.White.copy(alpha = 0.9f)
            )

            Text(
                "색상: ${selectedColors.joinToString(", ")}",
                fontSize = 13.sp,
                color = Color.White.copy(alpha = 0.8f)
            )

            Text(
                "소재: $selectedMaterial",
                fontSize = 13.sp,
                color = Color.White.copy(alpha = 0.8f)
            )
        }
    }
}