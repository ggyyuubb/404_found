package com.example.wearther.community.screen

import android.Manifest
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.content.FileProvider
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.wearther.home.recommendation.RecommendationViewModel
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddPostScreen(
    navController: NavController,
    recommendationViewModel: RecommendationViewModel
) {
    var description by remember { mutableStateOf("") }
    var temperature by remember { mutableStateOf("") }
    var weather by remember { mutableStateOf("") }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var selectedImageUrl by remember { mutableStateOf<String?>(null) } // AI 추천 이미지 URL
    var showPhotoDialog by remember { mutableStateOf(false) }

    val context = LocalContext.current

    LaunchedEffect(Unit) {
        navController.currentBackStackEntry
            ?.savedStateHandle
            ?.get<String>("selected_image_url")
            ?.let { url ->
                selectedImageUrl = url
                selectedImageUri = null
                navController.currentBackStackEntry
                    ?.savedStateHandle
                    ?.remove<String>("selected_image_url")
            }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("게시글 작성") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "뒤로가기")
                    }
                },
                actions = {
                    TextButton(
                        onClick = {
                            // TODO: 게시글 저장 로직 (이미지 Uri 또는 URL 사용)
                            if (description.isNotEmpty()) {
                                Toast.makeText(
                                    context,
                                    "게시글이 등록되었습니다",
                                    Toast.LENGTH_SHORT
                                ).show()
                                navController.popBackStack()
                            } else {
                                Toast.makeText(
                                    context,
                                    "내용을 입력해주세요",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        },
                        enabled = description.isNotEmpty()
                    ) {
                        Text(
                            "등록",
                            style = MaterialTheme.typography.titleMedium,
                            color = if (description.isNotEmpty())
                                MaterialTheme.colorScheme.primary
                            else Color.Gray
                        )
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 이미지 업로드 영역
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFF3F4F6))
                    .clickable { showPhotoDialog = true },
                contentAlignment = Alignment.Center
            ) {
                when {
                    // URI 이미지 (카메라/갤러리)
                    selectedImageUri != null -> {
                        Image(
                            painter = rememberAsyncImagePainter(selectedImageUri),
                            contentDescription = "선택된 이미지",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                    // URL 이미지 (AI 추천)
                    selectedImageUrl != null -> {
                        Image(
                            painter = rememberAsyncImagePainter(selectedImageUrl),
                            contentDescription = "AI 추천 이미지",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                    // 플레이스홀더
                    else -> {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AddAPhoto,
                                contentDescription = "사진 추가",
                                tint = Color(0xFF9CA3AF),
                                modifier = Modifier.size(56.dp)
                            )
                            Text(
                                text = "사진 추가",
                                style = MaterialTheme.typography.titleMedium,
                                color = Color(0xFF6B7280),
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "탭하여 촬영, 앨범 선택, AI 추천",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF9CA3AF)
                            )
                        }
                    }
                }

                // 이미지 삭제 버튼
                if (selectedImageUri != null || selectedImageUrl != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(8.dp),
                        contentAlignment = Alignment.TopEnd
                    ) {
                        IconButton(
                            onClick = {
                                selectedImageUri = null
                                selectedImageUrl = null
                            },
                            modifier = Modifier
                                .background(
                                    Color.Black.copy(alpha = 0.5f),
                                    RoundedCornerShape(50)
                                )
                        ) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "이미지 삭제",
                                tint = Color.White
                            )
                        }
                    }
                }
            }

            // 설명 입력
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("오늘의 코디를 소개해주세요") },
                placeholder = { Text("예: 오늘 날씨 완전 좋아요! 가을 코디 추천합니다 🍂") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp),
                maxLines = 5,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = Color(0xFFE5E7EB)
                )
            )

            // 날씨 정보
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = temperature,
                    onValueChange = { temperature = it },
                    label = { Text("온도") },
                    placeholder = { Text("18°C") },
                    leadingIcon = {
                        Icon(
                            Icons.Default.Thermostat,
                            contentDescription = null,
                            tint = Color(0xFF9CA3AF)
                        )
                    },
                    modifier = Modifier.weight(1f),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = Color(0xFFE5E7EB)
                    )
                )
                OutlinedTextField(
                    value = weather,
                    onValueChange = { weather = it },
                    label = { Text("날씨") },
                    placeholder = { Text("맑음") },
                    leadingIcon = {
                        Icon(
                            Icons.Default.WbSunny,
                            contentDescription = null,
                            tint = Color(0xFF9CA3AF)
                        )
                    },
                    modifier = Modifier.weight(1f),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = Color(0xFFE5E7EB)
                    )
                )
            }

            // 안내 카드
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFF0F9FF)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Lightbulb,
                        contentDescription = null,
                        tint = Color(0xFF0EA5E9),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "오늘 날씨에 어울리는 코디를 공유해보세요!",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF0369A1)
                    )
                }
            }
        }
    }

    // 사진 선택 다이얼로그
    if (showPhotoDialog) {
        PhotoSelectionDialog(
            onDismiss = { showPhotoDialog = false },
            onImageUriSelected = { uri ->
                selectedImageUri = uri
                selectedImageUrl = null
            },
            onAIRecommendation = {
                // AI 추천 화면으로 이동하여 이미지 선택
                navController.navigate("ai_recommendation_picker") {
                    launchSingleTop = true
                }
                showPhotoDialog = false
            }
        )
    }
}

