package com.example.wearther.closet.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
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
import com.example.wearther.closet.data.ClosetSortUtils

/* ---------------- helpers: 색상 한↔영 ---------------- */

private val COLORS_KO = listOf(
    "베이지","블랙","블루","브라운","그레이","그린",
    "오렌지","핑크","퍼플","레드","화이트","옐로우","네이비","카키","아이보리"
)

private val KO_TO_EN_COLOR = mapOf(
    "베이지" to "beige",
    "블랙" to "black",
    "블루" to "blue",
    "브라운" to "brown",
    "그레이" to "gray",
    "그린" to "green",
    "오렌지" to "orange",
    "핑크" to "pink",
    "퍼플" to "purple",
    "레드" to "red",
    "화이트" to "white",
    "옐로우" to "yellow",
    "네이비" to "navy",
    "카키" to "khaki",
    "아이보리" to "ivory"
)

private fun enListToKoList(en: List<String>?): List<String> =
    en.orEmpty().map { ClosetSortUtils.colorToKorean(it) }

/* ---------------- Dialog ---------------- */

@Composable
fun ClothingDetailDialog(
    item: ClosetImage,
    onDismiss: () -> Unit,
    onDelete: () -> Unit,
    // 저장 시: englishSubCategory(=category), englishColors, material, temperature
    onUpdate: (clothingType: String, colors: List<String>, material: String, temperature: String) -> Unit = { _, _, _, _ -> }
) {
    var isEditMode by remember { mutableStateOf(false) }

    // 세부카테고리(한글) 상태
    var editSubKo by remember { mutableStateOf(ClosetSortUtils.toKorean(item.category)) }
    // 색상(한글 목록) 상태
    var editColorsKo by remember { mutableStateOf(enListToKoList(item.colors)) }
    var editMaterial by remember { mutableStateOf(item.material ?: "") }
    var editTemperature by remember { mutableStateOf(item.suitable_temperature ?: "") }

    // 아이템이 바뀔 때마다 상태 초기화
    LaunchedEffect(item.id, item.category, item.colors, item.material, item.suitable_temperature) {
        editSubKo = ClosetSortUtils.toKorean(item.category)
        editColorsKo = enListToKoList(item.colors)
        editMaterial = item.material ?: ""
        editTemperature = item.suitable_temperature ?: ""
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.85f),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(Modifier.fillMaxSize()) {

                /* 헤더 */
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
                        Button(
                            onClick = {
                                if (isEditMode) {
                                    // 저장 직전 변환들
                                    val englishSub =
                                        ClosetSortUtils.toEnglish(editSubKo) ?: item.category ?: ""

                                    // 한글 색상 → 영문, 서버 표기(첫 문자 대문자)
                                    val englishColors = editColorsKo
                                        .mapNotNull { KO_TO_EN_COLOR[it] }
                                        .map { it.replaceFirstChar { c -> c.titlecase() } }

                                    onUpdate(
                                        englishSub,          // 서버 category 필드로 쓸 영문 세부
                                        englishColors,       // ["Blue","Gray"] 같은 배열
                                        editMaterial,
                                        editTemperature
                                    )
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
                            Text(if (isEditMode) "저장" else "수정", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Spacer(Modifier.width(4.dp))
                            Icon(
                                imageVector = if (isEditMode) Icons.Default.Check else Icons.Default.Edit,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Default.Close, contentDescription = "닫기", tint = Color.White)
                        }
                    }
                }

                /* 내용 */
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    // 이미지
                    Image(
                        painter = rememberAsyncImagePainter(item.url),
                        contentDescription = "옷 이미지",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(350.dp)
                            .clip(RoundedCornerShape(16.dp)),
                        contentScale = ContentScale.Crop
                    )

                    Spacer(Modifier.height(24.dp))

                    // 세부카테고리 (한글)
                    if (isEditMode) {
                        CategoryDropdown(
                            title = "세부 카테고리",
                            selectedValue = editSubKo,
                            onValueChange = { editSubKo = it }
                        )
                    } else {
                        val mainKo = ClosetSortUtils.getMainCategory(editSubKo)
                        DetailInfoCard("카테고리", "$mainKo - $editSubKo")
                    }

                    Spacer(Modifier.height(12.dp))

                    // 색상 (한글 선택/표시, 저장은 영어 배열로 변환)
                    if (isEditMode) {
                        ColorSelector(
                            title = "색상 (여러 개 선택 가능)",
                            selectedColorsKo = editColorsKo,
                            onColorsChangeKo = { editColorsKo = it }
                        )
                    } else {
                        val colorsText =
                            if (editColorsKo.isEmpty()) "정보 없음"
                            else editColorsKo.joinToString(", ")
                        DetailInfoCard("색상", colorsText)
                    }

                    Spacer(Modifier.height(12.dp))

                    // 재질
                    if (isEditMode) {
                        MaterialPicker(
                            label = "재질",
                            value = editMaterial,
                            onChange = { editMaterial = it }
                        )
                    } else {
                        DetailInfoCard("재질", editMaterial.ifBlank { "정보 없음" })
                    }

                    Spacer(Modifier.height(12.dp))

                    // 적정 온도
                    if (isEditMode) {
                        EditableInfoCard(
                            title = "적정 온도",
                            value = editTemperature,
                            onValueChange = { editTemperature = it },
                            placeholder = "예: 27℃~23℃"
                        )
                    } else {
                        DetailInfoCard("적정 온도", editTemperature.ifBlank { "정보 없음" })
                    }

                    Spacer(Modifier.height(12.dp))

                    DetailInfoCard("업로드 날짜", item.uploaded_at?.trim().orEmpty().ifBlank { "정보 없음" })

                    Spacer(Modifier.height(24.dp))

                    Button(
                        onClick = { onDelete(); onDismiss() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF5252)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("이 옷 삭제하기", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }

                    Spacer(Modifier.height(16.dp))
                }
            }
        }
    }
}

