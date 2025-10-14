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
import coil.compose.AsyncImage
import com.example.wearther.closet.data.ClosetApi
import com.example.wearther.closet.data.ClosetImage
import com.example.wearther.closet.data.ItemCard
import com.example.wearther.closet.screen.ClothingDetailDialog
import com.example.wearther.closet.upload.PhotoSourceSelectionDialog
import com.example.wearther.remote.BASE_URL
import com.example.wearther.remote.getStoredJwtToken
import com.example.wearther.ui.screens.closet.components.CategoryTabs
import com.example.wearther.ui.screens.closet.components.CategoryMapper
import com.example.wearther.ui.screens.closet.components.Header
import com.example.wearther.ui.screens.closet.components.SortOption
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
    var isGridView by remember { mutableStateOf(true) }
    var selectedItem by remember { mutableStateOf<ClosetImage?>(null) }

    var showDeleteDialog by remember { mutableStateOf(false) }
    var itemToDelete by remember { mutableStateOf<ClosetImage?>(null) }

    val coroutineScope = rememberCoroutineScope()

    fun applyFiltering(type: String, subCategory: String) {
        val baseFiltered = if (subCategory == "전체") {
            // 서브카테고리가 "전체"일 때
            if (type == "전체") {
                allItems  // 대분류도 "전체" → 모든 아이템
            } else {
                // 대분류만 선택 (상의/하의/아우터/원피스)
                allItems.filter { item ->
                    val koreanCategory = CategoryMapper.toKorean(item.clothing_type)

                    when (type) {
                        "상의" -> koreanCategory in listOf("민소매", "반소매", "긴소매", "후드", "셔츠", "스웨터")
                        "하의" -> koreanCategory in listOf("면바지", "데님", "트레이닝팬츠", "슬랙스", "반바지", "스커트")
                        "아우터" -> koreanCategory in listOf("블레이저", "가디건", "코트", "롱패딩", "숏패딩", "후드집업", "플리스", "점퍼")
                        "원피스" -> koreanCategory == "원피스"
                        else -> true
                    }
                }
            }
        } else {
            // 서브카테고리 선택 시 (민소매, 반소매 등)
            allItems.filter { item ->
                val koreanCategory = CategoryMapper.toKorean(item.clothing_type)
                koreanCategory == subCategory
            }
        }

        filteredItems = ClosetSortUtils.sortItems(baseFiltered, currentSortOption)
        Log.d("ClosetScreen", "필터링 완료: type=$type, sub=$subCategory, 전체=${allItems.size}, 필터=${baseFiltered.size}")
    }

    fun fetchClosetImages(type: String) {
        coroutineScope.launch {
            try {
                Log.e("JWT_CHECK", "현재 JWT: $jwtToken")
                Log.e("JWT_CHECK", "토큰 헤더: $tokenHeader")

                val response = closetApi.getMyImages(
                    token = tokenHeader,
                    type = if (type == "전체") null else type,
                    category = null
                )
                Log.d("ClosetScreen", "받아온 이미지 개수: ${response.images.size}")

                response.images.forEach { item ->
                    Log.d("ClosetScreen", "ID: ${item.id}, clothing_type: '${item.clothing_type}'")
                }

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
                            Color(0xFFFFFFFF),
                            Color(0xFFFAFAFA),
                            Color(0xFFF5F5F5),
                            Color(0xFFFAFAFA),
                            Color(0xFFFFFFFF)
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
                onToggleView = { isGridView = !isGridView }
            )

            if (filteredItems.isEmpty()) {
                EmptyState()
            } else {
                if (isGridView) {
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
                                clothingType = item.clothing_type,
                                onClick = { selectedItem = item },
                                onDelete = { requestDelete(item) }
                            )
                        }
                    }
                } else {
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
                                    text = CategoryMapper.toKorean(item.clothing_type),
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

    // ===== 기존 코드 (주석 처리) =====
    /*
    if (showPhotoDialog) {
        PhotoSourceSelectionDialog(
            onDismiss = { showPhotoDialog = false },
            onImageSelected = { uri ->
                showPhotoDialog = false
                onNavigateToUpload(uri)
            }
        )
    }
    */

    // ===== 임시 테스트: AI 자동 분류 업로드 =====
    if (showPhotoDialog) {
        PhotoSourceSelectionDialog(
            onDismiss = { showPhotoDialog = false },
            onImageSelected = { uri ->
                Log.e("UPLOAD_DEBUG", "========== 이미지 선택됨 ==========")
                Log.e("UPLOAD_DEBUG", "선택된 URI: $uri")

                showPhotoDialog = false
                coroutineScope.launch {
                    try {
                        Log.e("UPLOAD_DEBUG", "JWT 토큰: $jwtToken")
                        Log.e("UPLOAD_DEBUG", "토큰 헤더: $tokenHeader")

                        val file = uriToFile(context, uri)
                        Log.e("UPLOAD_DEBUG", "파일 생성 완료: ${file.name}, 크기: ${file.length()} bytes")

                        val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
                        val imagePart = MultipartBody.Part.createFormData("image", file.name, requestFile)
                        Log.e("UPLOAD_DEBUG", "MultipartBody 생성 완료")

                        Toast.makeText(context, "AI 분석 중...", Toast.LENGTH_SHORT).show()

                        val uploadRetrofit = Retrofit.Builder()
                            .baseUrl(BASE_URL)
                            .addConverterFactory(GsonConverterFactory.create())
                            .build()
                        val uploadApi = uploadRetrofit.create(com.example.wearther.closet.upload.UploadApi::class.java)

                        Log.e("UPLOAD_DEBUG", "API 호출 시작 - BASE_URL: $BASE_URL")

                        val response = uploadApi.uploadImage(
                            token = tokenHeader,
                            image = imagePart
                        )

                        Log.e("UPLOAD_DEBUG", "✅ API 호출 성공!")
                        Log.e("UPLOAD_DEBUG", "응답 메시지: ${response.message}")
                        Log.e("UPLOAD_DEBUG", "응답 ID: ${response.id}")
                        Log.e("UPLOAD_DEBUG", "응답 URL: ${response.url}")
                        Log.e("UPLOAD_DEBUG", "AI 결과 존재 여부: ${response.ai_result != null}")

                        file.delete()

                        Log.d("AI자동분류", "========================================")
                        Log.d("AI자동분류", "옷 종류: ${response.ai_result?.clothing_type}")
                        Log.d("AI자동분류", "색상: ${response.ai_result?.colors}")
                        Log.d("AI자동분류", "재질: ${response.ai_result?.material}")
                        Log.d("AI자동분류", "적정 온도: ${response.ai_result?.suitable_temperature}")
                        Log.d("AI자동분류", "========================================")

                        Toast.makeText(context, "AI가 자동으로 분류 완료!", Toast.LENGTH_SHORT).show()
                        fetchClosetImages(activeCategory)

                    } catch (e: Exception) {
                        Log.e("UPLOAD_DEBUG", "❌❌❌ 업로드 실패 ❌❌❌")
                        Log.e("UPLOAD_DEBUG", "에러 메시지: ${e.message}")
                        Log.e("UPLOAD_DEBUG", "에러 타입: ${e.javaClass.simpleName}")
                        e.printStackTrace()

                        Toast.makeText(context, "업로드 실패: ${e.message}", Toast.LENGTH_LONG).show()
                    }
                }
            }
        )
    }

    selectedItem?.let { item ->
        ClothingDetailDialog(
            item = item,
            onDismiss = { selectedItem = null },
            onDelete = {
                deleteImage(item.id)
                selectedItem = null
            },
            onUpdate = { clothingType, colors, material, temperature ->
                coroutineScope.launch {
                    try {
                        val uploadRetrofit = Retrofit.Builder()
                            .baseUrl(BASE_URL)
                            .addConverterFactory(GsonConverterFactory.create())
                            .build()
                        val uploadApi = uploadRetrofit.create(com.example.wearther.closet.upload.UploadApi::class.java)

                        uploadApi.updateClothing(
                            token = tokenHeader,
                            imageId = item.id,
                            clothingType = clothingType,
                            material = material,
                            suitableTemperature = temperature
                        )

                        Toast.makeText(context, "정보가 업데이트되었습니다!", Toast.LENGTH_SHORT).show()
                        fetchClosetImages(activeCategory)
                        selectedItem = null
                    } catch (e: Exception) {
                        Log.e("UpdateClothing", "업데이트 실패: ${e.message}", e)
                        Toast.makeText(context, "업데이트 실패: ${e.message}", Toast.LENGTH_LONG).show()
                    }
                }
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

private fun uriToFile(context: android.content.Context, uri: Uri): File {
    val file = File(context.cacheDir, "temp_${System.currentTimeMillis()}.jpg")
    context.contentResolver.openInputStream(uri)?.use { input ->
        file.outputStream().use { output -> input.copyTo(output) }
    }
    return file
}