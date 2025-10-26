package com.example.wearther.community.screen

import android.Manifest
import android.net.Uri
import android.os.Build
import android.os.Environment
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.wearther.community.vm.CommunityViewModel
import kotlinx.coroutines.launch
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddPostScreen(
    navController: NavController,
    viewModel: CommunityViewModel
) {
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var description by remember { mutableStateOf("") }
    var temperature by remember { mutableStateOf("") }
    var weather by remember { mutableStateOf("") }

    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var aiImageUrl by remember { mutableStateOf<String?>(null) }
    var pendingCameraUri by remember { mutableStateOf<Uri?>(null) }

    val isUploading by viewModel.isLoading.collectAsState()
    var showImageSourceDialog by remember { mutableStateOf(false) }

    // AI 추천 이미지 URL 수신
    val selectedAiImageUrl = navController.currentBackStackEntry
        ?.savedStateHandle
        ?.get<String>("selected_image_url")

    LaunchedEffect(selectedAiImageUrl) {
        selectedAiImageUrl?.let {
            aiImageUrl = it
            selectedImageUri = null
            navController.currentBackStackEntry
                ?.savedStateHandle
                ?.remove<String>("selected_image_url")
        }
    }

    fun showSnack(msg: String) {
        scope.launch { snackbarHostState.showSnackbar(msg) }
    }

    // 카메라 촬영
    val takePictureLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            selectedImageUri = pendingCameraUri
            aiImageUrl = null
        }
        pendingCameraUri = null
    }

    // 카메라 권한
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            try {
                val photoFile = File(
                    context.getExternalFilesDir(Environment.DIRECTORY_PICTURES),
                    "IMG_${System.currentTimeMillis()}.jpg"
                )

                val fileUri = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    photoFile
                )

                pendingCameraUri = fileUri
                takePictureLauncher.launch(fileUri)
            } catch (e: Exception) {
                showSnack("카메라 실행 중 오류가 발생했습니다")
            }
        } else {
            showSnack("카메라 권한이 필요합니다.")
        }
    }

    fun takePhoto() {
        cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
    }

    // 갤러리 선택 (Android 13+)
    val pickPhoto13Plus = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        uri?.let {
            selectedImageUri = it
            aiImageUrl = null
        }
    }

    // 갤러리 선택 (Legacy)
    val pickPhotoLegacy = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            selectedImageUri = it
            aiImageUrl = null
        }
    }

    fun pickFromGallery() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            pickPhoto13Plus.launch(
                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
            )
        } else {
            pickPhotoLegacy.launch("image/*")
        }
    }

    // 🔥 수정된 업로드 함수
    fun upload() {
        if (description.isBlank()) {
            showSnack("내용을 입력해 주세요.")
            return
        }

        // 1) 로컬 이미지가 있을 때 - 멀티파트 업로드 사용
        selectedImageUri?.let { uri ->
            viewModel.addFeedWithImage(
                context = context,
                description = description,
                temperature = temperature.ifBlank { "N/A" },
                weather = weather.ifBlank { "N/A" },
                imageUri = uri
            )
            // 성공/실패는 ViewModel의 errorMessage StateFlow로 확인
            scope.launch {
                // 업로드 완료 대기
                kotlinx.coroutines.delay(1000)
                if (viewModel.errorMessage.value == null) {
                    showSnack("등록 완료!")
                    navController.popBackStack()
                } else {
                    showSnack(viewModel.errorMessage.value ?: "업로드 실패")
                }
            }
            return
        }

        // 2) AI 이미지 URL만 있을 때
        aiImageUrl?.let { url ->
            viewModel.addFeed(
                description = description,
                temperature = temperature.ifBlank { "N/A" },
                weather = weather.ifBlank { "N/A" },
                imageUrl = url
            )
            scope.launch {
                kotlinx.coroutines.delay(1000)
                if (viewModel.errorMessage.value == null) {
                    showSnack("등록 완료!")
                    navController.popBackStack()
                } else {
                    showSnack(viewModel.errorMessage.value ?: "업로드 실패")
                }
            }
            return
        }

        // 3) 텍스트만 게시
        viewModel.addFeed(
            description = description,
            temperature = temperature.ifBlank { "N/A" },
            weather = weather.ifBlank { "N/A" },
            imageUrl = null
        )
        scope.launch {
            kotlinx.coroutines.delay(1000)
            if (viewModel.errorMessage.value == null) {
                showSnack("등록 완료!")
                navController.popBackStack()
            } else {
                showSnack(viewModel.errorMessage.value ?: "업로드 실패")
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("게시글 작성") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "뒤로가기")
                    }
                },
                actions = {
                    TextButton(
                        onClick = { upload() },
                        enabled = !isUploading && description.isNotBlank()
                    ) {
                        Text(
                            if (isUploading) "업로드 중..." else "등록",
                            color = if (!isUploading && description.isNotBlank())
                                MaterialTheme.colorScheme.primary
                            else Color.Gray
                        )
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // 이미지 미리보기
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFFF3F4F6))
                    .clickable { showImageSourceDialog = true },
                contentAlignment = Alignment.Center
            ) {
                when {
                    selectedImageUri != null -> {
                        Image(
                            painter = rememberAsyncImagePainter(selectedImageUri),
                            contentDescription = "선택한 이미지",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                    aiImageUrl != null -> {
                        Image(
                            painter = rememberAsyncImagePainter(aiImageUrl),
                            contentDescription = "AI 추천 이미지",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                    else -> {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.CameraAlt,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = Color.Gray
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("사진 추가", color = Color.Gray)
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            Button(
                onClick = { navController.navigate("ai_recommendation_picker") },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                )
            ) {
                Icon(imageVector = Icons.Filled.PhotoLibrary, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("AI 추천 코디 선택")
            }

            Spacer(Modifier.height(24.dp))

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("내용을 입력하세요") },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 120.dp),
                maxLines = 10
            )

            Spacer(Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = temperature,
                    onValueChange = { temperature = it },
                    label = { Text("온도") },
                    placeholder = { Text("예: 18°C") },
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = weather,
                    onValueChange = { weather = it },
                    label = { Text("날씨") },
                    placeholder = { Text("예: 맑음") },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(Modifier.height(12.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                FilledTonalButton(
                    enabled = !isUploading,
                    onClick = { takePhoto() }
                ) {
                    Icon(Icons.Filled.CameraAlt, contentDescription = null)
                    Spacer(Modifier.width(6.dp))
                    Text("카메라")
                }
                OutlinedButton(
                    enabled = !isUploading,
                    onClick = { pickFromGallery() }
                ) {
                    Icon(Icons.Filled.PhotoLibrary, contentDescription = null)
                    Spacer(Modifier.width(6.dp))
                    Text("갤러리")
                }
                Spacer(Modifier.weight(1f))
                Button(
                    enabled = !isUploading && description.isNotBlank(),
                    onClick = { upload() }
                ) {
                    Text(if (isUploading) "업로드 중..." else "업로드")
                }
            }
        }
    }

    if (showImageSourceDialog) {
        AlertDialog(
            onDismissRequest = { showImageSourceDialog = false },
            title = { Text("사진 선택") },
            text = {
                Column {
                    TextButton(
                        onClick = {
                            showImageSourceDialog = false
                            takePhoto()
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Filled.CameraAlt, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("카메라로 촬영")
                    }
                    TextButton(
                        onClick = {
                            showImageSourceDialog = false
                            pickFromGallery()
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Filled.PhotoLibrary, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("갤러리에서 선택")
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showImageSourceDialog = false }) {
                    Text("닫기")
                }
            }
        )
    }
}