/* ---------------- UI subcomponents ---------------- */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CategoryDropdown(
    title: String,
    selectedValue: String,
    onValueChange: (String) -> Unit
) {
    // 한글 "세부카테고리" 목록
    val categories = listOf(
        // 상의
        "민소매","반소매","긴소매","후드","셔츠","스웨터",
        // 하의
        "반바지","면바지","데님","트레이닝바지","슬랙스","스커트",
        // 아우터
        "점퍼","블레이저","가디건","코트","롱패딩","숏패딩","후드집업","플리스",
        // 원피스
        "원피스"
    )

    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(Modifier.fillMaxWidth().padding(16.dp)) {
            Text(title, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(8.dp))
            ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
                OutlinedTextField(
                    value = selectedValue,
                    onValueChange = {},
                    readOnly = true,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                    modifier = Modifier.fillMaxWidth().menuAnchor(),
                    colors = OutlinedTextFieldDefaults.colors()
                )
                ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    categories.forEach { categoryKo ->
                        DropdownMenuItem(
                            text = { Text(categoryKo) },
                            onClick = { onValueChange(categoryKo); expanded = false }
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
    selectedColorsKo: List<String>,
    onColorsChangeKo: (List<String>) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(Modifier.fillMaxWidth().padding(16.dp)) {
            Text(title, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(8.dp))

            // 3개씩 칩 배치
            COLORS_KO.chunked(3).forEach { row ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    row.forEach { colorKo ->
                        val selected = selectedColorsKo.contains(colorKo)
                        FilterChip(
                            selected = selected,
                            onClick = {
                                onColorsChangeKo(
                                    if (selected) selectedColorsKo - colorKo else selectedColorsKo + colorKo
                                )
                            },
                            label = { Text(colorKo, fontSize = 12.sp) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    repeat(3 - row.size) { Spacer(Modifier.weight(1f)) }
                }
                Spacer(Modifier.height(8.dp))
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
        Column(Modifier.fillMaxWidth().padding(16.dp)) {
            Text(title, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(8.dp))
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
        Column(Modifier.fillMaxWidth().padding(16.dp)) {
            Text(title, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(8.dp))
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MaterialPicker(
    label: String,
    value: String,
    onChange: (String) -> Unit
) {
    val materials = listOf(
        "면", "데님", "니트", "린넨/마", "가죽", "퍼/털", "시폰/얇은 원단",
        "실크/새틴", "스판덱스/신축성 스포츠 원단", "나일론/방수원단",
        "울/정장소재", "니트/스웨터소재", "벨벳", "트위드"
    )

    var expanded by remember { mutableStateOf(false) }

    Column(Modifier.fillMaxWidth()) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(Modifier.height(8.dp))

        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            OutlinedTextField(
                value = value,
                onValueChange = {},
                readOnly = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(),
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                }
            )

            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                materials.forEach { m ->
                    DropdownMenuItem(
                        text = { Text(m) },
                        onClick = {
                            onChange(m)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}
