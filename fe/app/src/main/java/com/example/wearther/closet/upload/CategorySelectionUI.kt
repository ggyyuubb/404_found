package com.example.wearther.closet.upload

import android.net.Uri
// ⭐️ ClosetSortUtils 임포트 추가
import com.example.wearther.closet.data.ClosetSortUtils
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategorySelectionScreen(
    selectedImageUri: Uri,
    aiResult: AIAnalysisResult?,
    onNavigateBack: () -> Unit,
    onUploadSuccess: () -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // 영문 색상 → 한글 색상 변환 (EditClothingDialog와 동일)
    val colorNameMap = mapOf(
        "Beige" to "베이지",
        "Black" to "블랙",
        "Blue" to "블루",
        "Brown" to "브라운",
        "Gray" to "그레이",
        "Green" to "그린",
        "Orange" to "오렌지",
        "Pink" to "핑크",
        "Purple" to "퍼플",
        "Red" to "레드",
        "White" to "화이트",
        "Yellow" to "옐로우",
        "Navy" to "네이비",
        "Khaki" to "카키",
        "Ivory" to "아이보리"
    )

    // 한글 색상 → 영문 색상 (업로드용)
    val colorNameReverseMap = colorNameMap.entries.associate { (k, v) -> v to k }

    // AI 결과의 영문 색상을 한글로 변환
    val initialColors = aiResult?.colors?.mapNotNull { colorNameMap[it] } ?: emptyList()


    // ⭐️ [수정됨] AI의 영문 카테고리를 ClosetSortUtils를 이용해 한글로 변환
    val initialCategoryInKorean = ClosetSortUtils.toKorean(aiResult?.category)

    // ⭐️ [수정됨] "미분류"가 반환되면, 버튼 선택이 안 되도록 빈 값("")으로 처리
    val initialCategory = if (initialCategoryInKorean == "미분류") "" else initialCategoryInKorean


    var selectedType by remember { mutableStateOf(aiResult?.type ?: "") }
    // ⭐️ [수정됨] 한글로 변환된 값으로 상태 초기화
    var selectedCategory by remember { mutableStateOf(initialCategory) }
    var selectedColors by remember { mutableStateOf(initialColors) }
    var selectedMaterial by remember { mutableStateOf(aiResult?.material ?: "") }
    var isUploading by remember { mutableStateOf(false) }

    val canProceed = selectedType.isNotEmpty() &&
            selectedCategory.isNotEmpty() &&
            selectedColors.isNotEmpty() &&
            selectedMaterial.isNotEmpty() &&
            !isUploading

    val buttonColor by animateColorAsState(
        targetValue = if (canProceed) Color.Black else Color.Gray,
        label = "ButtonColorAnim"
    )
    val scale by animateFloatAsState(
        targetValue = if (canProceed) 1f else 0.95f,
        label = "ButtonScaleAnim"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("카테고리 확인", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "뒤로가기")
                    }
                },
                actions = {
                    FilledTonalButton(
                        onClick = {
                            if (!isUploading) {
                                isUploading = true
                                // 한글 색상 → 영문 색상 변환
                                val colorsInEnglish = selectedColors.mapNotNull { colorNameReverseMap[it] }

                                // ⭐️ [수정 없음]
                                // selectedCategory는 "반소매" (한글) 상태이며,
                                // handleUpload 내부에서 ClosetSortUtils.toEnglish()를 호출하므로
                                // 이 코드는 이미 올바르게 작동합니다.
                                handleUpload(
                                    context = context,
                                    coroutineScope = coroutineScope,
                                    selectedImageUri = selectedImageUri,
                                    selectedType = selectedType,
                                    selectedCategory = selectedCategory, // "반소매" (한글) 전달
                                    selectedColors = colorsInEnglish,
                                    selectedMaterial = selectedMaterial,
                                    onSuccess = onUploadSuccess,
                                    onError = { isUploading = false }
                                )
                            }
                        },
                        enabled = canProceed,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .scale(scale)
                            .padding(end = 8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = buttonColor,
                            contentColor = Color.White
                        ),
                        elevation = ButtonDefaults.buttonElevation(
                            defaultElevation = if (canProceed) 6.dp else 0.dp
                        )
                    ) {
                        if (isUploading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp,
                                color = Color.White
                            )
                            Spacer(Modifier.width(8.dp))
                            Text("업로드 중...")
                        } else {
                            Text("업로드")
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            CategorySelectionContent(
                selectedImageUri = selectedImageUri,
                aiResult = aiResult,
                selectedType = selectedType,
                selectedCategory = selectedCategory, // ⬅️ "반소매" (한글)가 전달됨
                selectedColors = selectedColors,
                selectedMaterial = selectedMaterial,
                onTypeSelected = { type ->
                    val oldType = selectedType
                    selectedType = type
                    // 상위 카테고리 변경 시 하위 카테고리 초기화
                    if (type != oldType) {
                        selectedCategory = ""
                    }
                },
                onCategorySelected = { selectedCategory = it },
                onColorsChanged = { selectedColors = it },
                onMaterialSelected = { selectedMaterial = it },
                canProceed = canProceed
            )
        }
    }
}

