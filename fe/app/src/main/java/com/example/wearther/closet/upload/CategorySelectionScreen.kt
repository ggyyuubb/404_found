// closet/upload/screen/CategorySelectionScreen.kt (수정)
package com.example.wearther.closet.upload

import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.wearther.remote.BASE_URL
import com.example.wearther.remote.getStoredJwtToken
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
import java.io.FileOutputStream

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategorySelectionScreen(
    selectedImageUri: Uri,
    onNavigateBack: () -> Unit,
    onUploadSuccess: () -> Unit // 업로드 성공 시 호출
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var selectedType by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("") }
    var isUploading by remember { mutableStateOf(false) }

    val typeCategoryMap = mapOf(
        "상의" to listOf("민소매", "반소매", "긴소매", "후드", "셔츠", "스웨터"),
        "하의" to listOf("데님", "트레이닝", "슬랙스", "숏팬츠", "스커트"),
        "아우터" to listOf("후드집업", "자켓", "코트", "무스탕&퍼", "플리스", "패딩"),
        "원피스" to listOf("맥시", "나시원피스", "셔츠원피스")
    )

    val canProceed = selectedType.isNotEmpty() && selectedCategory.isNotEmpty() && !isUploading

    // Retrofit 설정
    val retrofit = remember {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
    val uploadApi = retrofit.create(UploadApi::class.java)

    // 업로드 함수
    fun uploadImage() {
        if (!canProceed) return
        Log.d("CategorySelection", "업로드 시작: type=$selectedType, category=$selectedCategory")

        coroutineScope.launch {
            isUploading = true
            try {
                val jwtToken = getStoredJwtToken(context)
                if (jwtToken == null) {
                    Toast.makeText(context, "로그인이 필요합니다", Toast.LENGTH_SHORT).show()
                    return@launch
                }

                // URI를 File로 변환
                val file = uriToFile(context, selectedImageUri)
                Log.d("CategorySelection", "파일 생성됨: ${file.name}, 크기: ${file.length()} bytes")
                val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
                val imagePart = MultipartBody.Part.createFormData("image", file.name, requestFile)
                val typePart = selectedType.toRequestBody("text/plain".toMediaTypeOrNull())
                val categoryPart = selectedCategory.toRequestBody("text/plain".toMediaTypeOrNull())

                Log.d("CategorySelection", "서버 요청 시작...")

                val response = uploadApi.uploadClothes(
                    token = "Bearer $jwtToken",
                    image = imagePart,
                    type = typePart,
                    category = categoryPart
                )
                Log.d("CategorySelection", "서버 응답: $response")

                // 임시 파일 삭제
                file.delete()

                if (response.error != null) {
                    Toast.makeText(context, "업로드 실패: ${response.error}", Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(context, "업로드 완료!", Toast.LENGTH_SHORT).show()
                    onUploadSuccess()
                }

            } catch (e: Exception) {
                Toast.makeText(context, "업로드 중 오류 발생: ${e.message}", Toast.LENGTH_LONG).show()
            } finally {
                isUploading = false
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        // Top Bar
        TopAppBar(
            title = { Text("카테고리 선택") },
            navigationIcon = {
                IconButton(onClick = onNavigateBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "뒤로가기")
                }
            },
            actions = {
                Button(
                    onClick = { uploadImage() },
                    enabled = canProceed,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (canProceed) Color.Black else Color.Gray,
                        contentColor = Color.White
                    )
                ) {
                    if (isUploading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Text(if (isUploading) "업로드 중..." else "업로드")
                }
            }
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // 선택된 이미지 미리보기 (전체 이미지 보이도록 수정)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp), // 높이 증가
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                AsyncImage(
                    model = selectedImageUri,
                    contentDescription = "선택된 이미지",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit // Crop 대신 Fit 사용으로 전체 이미지 표시
                )
            }

            // 타입 선택 섹션
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(
                    "옷의 종류를 선택해주세요",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(horizontal = 4.dp)
                ) {
                    items(typeCategoryMap.keys.toList()) { type ->
                        FilterChip(
                            onClick = {
                                selectedType = type
                                selectedCategory = "" // 타입 변경시 카테고리 초기화
                            },
                            label = {
                                Text(
                                    type,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            },
                            selected = selectedType == type,
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color.Black,
                                selectedLabelColor = Color.White,
                                containerColor = Color.Gray.copy(alpha = 0.1f),
                                labelColor = Color.Black
                            ),
                            modifier = Modifier.height(48.dp)
                        )
                    }
                }
            }

            // 세부 카테고리 선택 섹션
            if (selectedType.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text(
                        "세부 카테고리를 선택해주세요",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )

                    val categories = typeCategoryMap[selectedType] ?: emptyList()
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(3),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.height(((categories.size / 3 + 1) * 56).dp)
                    ) {
                        items(categories) { category ->
                            FilterChip(
                                onClick = { selectedCategory = category },
                                label = {
                                    Text(
                                        category,
                                        fontSize = 14.sp,
                                        maxLines = 1
                                    )
                                },
                                selected = selectedCategory == category,
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Color.Black,
                                    selectedLabelColor = Color.White,
                                    containerColor = Color.Gray.copy(alpha = 0.1f),
                                    labelColor = Color.Black
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                            )
                        }
                    }
                }
            }

            // 선택 상태 표시
            if (selectedType.isNotEmpty() && selectedCategory.isNotEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.Green.copy(alpha = 0.1f)
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = Color.Green,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                "선택 완료",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Green
                            )
                            Text(
                                "$selectedType > $selectedCategory",
                                fontSize = 14.sp,
                                color = Color.Gray
                            )
                        }
                        Spacer(modifier = Modifier.weight(1f))
                        Text(
                            "업로드 버튼을 눌러주세요",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    }
                }
            }
        }
    }
}

// URI를 File로 변환하는 헬퍼 함수
private fun uriToFile(context: android.content.Context, uri: Uri): File {
    val contentResolver = context.contentResolver
    val file = File(context.cacheDir, "temp_image_${System.currentTimeMillis()}.jpg")

    contentResolver.openInputStream(uri)?.use { input ->
        FileOutputStream(file).use { output ->
            input.copyTo(output)
        }
    }

    return file
}