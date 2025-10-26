package com.example.wearther.closet.screen

import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.wearther.closet.data.ClosetImage
import com.example.wearther.closet.data.ClosetViewModel
import com.example.wearther.closet.data.ClosetViewModelFactory
import com.example.wearther.closet.data.ClosetSortUtils
import com.example.wearther.closet.data.ClosetListItem
import com.example.wearther.remote.BASE_URL
import com.example.wearther.remote.getStoredJwtToken
import com.example.wearther.ui.screens.closet.components.CategoryTabs
import com.example.wearther.ui.screens.closet.components.Header
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

@Composable
fun ClosetScreen(
    onNavigateToUpload: (Uri) -> Unit = {}
) {
    val context = LocalContext.current
    val jwtToken = getStoredJwtToken(context)
    val tokenHeader = "Bearer $jwtToken"

    // ✅ Retrofit 인스턴스 생성
    val retrofit = remember {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
    val closetApi = retrofit.create(com.example.wearther.closet.data.ClosetApi::class.java)

    // ✅ ViewModel 생성
    val viewModel: ClosetViewModel = viewModel(
        factory = ClosetViewModelFactory(closetApi, tokenHeader)
    )

    // ✅ State 구독
    val filteredItems by viewModel.filteredItems.collectAsState()
    val activeCategory by viewModel.activeCategory.collectAsState()
    val activeSubCategory by viewModel.activeSubCategory.collectAsState()
    val currentSortOption by viewModel.currentSortOption.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    // ✅ UI State
    var isGridView by remember { mutableStateOf(true) }
    var selectedItem by remember { mutableStateOf<ClosetImage?>(null) }
    var showPhotoDialog by remember { mutableStateOf(false) }

    // ⭐️ 삭제 모드 관련 state
    var isDeleteMode by remember { mutableStateOf(false) }
    var selectedItemsForDelete by remember { mutableStateOf<Set<String>>(emptySet()) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }

    // ⭐️ 스크롤 상태 저장
    val gridState = rememberLazyGridState()
    val listState = rememberLazyListState()

    val topCategories = listOf("전체", "상의", "하의", "아우터", "원피스")

    // ✅ 에러 표시
    error?.let { errorMessage ->
        LaunchedEffect(errorMessage) {
            Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
            viewModel.clearError()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color(0xFFFFFFFF),
                            Color(0xFFFAFAFA),
                            Color(0xFFF5F5F5),
                            Color(0xFFFAFAFA),
                            Color(0xFFFFFFFF)
                        )
                    )
                )
        ) {
            // ✅ 카테고리 탭
            CategoryTabs(
                categories = topCategories,
                activeCategory = activeCategory,
                onCategoryChange = { viewModel.setCategory(it) },
                activeSubCategory = activeSubCategory,
                onSubCategoryChange = { viewModel.setSubCategory(it) }
            )

            // ✅ 헤더 (정렬, 뷰 전환)
            Header(
                totalItems = filteredItems.size,
                currentSortOption = currentSortOption,
                onSortChange = { viewModel.setSortOption(it) },
                isGridView = isGridView,
                onToggleView = { isGridView = !isGridView }
            )

            // ✅ 로딩 표시
            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            // ✅ 빈 상태
            else if (filteredItems.isEmpty()) {
                EmptyState()
            }
            // ✅ 그리드 뷰
            else if (isGridView) {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    state = gridState,
                    modifier = Modifier
                        .padding(horizontal = 16.dp, vertical = 16.dp)
                        .fillMaxSize(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    items(
                        items = filteredItems,
                        key = { it.id }
                    ) { item ->
                        val subKo = ClosetSortUtils.toKorean(item.category)
                        val mainKo = ClosetSortUtils.getMainCategory(subKo)

                        Box {
                            ItemCard(
                                imageUrl = item.url,
                                bigCategory = mainKo,
                                subCategory = subKo,
                                colorNames = item.colors,
                                onClick = {
                                    if (isDeleteMode) {
                                        // 삭제 모드: 선택/해제
                                        selectedItemsForDelete = if (selectedItemsForDelete.contains(item.id)) {
                                            selectedItemsForDelete - item.id
                                        } else {
                                            selectedItemsForDelete + item.id
                                        }
                                    } else {
                                        // 일반 모드: 상세보기
                                        selectedItem = item
                                    }
                                }
                            )

                            // ⭐️ 삭제 모드일 때 체크박스 표시
                            if (isDeleteMode) {
                                Checkbox(
                                    checked = selectedItemsForDelete.contains(item.id),
                                    onCheckedChange = {
                                        selectedItemsForDelete = if (it) {
                                            selectedItemsForDelete + item.id
                                        } else {
                                            selectedItemsForDelete - item.id
                                        }
                                    },
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .padding(8.dp)
                                )
                            }
                        }
                    }
                }
            }
            // ✅ 리스트 뷰
            else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    items(
                        items = filteredItems,
                        key = { it.id }
                    ) { item ->
                        val subKo = ClosetSortUtils.toKorean(item.category)
                        val mainKo = ClosetSortUtils.getMainCategory(subKo)

                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // ⭐️ 삭제 모드일 때 체크박스
                            if (isDeleteMode) {
                                Checkbox(
                                    checked = selectedItemsForDelete.contains(item.id),
                                    onCheckedChange = {
                                        selectedItemsForDelete = if (it) {
                                            selectedItemsForDelete + item.id
                                        } else {
                                            selectedItemsForDelete - item.id
                                        }
                                    }
                                )
                            }

                            ClosetListItem(
                                imageUrl = item.url,
                                bigCategory = mainKo,
                                subCategory = subKo,
                                colorNames = item.colors,
                                material = item.material,
                                uploaded_at = item.uploaded_at,
                                onClick = {
                                    if (isDeleteMode) {
                                        selectedItemsForDelete = if (selectedItemsForDelete.contains(item.id)) {
                                            selectedItemsForDelete - item.id
                                        } else {
                                            selectedItemsForDelete + item.id
                                        }
                                    } else {
                                        selectedItem = item
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }

        // ⭐️ 우측 하단 버튼들
        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 삭제 모드일 때
            if (isDeleteMode) {
                // 선택된 개수 표시
                if (selectedItemsForDelete.isNotEmpty()) {
                    Surface(
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(24.dp),
                        color = Color.White.copy(alpha = 0.95f),
                        shadowElevation = 4.dp
                    ) {
                        Text(
                            text = "${selectedItemsForDelete.size}개 선택",
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                // 삭제하기 버튼
                if (selectedItemsForDelete.isNotEmpty()) {
                    FloatingActionButton(
                        onClick = { showDeleteConfirmDialog = true },
                        containerColor = Color.Red.copy(alpha = 0.9f),
                        contentColor = Color.White
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "삭제하기"
                        )
                    }
                }

                // 취소 버튼
                FloatingActionButton(
                    onClick = {
                        isDeleteMode = false
                        selectedItemsForDelete = emptySet()
                    },
                    containerColor = Color.White.copy(alpha = 0.9f),
                    contentColor = Color.Black
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "취소"
                    )
                }
            }
            // 일반 모드일 때
            else {
                // 휴지통 버튼 (삭제 모드 진입)
                FloatingActionButton(
                    onClick = { isDeleteMode = true },
                    containerColor = Color.White.copy(alpha = 0.9f),
                    contentColor = Color.Black
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "삭제 모드"
                    )
                }

                // + 버튼 (옷 추가)
                FloatingActionButton(
                    onClick = { showPhotoDialog = true },
                    containerColor = Color.White.copy(alpha = 0.9f),
                    contentColor = Color.Black
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "옷 추가하기"
                    )
                }
            }
        }
    }

    // ⭐️ 삭제 확인 다이얼로그
    if (showDeleteConfirmDialog) {
        DeleteConfirmDialog(
            selectedCount = selectedItemsForDelete.size,
            onConfirm = {
                // 삭제 실행
                selectedItemsForDelete.forEach { imageId ->
                    viewModel.deleteImage(
                        imageId = imageId,
                        onSuccess = {},
                        onError = { errorMsg ->
                            Toast.makeText(context, errorMsg, Toast.LENGTH_SHORT).show()
                        }
                    )
                }
                showDeleteConfirmDialog = false
                isDeleteMode = false
                selectedItemsForDelete = emptySet()
                Toast.makeText(context, "삭제되었습니다", Toast.LENGTH_SHORT).show()
            },
            onDismiss = { showDeleteConfirmDialog = false }
        )
    }

    // ✅ 사진 선택 다이얼로그
    if (showPhotoDialog) {
        com.example.wearther.closet.upload.PhotoSourceSelectionDialog(
            onDismiss = { showPhotoDialog = false },
            onImageSelected = { uri ->
                showPhotoDialog = false
                onNavigateToUpload(uri)
            }
        )
    }

    // ✅ 상세 정보 다이얼로그 (삭제 모드가 아닐 때만)
    if (!isDeleteMode) {
        selectedItem?.let { item ->
            ClothingDetailDialog(
                item = item,
                onDismiss = { selectedItem = null },
                onUpdate = { imageId, categoryEn, colorsEn, material, temperature ->
                    Log.d("ClosetScreen", "========== 수정 시작 ==========")
                    Log.d("ClosetScreen", "imageId: $imageId")
                    Log.d("ClosetScreen", "categoryEn: $categoryEn")
                    Log.d("ClosetScreen", "colorsEn: $colorsEn")
                    Log.d("ClosetScreen", "material: $material")
                    Log.d("ClosetScreen", "temperature: $temperature")

                    viewModel.updateImage(
                        imageId = imageId,
                        category = categoryEn,
                        colors = colorsEn,
                        material = material,
                        temperature = temperature,
                        onSuccess = {
                            selectedItem = null
                            Toast.makeText(context, "정보가 업데이트되었습니다!", Toast.LENGTH_SHORT).show()
                        },
                        onError = { errorMsg ->
                            Toast.makeText(context, errorMsg, Toast.LENGTH_LONG).show()
                        }
                    )
                }
            )
        }
    }
}