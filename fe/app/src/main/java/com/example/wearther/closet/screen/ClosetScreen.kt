package com.example.wearther.closet.screen

import android.net.Uri
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
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
    var isGridView by remember { mutableStateOf(true) }   // ✅ 뷰 모드 상태 추가

    // 삭제 다이얼로그 상태
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
        filteredItems = ClosetSortUtils.sortItems(baseFiltered, currentSortOption)
        Log.d("ClosetScreen", "필터링 완료: 전체=${allItems.size}, 필터=${baseFiltered.size}, 정렬=${currentSortOption}")
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

    val onSortChange = { newSortOption: SortOption ->
        currentSortOption = newSortOption
        applyFiltering(activeCategory, activeSubCategory)
    }

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
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color(0xFFFFFFFF), // 흰색
                            Color(0xFFFAFAFA), // 아주아주 연한 회색
                            Color(0xFFF5F5F5), // 아주 연한 회색
                            Color(0xFFFAFAFA), // 아주아주 연한 회색
                            Color(0xFFFFFFFF)  // 흰색
                        )
                    )
                )
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
                onSortChange = onSortChange,
                isGridView = isGridView,
                onToggleView = { isGridView = !isGridView } // ✅ 버튼 연결
            )

            if (filteredItems.isEmpty()) {
                EmptyState()
            } else {
                if (isGridView) {
                    // ✅ 그리드 뷰
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        modifier = Modifier
                            .padding(horizontal = 16.dp, vertical = 16.dp)
                            .fillMaxSize(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        contentPadding = PaddingValues(bottom = 10.dp)
                    ) {
                        items(filteredItems) { item ->
                            ItemCard(
                                imageUrl = item.url,
                                category = item.category,
                                onDelete = { requestDelete(item) }
                            )
                        }
                    }
                } else {
                    // ✅ 리스트 뷰
                    LazyColumn(
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(bottom = 1.dp)
                    ) {
                        items(filteredItems) { item ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color.White, shape = MaterialTheme.shapes.medium)
                                    .padding(8.dp)
                            ) {
                                AsyncImage(
                                    model = item.url,
                                    contentDescription = "옷 이미지",
                                    modifier = Modifier
                                        .size(80.dp)
                                        .background(Color.LightGray, shape = MaterialTheme.shapes.small)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = item.category,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color.Black
                                )
                                Spacer(modifier = Modifier.weight(1f))
                                TextButton(onClick = { requestDelete(item) }) {
                                    Text("삭제", color = MaterialTheme.colorScheme.error)
                                }
                            }
                        }
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = { showPhotoDialog = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            containerColor = Color.Black,
            contentColor = Color.White
        ) {
            Icon(imageVector = Icons.Default.Add, contentDescription = "옷 추가하기")
        }
    }

    if (showPhotoDialog) {
        PhotoSourceSelectionDialog(
            onDismiss = { showPhotoDialog = false },
            onImageSelected = { uri ->
                showPhotoDialog = false
                onNavigateToUpload(uri)
            }
        )
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = {
                showDeleteDialog = false
                itemToDelete = null
            },
            title = { Text("옷 삭제", fontWeight = FontWeight.Bold) },
            text = { Text("이 옷을 삭제하시겠습니까?\n삭제한 옷은 복구할 수 없습니다.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        itemToDelete?.let { deleteImage(it.id) }
                        showDeleteDialog = false
                        itemToDelete = null
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("삭제")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showDeleteDialog = false
                    itemToDelete = null
                }) {
                    Text("취소")
                }
            }
        )
    }
}
