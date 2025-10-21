package com.example.wearther.closet.screen

import android.net.Uri
import android.util.Log
import android.widget.Toast
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
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.wearther.closet.data.ClosetImage
import com.example.wearther.closet.data.ClosetViewModel
import com.example.wearther.closet.data.ClosetViewModelFactory
import com.example.wearther.closet.data.ClosetSortUtils
import com.example.wearther.closet.upload.UpdateClothingRequest
import com.example.wearther.remote.BASE_URL
import com.example.wearther.remote.getStoredJwtToken
import com.example.wearther.ui.screens.closet.components.CategoryTabs
import com.example.wearther.ui.screens.closet.components.Header
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File

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

    // ✅ UI State (ViewModel에 없는 것들)
    var isGridView by remember { mutableStateOf(true) }
    var selectedItem by remember { mutableStateOf<ClosetImage?>(null) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var itemToDelete by remember { mutableStateOf<ClosetImage?>(null) }
    var showPhotoDialog by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()
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
            } else if (isGridView) {
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
                        val subKo = ClosetSortUtils.toKorean(item.category)
                        val mainKo = ClosetSortUtils.getMainCategory(subKo)
                        com.example.wearther.closet.screen.ItemCard(
                            imageUrl = item.url,
                            bigCategory = mainKo,
                            subCategory = subKo,
                            colorNames = item.colors,
                            onClick = { selectedItem = item },
                            onDelete = {
                                itemToDelete = item
                                showDeleteDialog = true
                            }
                        )
                    }
                }
            }
            // ✅ 리스트 뷰
            else {
                LazyColumn(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    items(filteredItems) { item ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp)
                        ) {
                            AsyncImage(
                                model = item.url,
                                contentDescription = "옷 이미지",
                                modifier = Modifier.size(80.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = item.type ?: "미분류",
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Black
                                )
                                Text(
                                    text = ClosetSortUtils.toKorean(item.category),
                                    fontWeight = FontWeight.Normal,
                                    color = Color.Gray
                                )
                            }
                            Spacer(modifier = Modifier.weight(1f))
                            TextButton(
                                onClick = {
                                    itemToDelete = item
                                    showDeleteDialog = true
                                }
                            ) {
                                Text("삭제", color = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }
            }
        }

        // ✅ FAB (옷 추가)
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

    // ✅ 사진 선택 다이얼로그
    if (showPhotoDialog) {
        com.example.wearther.closet.upload.PhotoSourceSelectionDialog(
            onDismiss = { showPhotoDialog = false },
            onImageSelected = { uri ->
                showPhotoDialog = false
                coroutineScope.launch {
                    try {
                        val file = uriToFile(context, uri)
                        val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
                        val imagePart = MultipartBody.Part.createFormData("image", file.name, requestFile)

                        Toast.makeText(context, "AI 분석 중...", Toast.LENGTH_SHORT).show()

                        val uploadApi = retrofit.create(com.example.wearther.closet.upload.UploadApi::class.java)
                        val response = uploadApi.uploadImage(
                            token = tokenHeader,
                            image = imagePart
                        )

                        file.delete()

                        Toast.makeText(context, "AI가 자동으로 분류 완료!", Toast.LENGTH_SHORT).show()
                        viewModel.fetchClosetImages()

                    } catch (e: Exception) {
                        Log.e("ClosetScreen", "업로드 실패", e)
                        Toast.makeText(context, "업로드 실패: ${e.message}", Toast.LENGTH_LONG).show()
                    }
                }
            }
        )
    }

    // ✅ 상세 정보 다이얼로그 - 수정 기능 연결
    selectedItem?.let { item ->
        ClothingDetailDialog(
            item = item,
            onDismiss = { selectedItem = null },
            onDelete = {
                viewModel.deleteImage(
                    imageId = item.id,
                    onSuccess = {
                        selectedItem = null
                        Toast.makeText(context, "삭제되었습니다", Toast.LENGTH_SHORT).show()
                    },
                    onError = { error ->
                        Toast.makeText(context, error, Toast.LENGTH_LONG).show()
                    }
                )
            },
            // ✅ 수정 기능 - ViewModel의 updateImage() 사용
            onUpdate = { categoryEn, colorsEn, material, temperature ->
                Log.d("ClosetScreen", "========== 수정 시작 ==========")
                Log.d("ClosetScreen", "categoryEn: $categoryEn")
                Log.d("ClosetScreen", "colorsEn: $colorsEn")
                Log.d("ClosetScreen", "material: $material")
                Log.d("ClosetScreen", "temperature: $temperature")

                viewModel.updateImage(
                    imageId = item.id,
                    category = categoryEn,      // 영문 세부 카테고리
                    colors = colorsEn,          // 영문 색상 배열 ["Blue", "Gray"]
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

    // ✅ 삭제 확인 다이얼로그
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
                        itemToDelete?.let { item ->
                            viewModel.deleteImage(
                                imageId = item.id,
                                onSuccess = {
                                    Toast.makeText(context, "삭제되었습니다", Toast.LENGTH_SHORT).show()
                                },
                                onError = { error ->
                                    Toast.makeText(context, error, Toast.LENGTH_LONG).show()
                                }
                            )
                        }
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

private fun uriToFile(context: android.content.Context, uri: Uri): File {
    val file = File(context.cacheDir, "temp_${System.currentTimeMillis()}.jpg")
    context.contentResolver.openInputStream(uri)?.use { input ->
        file.outputStream().use { output -> input.copyTo(output) }
    }
    return file
}