// -------------------------------------------------------------------
// [수정됨] CategorySelectionContent - 색상과 소재 옵션 수정
// -------------------------------------------------------------------
@Composable
fun CategorySelectionContent(
    selectedImageUri: Uri,
    aiResult: AIAnalysisResult?,
    selectedType: String,
    selectedCategory: String,
    selectedColors: List<String>,
    selectedMaterial: String,
    onTypeSelected: (String) -> Unit,
    onCategorySelected: (String) -> Unit,
    onColorsChanged: (List<String>) -> Unit,
    onMaterialSelected: (String) -> Unit,
    canProceed: Boolean
) {

    val typeCategoryMap = mapOf(
        "상의" to listOf("민소매", "반소매", "긴소매", "후드", "셔츠", "스웨터"),
        "하의" to listOf("데님", "트레이닝", "면바지", "스커트"),
        "아우터" to listOf("후드집업", "가디건", "블레이저", "코트", "플리스", "숏패딩", "롱패딩"),
        "원피스" to listOf("원피스")
    )

    // ⭐️ EditClothingDialog와 동일한 색상 옵션 (15개)
    val colorOptions = listOf(
        "베이지", "블랙", "블루", "브라운", "그레이",
        "그린", "오렌지", "핑크", "퍼플", "레드",
        "화이트", "옐로우", "네이비", "카키", "아이보리"
    )

    // ⭐️ EditClothingDialog와 동일한 소재 옵션 (13개)
    val materialOptions = listOf(
        "면", "데님", "린넨/마", "가죽", "퍼/털", "시폰/얇은 원단",
        "실크/새틴", "스판덱스/신축성 스포츠 원단", "나일론/방수원단",
        "울/정장소재", "니트/스웨터소재", "벨벳", "트위드"
    )

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // AI 분석 결과 카드
            if (aiResult != null) {
                AIResultCard()
            }

            // 이미지 프리뷰
            ImagePreviewCard(selectedImageUri)

            // 옷의 종류
            TypeSelectionSection(
                typeCategoryMap = typeCategoryMap,
                selectedType = selectedType,
                onTypeSelected = onTypeSelected
            )

            // 세부 카테고리
            if (selectedType.isNotEmpty()) {
                CategorySelectionSection(
                    typeCategoryMap = typeCategoryMap,
                    selectedType = selectedType,
                    selectedCategory = selectedCategory, // ⬅️ "반소매" (한글)가 전달됨
                    onCategorySelected = onCategorySelected
                )
            }

            // 색상 선택 (한글 표시)
            ColorSelectionSection(
                colorOptions = colorOptions,
                selectedColors = selectedColors,
                onColorsChanged = onColorsChanged
            )

            // 소재 선택
            MaterialSelectionSection(
                materialOptions = materialOptions,
                selectedMaterial = selectedMaterial,
                onMaterialSelected = onMaterialSelected
            )

            Spacer(Modifier.height(80.dp))
        }

        // 선택 완료 카드
        if (canProceed) {
            SelectionSummaryCard(
                selectedType = selectedType,
                selectedCategory = selectedCategory,
                selectedColors = selectedColors,
                selectedMaterial = selectedMaterial,
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }
    }
}