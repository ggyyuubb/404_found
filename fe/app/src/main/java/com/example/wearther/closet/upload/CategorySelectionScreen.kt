// closet/upload/screen/CategorySelectionScreen.kt
package com.example.wearther.closet.upload

import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.ui.draw.scale
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
    onUploadSuccess: () -> Unit
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

    // ✅ Retrofit 설정
    val retrofit = remember {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
    val uploadApi = retrofit.create(UploadApi::class.java)

    // ✅ 업로드 함수
    fun uploadImage() {
        if (!canProceed) return
        coroutineScope.launch {
            isUploading = true
            try {
                val jwtToken = getStoredJwtToken(context)
                if (jwtToken == null) {
                    Toast.makeText(context, "로그인이 필요합니다", Toast.LENGTH_SHORT).show()
                    return@launch
                }

                val file = uriToFile(context, selectedImageUri)
                val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
                val imagePart = MultipartBody.Part.createFormData("image", file.name, requestFile)
                val typePart = selectedType.toRequestBody("text/plain".toMediaTypeOrNull())
                val categoryPart = selectedCategory.toRequestBody("text/plain".toMediaTypeOrNull())

                val response = uploadApi.uploadClothes(
                    token = "Bearer $jwtToken",
                    image = imagePart,
                    type = typePart,
                    category = categoryPart
                )

                file.delete()

                if (response.error != null) {
                    Toast.makeText(context, "업로드 실패: ${response.error}", Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(context, "업로드 완료!", Toast.LENGTH_SHORT).show()
                    onUploadSuccess()
                }

            } catch (e: Exception) {
                Toast.makeText(context, "업로드 중 오류: ${e.message}", Toast.LENGTH_LONG).show()
            } finally {
                isUploading = false
            }
        }
    }

    // ✅ 애니메이션 효과
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
                title = { Text("카테고리 선택", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "뒤로가기")
                    }
                },
                actions = {
                    // ✅ 업로드 버튼 (애니메이션 적용)
                    FilledTonalButton(
                        onClick = { uploadImage() },
                        enabled = canProceed,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.scale(scale),
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
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // ✅ 이미지 미리보기
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth(0.8f)
                            .aspectRatio(0.75f),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        AsyncImage(
                            model = selectedImageUri,
                            contentDescription = "선택된 이미지",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                }

                // ✅ 타입 선택
                Column {
                    Text("옷의 종류", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Spacer(Modifier.height(12.dp))

                    LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        items(typeCategoryMap.keys.toList()) { type ->
                            FilterChip(
                                selected = selectedType == type,
                                onClick = {
                                    selectedType = type
                                    selectedCategory = ""
                                },
                                label = { Text(type) },
                                shape = RoundedCornerShape(20.dp),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }
                }

                // ✅ 세부 카테고리
                if (selectedType.isNotEmpty()) {
                    Column {
                        Text("세부 카테고리", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Spacer(Modifier.height(12.dp))

                        val categories = typeCategoryMap[selectedType] ?: emptyList()
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(3),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.height(((categories.size / 3 + 1) * 56).dp)
                        ) {
                            items(categories) { category ->
                                FilterChip(
                                    selected = selectedCategory == category,
                                    onClick = { selectedCategory = category },
                                    label = { Text(category, fontSize = 14.sp) },
                                    shape = RoundedCornerShape(20.dp),
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                                        selectedLabelColor = Color.White,
                                        containerColor = Color.Gray.copy(alpha = 0.1f),
                                        labelColor = Color.Black
                                    ),
                                    modifier = Modifier.height(40.dp)
                                )
                            }
                        }
                    }
                }
            }

            // ✅ 선택 완료 안내 (하단 고정)
            if (selectedType.isNotEmpty() && selectedCategory.isNotEmpty()) {
                Card(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .wrapContentHeight()
                        .padding(16.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF2C2C2C)
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
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
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            "$selectedType > $selectedCategory 선택됨",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

// ✅ URI → File 변환 함수
private fun uriToFile(context: android.content.Context, uri: Uri): File {
    val contentResolver = context.contentResolver
    val file = File(context.cacheDir, "temp_image_${System.currentTimeMillis()}.jpg")
    contentResolver.openInputStream(uri)?.use { input ->
        FileOutputStream(file).use { output -> input.copyTo(output) }
    }
    return file
}