@Composable
fun PhotoSelectionDialog(
    onDismiss: () -> Unit,
    onImageUriSelected: (Uri) -> Unit,
    onAIRecommendation: () -> Unit
) {
    val context = LocalContext.current
    var cameraImageUri by remember { mutableStateOf<Uri?>(null) }

    // 갤러리 런처
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            onImageUriSelected(it)
            onDismiss()
        }
    }

    // 카메라 런처
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && cameraImageUri != null) {
            onImageUriSelected(cameraImageUri!!)
            onDismiss()
        }
    }

    // 카메라 실행 함수
    fun launchCamera() {
        try {
            val photoFile = File(
                context.getExternalFilesDir(Environment.DIRECTORY_PICTURES),
                "IMG_${System.currentTimeMillis()}.jpg"
            )

            cameraImageUri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                photoFile
            )

            cameraLauncher.launch(cameraImageUri)
        } catch (e: Exception) {
            Toast.makeText(context, "카메라 실행 중 오류가 발생했습니다", Toast.LENGTH_SHORT).show()
        }
    }

    // 카메라 권한 런처
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            launchCamera()
        } else {
            Toast.makeText(context, "카메라 권한이 필요합니다", Toast.LENGTH_SHORT).show()
        }
    }

    // 갤러리 권한 런처
    val galleryPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            galleryLauncher.launch("image/*")
        } else {
            Toast.makeText(context, "갤러리 접근 권한이 필요합니다", Toast.LENGTH_SHORT).show()
        }
    }

    // 갤러리 실행 함수
    fun launchGallery() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            galleryPermissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES)
        } else {
            galleryLauncher.launch("image/*")
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(8.dp)
        ) {
            Column(
                modifier = Modifier.padding(28.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // 헤더 아이콘
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .background(
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                            shape = RoundedCornerShape(50)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.AddAPhoto,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(36.dp)
                    )
                }

                // 타이틀
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        "사진 추가",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                    Text(
                        "사진을 불러올 방법을 선택하세요",
                        fontSize = 14.sp,
                        color = Color(0xFF6B7280)
                    )
                }

                // 버튼 영역
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // 📷 카메라 버튼
                    ElevatedButton(
                        onClick = {
                            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.elevatedButtonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Icon(
                            Icons.Default.CameraAlt,
                            contentDescription = null,
                            modifier = Modifier.size(22.dp),
                            tint = Color.White
                        )
                        Spacer(Modifier.width(12.dp))
                        Text(
                            "카메라로 촬영하기",
                            fontSize = 16.sp,
                            color = Color.White
                        )
                    }

                    // 🖼️ 갤러리 버튼
                    OutlinedButton(
                        onClick = { launchGallery() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Icon(
                            Icons.Default.PhotoLibrary,
                            contentDescription = null,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(Modifier.width(12.dp))
                        Text("갤러리에서 선택하기", fontSize = 16.sp)
                    }

                    // 🤖 AI 추천 버튼
                    OutlinedButton(
                        onClick = {
                            onAIRecommendation()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color(0xFF8B5CF6)
                        )
                    ) {
                        Icon(
                            Icons.Default.AutoAwesome,
                            contentDescription = null,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(Modifier.width(12.dp))
                        Text("AI 코디 추천받기", fontSize = 16.sp)
                    }
                }

                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 8.dp),
                    color = Color(0xFFE5E7EB)
                )

                // 취소 버튼
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("취소", color = Color(0xFF6B7280), fontSize = 16.sp)
                }
            }
        }
    }
}