package com.example.wearther.closet.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.example.wearther.closet.data.ClosetImage
import com.example.wearther.closet.data.ClosetSortUtils
import com.example.wearther.closet.data.nameToColor

@Composable
fun ClothingDetailDialog(
    item: ClosetImage,
    onDismiss: () -> Unit,
    onUpdate: (
        imageId: String,
        category: String,
        colors: List<String>,
        material: String,
        temperature: String
    ) -> Unit
) {
    // ⭐️ 로그 추가
    android.util.Log.d("ClothingDetailDialog", "========== 다이얼로그 열림 ==========")
    android.util.Log.d("ClothingDetailDialog", "아이템 ID: '${item.id}'")
    android.util.Log.d("ClothingDetailDialog", "uploaded_at (원본): '${item.uploaded_at}'")
    android.util.Log.d("ClothingDetailDialog", "uploaded_at isNullOrBlank: ${item.uploaded_at.isNullOrBlank()}")
    android.util.Log.d("ClothingDetailDialog", "전체 아이템 정보: $item")
    android.util.Log.d("ClothingDetailDialog", "=========================================")

    var showEditDialog by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 헤더
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "옷 상세정보",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "닫기")
                    }
                }

                // 이미지
                AsyncImage(
                    model = item.url,
                    contentDescription = "옷 이미지",
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(0.75f)
                        .clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Crop
                )

                // 상세 정보
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // 카테고리
                    val subKo = ClosetSortUtils.toKorean(item.category)
                    val mainKo = ClosetSortUtils.getMainCategory(subKo)
                    InfoRow(
                        label = "카테고리",
                        value = "$mainKo · $subKo"
                    )

                    // 색상
                    if (!item.colors.isNullOrEmpty()) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = "색상",
                                fontSize = 16.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.SemiBold
                            )
                            item.colors.forEach { colorName ->
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(16.dp)
                                            .border(1.dp, Color(0x44000000), RoundedCornerShape(4.dp))
                                            .background(nameToColor(colorName), RoundedCornerShape(4.dp))
                                    )
                                    Text(
                                        text = colorName,
                                        fontSize = 15.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }

                    // 소재
                    if (!item.material.isNullOrBlank()) {
                        InfoRow(
                            label = "소재",
                            value = item.material
                        )
                    }

                    // 적정 온도
                    if (!item.suitable_temperature.isNullOrBlank()) {
                        InfoRow(
                            label = "적정 온도",
                            value = reverseTemperatureRange(item.suitable_temperature)
                        )
                    }

                    // 업로드 시각
                    val uploadedAtDisplay = item.uploaded_at ?: "정보 없음"
                    android.util.Log.d("ClothingDetailDialog", "표시할 uploaded_at: '$uploadedAtDisplay'")

                    InfoRow(
                        label = "업로드",
                        value = uploadedAtDisplay
                    )
                }

                // 정보 수정 버튼
                Button(
                    onClick = { showEditDialog = true },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("정보 수정")
                }
            }
        }
    }

    // 편집 다이얼로그
    if (showEditDialog) {
        EditClothingDialog(
            item = item,
            onDismiss = { showEditDialog = false },
            onConfirm = { category, colors, material, temperature ->
                onUpdate(item.id, category, colors, material, temperature)
                showEditDialog = false
                onDismiss()
            }
        )
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = label,
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = value,
            fontSize = 15.sp,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

// ⭐️ 온도 범위 순서 변환 함수
// 서버: 22~20 (큰 숫자 먼저)
// 화면: 20~22 (작은 숫자 먼저)
private fun reverseTemperatureRange(temp: String): String {
    if (temp.isBlank()) return ""

    // "22~20" → "20~22" 또는 "28~" → "28~" (변환 없음)
    val parts = temp.replace("℃", "").split("~")
    return when {
        parts.size == 2 && parts[0].isNotBlank() && parts[1].isNotBlank() -> {
            // 양쪽 모두 숫자가 있는 경우
            "${parts[1]}℃~${parts[0]}℃"
        }
        temp.startsWith("~") -> {
            // "~4℃" 형식
            temp
        }
        temp.endsWith("~") -> {
            // "28℃~" 형식
            temp
        }
        else -> temp
    }
}