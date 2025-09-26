// closet/screen/ClosetScreen.kt (정렬 로직 분리 후)
package com.example.wearther.closet.screen

import android.net.Uri
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.wearther.closet.data.ClosetApi
import com.example.wearther.closet.data.ClosetImage
import com.example.wearther.closet.data.ItemCard
import com.example.wearther.closet.upload.PhotoSourceSelectionDialog
import com.example.wearther.remote.BASE_URL
import com.example.wearther.remote.getStoredJwtToken
import com.example.wearther.ui.screens.closet.components.CategoryTabs
import com.example.wearther.ui.screens.closet.components.EmptyState
import com.example.wearther.ui.screens.closet.components.Header
import com.example.wearther.ui.screens.closet.components.SortOption
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

@Composable
fun ClosetScreen(
    onNavigateToUpload: (Uri) -> Unit = {}
) {
    val context = LocalContext.current
    val jwtToken = getStoredJwtToken(context)
    val tokenHeader = "Bearer $jwtToken"

    val retrofit = remember {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
    val closetApi = retrofit.create(ClosetApi::class.java)

    var activeCategory by remember { mutableStateOf("전체") }
    var activeSubCategory by remember { mutableStateOf("전체") }
    var allItems by remember { mutableStateOf<List<ClosetImage>>(emptyList()) }
    var filteredItems by remember { mutableStateOf<List<ClosetImage>>(emptyList()) }
    var currentSortOption by remember { mutableStateOf(SortOption.CATEGORY) }

    // 삭제 확인 다이얼로그 상태
    var showDeleteDialog by remember { mutableStateOf(false) }
    var itemToDelete by remember { mutableStateOf<ClosetImage?>(null) }

    val coroutineScope = rememberCoroutineScope()

    fun applyFiltering(type: String, subCategory: String) {
        val baseFiltered = if (type == "전체" || subCategory == "전체") {
            allItems
        } else {
            allItems.filter {
                it.category.contains(subCategory)
            }
        }

        // 정렬 유틸리티 사용
        filteredItems = ClosetSortUtils.sortItems(baseFiltered, currentSortOption)

        Log.d("ClosetScreen", "필터링 완료: 전체=${allItems.size}개, 필터=${baseFiltered.size}개, 정렬=${currentSortOption}")
    }

    fun fetchClosetImages(type: String) {
        coroutineScope.launch {
            try {
                val response = closetApi.getMyImages(
                    token = tokenHeader,
                    type = if (type == "전체") null else type,
                    category = null
                )
                Log.d("ClosetScreen", "받아온 이미지 개수: ${response.images.size}")
                allItems = response.images
                applyFiltering(type, activeSubCategory)
            } catch (e: Exception) {
                Log.e("ClosetScreen", "옷장 가져오기 실패: ${e.message}")
            }
        }
    }

    fun deleteImage(id: String) {
        coroutineScope.launch {
            try {
                closetApi.deleteImage(tokenHeader, id)
                fetchClosetImages(activeCategory)
            } catch (e: Exception) {
                Log.e("ClosetScreen", "삭제 실패: ${e.message}")
            }
        }
    }

    // 정렬 옵션 변경 함수
    val onSortChange = { newSortOption: SortOption ->
        Log.d("ClosetScreen", "정렬 옵션 변경: $currentSortOption -> $newSortOption")
        currentSortOption = newSortOption
        applyFiltering(activeCategory, activeSubCategory)
    }

    // 삭제 확인 요청 함수
    val requestDelete = { item: ClosetImage ->
        itemToDelete = item
        showDeleteDialog = true
    }

    LaunchedEffect(Unit) {
        fetchClosetImages(activeCategory)
    }

    val topCategories = listOf("전체", "상의", "하의", "아우터", "원피스")

    var showPhotoDialog by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Gray.copy(alpha = 0.05f))
        ) {
            CategoryTabs(
                categories = topCategories,
                activeCategory = activeCategory,
                onCategoryChange = {
                    activeCategory = it
                    activeSubCategory = "전체"
                    fetchClosetImages(it)
                },
                activeSubCategory = activeSubCategory,
                onSubCategoryChange = {
                    activeSubCategory = it
                    applyFiltering(activeCategory, it)
                }
            )

            Header(
                totalItems = filteredItems.size,
                currentSortOption = currentSortOption,
                onSortChange = onSortChange
            )

            if (filteredItems.isEmpty()) {
                EmptyState()
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier
                        .padding(horizontal = 16.dp, vertical = 16.dp)
                        .fillMaxSize(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    items(filteredItems) { item ->
                        ItemCard(
                            imageUrl = item.url,
                            category = item.category,
                            onDelete = { requestDelete(item) }
                        )
                    }
                }
            }
        }

        // + 버튼
        FloatingActionButton(
            onClick = {
                showPhotoDialog = true
            },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            containerColor = Color.Black,
            contentColor = Color.White
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "옷 추가하기"
            )
        }
    }

    // 사진 선택 다이얼로그
    if (showPhotoDialog) {
        PhotoSourceSelectionDialog(
            onDismiss = { showPhotoDialog = false },
            onImageSelected = { uri ->
                showPhotoDialog = false
                onNavigateToUpload(uri)
            }
        )
    }

    // 삭제 확인 다이얼로그
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = {
                showDeleteDialog = false
                itemToDelete = null
            },
            title = {
                Text(
                    text = "옷 삭제",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text("이 옷을 삭제하시겠습니까?\n삭제한 옷은 복구할 수 없습니다.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        itemToDelete?.let { item ->
                            deleteImage(item.id)
                        }
                        showDeleteDialog = false
                        itemToDelete = null
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("삭제")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        itemToDelete = null
                    }
                ) {
                    Text("취소")
                }
            }
        )
    }
}