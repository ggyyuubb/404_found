package com.example.wearther.closet.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import androidx.compose.ui.window.DialogProperties
import coil.compose.rememberAsyncImagePainter
import com.example.wearther.closet.data.ClosetImage
import com.example.wearther.ui.screens.closet.components.CategoryMapper

@Composable
fun ClothingDetailDialog(
    item: ClosetImage,
    onDismiss: () -> Unit,
    onDelete: () -> Unit,
    onUpdate: (clothingType: String, colors: List<String>, material: String, temperature: String) -> Unit = { _, _, _, _ -> }
) {
    var isEditMode by remember { mutableStateOf(false) }

    var editClothingType by remember { mutableStateOf(CategoryMapper.toKorean(item.clothing_type)) }
    var editColors by remember { mutableStateOf(item.colors ?: emptyList()) }
    var editMaterial by remember { mutableStateOf(item.material ?: "") }
    var editTemperature by remember { mutableStateOf(item.suitable_temperature ?: "") }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.85f),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.primary)
                        .padding(16.dp)
                ) {
                    Text(
                        text = if (isEditMode) "옷 정보 수정" else "옷 상세 정보",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.align(Alignment.CenterStart)
                    )

                    Row(
                        modifier = Modifier.align(Alignment.CenterEnd),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // 수정/저장 버튼
                        Button(
                            onClick = {
                                if (isEditMode) {
                                    val englishCategory = CategoryMapper.toEnglish(editClothingType) ?: editClothingType
                                    onUpdate(englishCategory, editColors, editMaterial, editTemperature)
                                }
                                isEditMode = !isEditMode
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.White,
                                contentColor = MaterialTheme.colorScheme.primary
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.height(40.dp)
                        ) {
                            Text(
                                text = if (isEditMode) "저장" else "수정",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = if (isEditMode) Icons.Default.Check else Icons.Default.Edit,
                                contentDescription = if (isEditMode) "저장" else "수정",
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        IconButton(onClick = onDismiss) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "닫기",
                                tint = Color.White
                            )
                        }
                    }
                }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Image(
                        painter = rememberAsyncImagePainter(item.url),
                        contentDescription = "옷 이미지",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(350.dp)
                            .clip(RoundedCornerShape(16.dp)),
                        contentScale = ContentScale.Crop
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // 옷 종류
                    if (isEditMode) {
                        CategoryDropdown(
                            title = "옷 종류",
                            selectedValue = editClothingType,
                            onValueChange = { editClothingType = it }
                        )
                    } else {
                        DetailInfoCard("옷 종류", editClothingType)
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // 색상
                    if (isEditMode) {
                        ColorSelector(
                            title = "색상 (여러 개 선택 가능)",
                            selectedColors = editColors,
                            onColorsChange = { editColors = it }
                        )
                    } else {
                        DetailInfoCard("색상", CategoryMapper.colorsToKorean(editColors))
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // 재질
                    if (isEditMode) {
                        MaterialDropdown(
                            title = "재질",
                            selectedValue = editMaterial,
                            onValueChange = { editMaterial = it }
                        )
                    } else {
                        DetailInfoCard("재질", editMaterial.takeIf { it.isNotEmpty() } ?: "정보 없음")
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // 적정 온도
                    if (isEditMode) {
                        EditableInfoCard(
                            title = "적정 온도",
                            value = editTemperature,
                            onValueChange = { editTemperature = it },
                            placeholder = "예: 20-25°C"
                        )
                    } else {
                        DetailInfoCard("적정 온도", editTemperature.takeIf { it.isNotEmpty() } ?: "정보 없음")
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    DetailInfoCard("업로드 날짜", item.uploaded_at?.trim()?.takeIf { it.isNotEmpty() } ?: "정보 없음")

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = {
                            onDelete()
                            onDismiss()
                        },
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF5252)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("이 옷 삭제하기", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CategoryDropdown(
    title: String,
    selectedValue: String,
    onValueChange: (String) -> Unit
) {
    val categories = listOf("민소매", "반소매", "긴소매", "후드", "셔츠", "스웨터", "점퍼",
        "면바지", "데님", "트레이닝팬츠", "슬랙스", "반바지", "스커트",
        "블레이저", "가디건", "코트", "롱패딩", "숏패딩", "후드집업", "플리스", "원피스")

    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Text(title, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(8.dp))
            ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
                OutlinedTextField(
                    value = selectedValue,
                    onValueChange = {},
                    readOnly = true,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    modifier = Modifier.fillMaxWidth().menuAnchor(),
                    colors = OutlinedTextFieldDefaults.colors()
                )
                ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    categories.forEach { category ->
                        DropdownMenuItem(
                            text = { Text(category) },
                            onClick = {
                                onValueChange(category)
                                expanded = false
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ColorSelector(
    title: String,
    selectedColors: List<String>,
    onColorsChange: (List<String>) -> Unit
) {
    val allColors = listOf("베이지", "블랙", "블루", "브라운", "그레이", "그린",
        "오렌지", "핑크", "퍼플", "레드", "화이트", "옐로우")

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Text(title, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(8.dp))

            allColors.chunked(3).forEach { rowColors ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    rowColors.forEach { color ->
                        val isSelected = selectedColors.contains(color)
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                onColorsChange(
                                    if (isSelected) selectedColors - color
                                    else selectedColors + color
                                )
                            },
                            label = { Text(color, fontSize = 12.sp) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    repeat(3 - rowColors.size) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MaterialDropdown(
    title: String,
    selectedValue: String,
    onValueChange: (String) -> Unit
) {
    val materials = listOf("면", "데님", "니트", "린넨/마", "가죽", "퍼/털", "시폰/얇은 원단",
        "실크/새틴", "스판덱스/신축성 스포츠 원단", "나일론/방수원단", "울/정장소재",
        "니트/스웨터소재", "벨벳", "트위드")

    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Text(title, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(8.dp))
            ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
                OutlinedTextField(
                    value = selectedValue,
                    onValueChange = {},
                    readOnly = true,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    modifier = Modifier.fillMaxWidth().menuAnchor(),
                    colors = OutlinedTextFieldDefaults.colors()
                )
                ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    materials.forEach { material ->
                        DropdownMenuItem(
                            text = { Text(material) },
                            onClick = {
                                onValueChange(material)
                                expanded = false
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailInfoCard(title: String, content: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Text(title, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(8.dp))
            Text(content, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface)
        }
    }
}

@Composable
private fun EditableInfoCard(
    title: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String = ""
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Text(title, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text(placeholder) },
                shape = RoundedCornerShape(8.dp)
            )
        }
    }
}