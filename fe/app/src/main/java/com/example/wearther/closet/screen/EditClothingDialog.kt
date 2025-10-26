package com.example.wearther.closet.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.wearther.closet.data.ClosetImage
import com.example.wearther.closet.data.ClosetSortUtils
import com.example.wearther.closet.data.nameToColor
import androidx.compose.material3.ExperimentalMaterial3Api

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditClothingDialog(
    item: ClosetImage,
    onDismiss: () -> Unit,
    onConfirm: (category: String, colors: List<String>, material: String, temperature: String) -> Unit
) {
    // 초기값
    val initialSubKo = ClosetSortUtils.toKorean(item.category)
    val initialMainKo = ClosetSortUtils.getMainCategory(initialSubKo)

    var selectedMainCategory by remember { mutableStateOf(initialMainKo) }
    var selectedSubCategory by remember { mutableStateOf(initialSubKo) }
    var selectedColors by remember { mutableStateOf(item.colors ?: emptyList()) }
    var selectedMaterial by remember { mutableStateOf(item.material ?: "") }
    var selectedTemperature by remember {
        mutableStateOf(reverseTemperatureRange(item.suitable_temperature ?: ""))
    }

    // 드롭다운 상태
    var expandedCategory by remember { mutableStateOf(false) }
    var expandedColors by remember { mutableStateOf(false) }
    var expandedMaterial by remember { mutableStateOf(false) }
    var expandedTemperature by remember { mutableStateOf(false) }

    // 옵션 리스트
    val mainCategories = listOf("상의", "하의", "아우터", "원피스")
    val availableColors = listOf(
        "베이지", "블랙", "블루", "브라운", "그레이",
        "그린", "오렌지", "핑크", "퍼플", "레드",
        "화이트", "옐로우", "네이비", "카키", "아이보리"
    )
    val materials = listOf(
        "면", "데님", "린넨/마", "가죽", "퍼/털", "시폰/얇은 원단",
        "실크/새틴", "스판덱스/신축성 스포츠 원단", "나일론/방수원단",
        "울/정장소재", "니트/스웨터소재", "벨벳", "트위드"
    )
    val temperatureRanges = listOf(
        "28℃~", "23℃~27℃", "20℃~22℃", "17℃~19℃",
        "12℃~16℃", "9℃~11℃", "5℃~8℃", "~4℃"
    )

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp)
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
                        text = "옷 정보 수정",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "닫기")
                    }
                }

                // 대분류 - 2줄로 배치
                Text(
                    text = "대분류",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    // 첫 번째 줄: 상의, 하의
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("상의", "하의").forEach { category ->
                            FilterChip(
                                selected = selectedMainCategory == category,
                                onClick = {
                                    selectedMainCategory = category
                                    selectedSubCategory = ClosetSortUtils.getSubCategories(category).firstOrNull() ?: "미분류"
                                },
                                label = { Text(category) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                    // 두 번째 줄: 아우터, 원피스
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("아우터", "원피스").forEach { category ->
                            FilterChip(
                                selected = selectedMainCategory == category,
                                onClick = {
                                    selectedMainCategory = category
                                    selectedSubCategory = ClosetSortUtils.getSubCategories(category).firstOrNull() ?: "미분류"
                                },
                                label = { Text(category) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                // 세부 카테고리 (드롭다운)
                Text(
                    text = "세부 카테고리",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                ExposedDropdownMenuBox(
                    expanded = expandedCategory,
                    onExpandedChange = { expandedCategory = it }
                ) {
                    OutlinedTextField(
                        value = selectedSubCategory,
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = {
                            Icon(Icons.Default.ArrowDropDown, null)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        colors = OutlinedTextFieldDefaults.colors()
                    )
                    ExposedDropdownMenu(
                        expanded = expandedCategory,
                        onDismissRequest = { expandedCategory = false }
                    ) {
                        ClosetSortUtils.getSubCategories(selectedMainCategory).forEach { sub ->
                            DropdownMenuItem(
                                text = { Text(sub) },
                                onClick = {
                                    selectedSubCategory = sub
                                    expandedCategory = false
                                }
                            )
                        }
                    }
                }

                // 색상 (드롭다운 - 복수 선택)
                Text(
                    text = "색상 (복수 선택 가능)",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                ExposedDropdownMenuBox(
                    expanded = expandedColors,
                    onExpandedChange = { expandedColors = it }
                ) {
                    OutlinedTextField(
                        value = selectedColors.joinToString(", "),
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = {
                            Icon(Icons.Default.ArrowDropDown, null)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        placeholder = { Text("색상 선택") }
                    )
                    ExposedDropdownMenu(
                        expanded = expandedColors,
                        onDismissRequest = { expandedColors = false }
                    ) {
                        availableColors.forEach { colorKo ->
                            val colorEn = availableColors.indexOf(colorKo).let { idx ->
                                listOf("Beige", "Black", "Blue", "Brown", "Gray",
                                    "Green", "Orange", "Pink", "Purple", "Red",
                                    "White", "Yellow", "Navy", "Khaki", "Ivory")[idx]
                            }
                            DropdownMenuItem(
                                text = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Checkbox(
                                            checked = selectedColors.contains(colorEn),
                                            onCheckedChange = null
                                        )
                                        Spacer(Modifier.width(8.dp))
                                        Box(
                                            modifier = Modifier
                                                .size(16.dp)
                                                .border(1.dp, Color(0x44000000), RoundedCornerShape(4.dp))
                                                .background(nameToColor(colorKo), RoundedCornerShape(4.dp))
                                        )
                                        Spacer(Modifier.width(8.dp))
                                        Text(colorKo)
                                    }
                                },
                                onClick = {
                                    selectedColors = if (selectedColors.contains(colorEn)) {
                                        selectedColors - colorEn
                                    } else {
                                        selectedColors + colorEn
                                    }
                                }
                            )
                        }
                    }
                }

                // 소재 (드롭다운)
                Text(
                    text = "소재",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                ExposedDropdownMenuBox(
                    expanded = expandedMaterial,
                    onExpandedChange = { expandedMaterial = it }
                ) {
                    OutlinedTextField(
                        value = selectedMaterial,
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = {
                            Icon(Icons.Default.ArrowDropDown, null)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        placeholder = { Text("소재 선택") }
                    )
                    ExposedDropdownMenu(
                        expanded = expandedMaterial,
                        onDismissRequest = { expandedMaterial = false }
                    ) {
                        materials.forEach { mat ->
                            DropdownMenuItem(
                                text = { Text(mat) },
                                onClick = {
                                    selectedMaterial = mat
                                    expandedMaterial = false
                                }
                            )
                        }
                    }
                }

                // 적정 온도 (드롭다운)
                Text(
                    text = "적정 온도 (℃)",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                ExposedDropdownMenuBox(
                    expanded = expandedTemperature,
                    onExpandedChange = { expandedTemperature = it }
                ) {
                    OutlinedTextField(
                        value = selectedTemperature,
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = {
                            Icon(Icons.Default.ArrowDropDown, null)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        placeholder = { Text("온도 범위 선택") }
                    )
                    ExposedDropdownMenu(
                        expanded = expandedTemperature,
                        onDismissRequest = { expandedTemperature = false }
                    ) {
                        temperatureRanges.forEach { temp ->
                            DropdownMenuItem(
                                text = { Text(temp) },
                                onClick = {
                                    selectedTemperature = temp
                                    expandedTemperature = false
                                }
                            )
                        }
                    }
                }

                // 버튼들
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("취소")
                    }
                    Button(
                        onClick = {
                            val englishCategory = ClosetSortUtils.toEnglish(selectedSubCategory) ?: "longsleeve"
                            val serverTemperature = reverseTemperatureRange(selectedTemperature)
                            onConfirm(englishCategory, selectedColors, selectedMaterial, serverTemperature)
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("저장")
                    }
                }
            }
        }
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